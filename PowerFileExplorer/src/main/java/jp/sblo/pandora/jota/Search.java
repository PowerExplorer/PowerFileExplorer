package jp.sblo.pandora.jota;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;
import net.gnu.explorer.R;



public class Search
{
    private ArrayList<Record>	mData ;
    private SearchTask mTask;
    private String mQuery;
    private Pattern mPattern;
    private Activity mParent;
    private CharSequence mText;
    OnSearchFinishedListener mListener;

    public static class Record {
        public int start;
        public int end;
    };
    public interface OnSearchFinishedListener {
        void onSearchFinished( ArrayList<Record> data );
    }

    public Search(Activity parent ,String query , CharSequence text , boolean regexp , boolean ignoreCase , OnSearchFinishedListener listener)
    {
        mData = new ArrayList<Record>();
        mQuery = query;
        mParent = parent;
        mText = text;
        mListener = listener;
        if ( query!=null && query.length() >0 ){
            String patternText = query;
            if ( !regexp ){
                patternText = escapeMetaChar(patternText);
            }
            try{
                if ( ignoreCase ){
                    mPattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                }else{
                    mPattern = Pattern.compile(patternText);
                }

                mTask = new SearchTask();
                mTask.execute();
            }
            catch( PatternSyntaxException e ){
                Toast.makeText(parent, R.string.toast_syntax_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    static public String escapeMetaChar( String pattern )
    {
        final String metachar = ".^$[]*+?|()\\";

        StringBuilder newpat = new StringBuilder();

        int len = pattern.length();

        for( int i=0;i<len;i++ ){
            char c = pattern.charAt(i);
            if ( metachar.indexOf(c) >=0 ){
                newpat.append('\\');
            }
            newpat.append(c);
        }
        return newpat.toString();
    }


    class SearchTask extends AsyncTask<String, Record, Boolean>
    {
        private ProgressDialog mProgressDialog;
        private boolean mCancelled;

        @Override
        protected void onPreExecute() {
            mCancelled=false;
            mProgressDialog = new ProgressDialog(mParent);
            mProgressDialog.setTitle(R.string.spinner_message);
            mProgressDialog.setMessage(mQuery);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog)
                {
                    mCancelled=true;
                    cancel(false);
                }
            });
            mProgressDialog.show();
            mParent = null;
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            if ( isCancelled() ){
                return true;
            }
            Pattern pattern = mPattern;
            Matcher m = pattern.matcher( mText );
            while ( m.find() ){
                Record record = new Record();
                record.start = m.start();
                record.end = m.end();
                mData.add( record );
                if ( mCancelled ){
                    break;
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            mListener.onSearchFinished(mData);
            mData = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            onPostExecute(false);
        }


    }

}
