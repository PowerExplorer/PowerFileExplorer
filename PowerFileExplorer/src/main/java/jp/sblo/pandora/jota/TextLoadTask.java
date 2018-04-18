package jp.sblo.pandora.jota;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import jp.sblo.pandora.jota.text.SpannableStringBuilder;
import net.gnu.explorer.R;

public class TextLoadTask extends AsyncTask<String, Integer, SpannableStringBuilder>{

    private static final boolean USE_JUNIVERSALCHARDET=true;

    OnFileLoadListener mFileLoadListener=null;
    private int mLineToChar=-1;

    private String mFilename;
    private String mCharset;
    private int mLinebreak;
    private ProgressDialog mProgressDialog;
    private Activity mActivity;
    private int mLine;
    private ContentResolver mCr;
    private boolean mSuppressMessage;

    public interface OnFileLoadListener
    {
        void onPreFileLoad();
        void onFileLoaded( SpannableStringBuilder result , String filename , String charset , int linebreak , int mOffset );
    }


    public TextLoadTask( Activity activity , OnFileLoadListener postProc , int initline , boolean suppressMessage )
    {
        mFileLoadListener = postProc;
        mActivity = activity;
        mLine = initline;
        mCr = mActivity.getContentResolver();
        mSuppressMessage = suppressMessage;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if ( mFileLoadListener!= null ){
            mFileLoadListener.onPreFileLoad();
        }
        mProgressDialog = new ProgressDialog(mActivity);
//        mProgressDialog.setTitle(R.string.spinner_message);
        mProgressDialog.setMessage(mActivity.getString(R.string.spinner_message));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
    @Override
    protected SpannableStringBuilder doInBackground(String... params)
    {
        String uri = params[0];
        String charset = params[1];
        if ( uri.startsWith("content://") ){
            // content provider
            try {
                return openFile( mCr.openInputStream(Uri.parse(uri)),charset );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            // file
            File f = new File(uri);
            if ( f.exists() ){
                mFilename = uri;
                try{
                    return openFile( new FileInputStream( f ),charset );
                } catch( Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected SpannableStringBuilder openFile(InputStream input , String encode)
    {
        SpannableStringBuilder result=new SpannableStringBuilder();
        InputStream is;
        try {
            mCharset = "utf-8";
            mLinebreak = LineBreak.LF;

            is = new BufferedInputStream( input , 65536 );
            is.mark(65536);

            // preread leading 64KB
            int nread;
            byte[] buff = new byte[64*1024];
            nread = is.read(buff);

            if ( nread <= 0 ){
                if ( encode.length() != 0 ){
                    mCharset = encode;
                }
                return new SpannableStringBuilder("");
            }
//            if ( USE_JUNIVERSALCHARDET ){
                UniversalDetector detector;
                if ( encode ==null || encode.length() == 0 ){
                    try {
                        detector = new UniversalDetector();
                        detector.handleData(buff, 0, nread);
                        detector.dataEnd();
                        encode = detector.getCharset();
                        detector.reset();
                    } catch (Exception e1) {
                    }
                }
//            }else{
//                // Detect charset
//                NativeUniversalDetector detector;
//                if ( encode ==null || encode.length() == 0 ){
//
//                    try {
//                        detector = new NativeUniversalDetector();
//                        detector.handleData(buff, 0, nread);
//                        detector.dataEnd();
//                        encode = detector.getCharset();
//                        detector.destroy();
//                    } catch (DetectorException e1) {
//                    }
//                }
//            }
            is.reset();
            // detect linbreak code
            if ( encode == null || encode.length() == 0 ){
                encode = "utf-8";
            }
            Charset charset = Charset.forName(encode);

            byte[] cr = new byte[] { '\r' , };
            byte[] lf = new byte[] { '\n' , };
            if ( charset != null ){
                ByteBuffer bb;
                bb = charset.encode("\r");
                cr = new byte[bb.limit()];
                bb.get(cr);
                bb = charset.encode("\n");
                lf = new byte[bb.limit()];
                bb.get(lf);
            }

            int linebreak = LineBreak.LF;
            if ( cr.length == 1 ){
                for( int i=0;i<nread-1 ;i++ ){
                    if ( buff[i] == lf[0] ){
                        linebreak = LineBreak.LF;
                        break;
                    } else if ( buff[i] == cr[0] ){
                        if ( buff[i+1] == lf[0] ){
                            linebreak = LineBreak.CRLF;
                        }else{
                            linebreak = LineBreak.CR;
                        }
                        break;
                    }
               }
            }else{      // cr.length == 2 // we dont think in the case cr.length>2
                for( int i=0;i<nread-2 ;i+=2 ){
                    if ( buff[i] == lf[0] && buff[i+1]==lf[1] ){
                        linebreak = LineBreak.LF;
                        break;
                    } else if ( buff[i] == cr[0] && buff[i+1]==cr[1] ){
                        if ( buff[i+2] == lf[0] && buff[i+3]==lf[1] ){
                            linebreak = LineBreak.CRLF;
                        }else{
                            linebreak = LineBreak.CR;
                        }
                        break;
                    }
               }
            }
//            if ( encode != null ){
//                Log.e( TAG , "CharSet="+encode+"Linebreak=" + new String[]{"CR","LF","CRLF"}[linebreak]);
//            }else{
//                Log.e( TAG , "CharSet="+"--"+"Linebreak=" + new String[]{"CR","LF","CRLF"}[linebreak]);
//            }
            mCharset = encode;
            mLinebreak = linebreak;

            BufferedReader br=null;
            try {
                br = new BufferedReader( new InputStreamReader( is , encode ) , 8192*2 );

                int line=0;
                String text;
                while(  ( text = br.readLine() )!=null ){
                    // remove BOM
                    if ( line == 0 ){
                        if ( text.length() > 0 && text.charAt(0) == 0xfeff ){
                            text = text.substring(1);
                        }
                    }

                    line++;
                    if ( line== mLine ){
                        mLineToChar = result.length();
                    }
                    result.append( text );
                    result.append( '\n' );
                }
                br.close();
                is.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(SpannableStringBuilder result)
    {
        try{
            mProgressDialog.dismiss();
        }catch(Exception e){}
        mProgressDialog = null;
        if ( result != null ){
            if ( !mSuppressMessage ){
                if ( mFilename != null ){
                    String[] linebreak =  mActivity.getResources().getStringArray(R.array.LineBreak);
                    String name = new File(mFilename).getName();
                    String message = mActivity.getString(R.string.toast_opening_message ,mCharset , linebreak[mLinebreak] ,name );
                    Toast.makeText(mActivity, message , Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mActivity, R.string.toast_open_via_content_provider , Toast.LENGTH_LONG).show();
                }
            }
        }else{
            Toast.makeText(mActivity, R.string.toast_open_failed, Toast.LENGTH_LONG).show();
        }
        mActivity = null;
        if ( mFileLoadListener!= null ){
            mFileLoadListener.onFileLoaded( result, mFilename, mCharset, mLinebreak , mLineToChar );
        }
    }
}

//class TextLoadTask extends AsyncTask<String, Integer, String>{
//
//    @Override
//    protected String doInBackground(String... params)
//    {
//    }
//
//    @Override
//    protected void onPostExecute(String result)
//    {
//        if ( result != null ){
//            SpannableString ss = new SpannableString( result );
//
//            if ( mSearchWord != null && mSearchWord.length() > 0 ){
//                Pattern p = Pattern.compile(mSearchWord);
//
//                Matcher m;
//
//                m = p.matcher(result);
//
//                int start = 0;
//                int end = 0;
//                while ( m.find(start) ){
//                    start = m.start();
//                    end = m.end();
//
//                    BackgroundColorSpan span = new BackgroundColorSpan( 0xFF00FFFF );
//                    ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    start = end;
//                }
//            }
//            mEditor.setText(ss);
//            mEditor.setChanged(false);
//            if ( mLineToChar > 0 ){
//                mEditor.setSelection(mLineToChar);
//            }
//
//            if ( mLine != -1 ){
//                final Rect rect = new Rect();
//                mEditor.getLineBounds(mLine, rect);
//
////            mScrollView.post(new Runnable() {
////                public void run() {
////                    mScrollView.scrollTo(0 , rect.bottom );
////                }
////            });
//            }else{
//                if ( mSelStart >=0 && mSelEnd >= 0 ){
//                    int len = mEditor.length();
//                    if ( mSelStart >= len ){
//                        mSelStart = len-1;
//                    }
//                    if ( mSelEnd >= len ){
//                        mSelEnd = len-1;
//                    }
//                    mEditor.setSelection(mSelStart,mSelEnd);
//                    mEditor.centerCursor();
//                }
//            }
//        }
//    }
//}

