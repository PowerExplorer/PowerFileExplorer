package jp.sblo.pandora.jota;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;
import net.gnu.explorer.R;

public class TextSaveTask extends AsyncTask<CharSequence, Integer, String>{

    Runnable mPreProc=null;
    Runnable mPostProc=null;
    Activity mActivity;
    private ProgressDialog mProgressDialog;

    public static final String RECOVERY_FILENAME = "lost.found";
    public static final String PREF_NAME = "recoverey_pref";
    public static final String PREF_KEY_FILENAME = "filename";
    private boolean mSuppressMessage;

    public TextSaveTask( Activity activity ,Runnable preProc , Runnable postProc, boolean suppressMessage)
    {
        mActivity = activity;
        mPreProc = preProc;
        mPostProc = postProc;
        mSuppressMessage = suppressMessage;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if ( mPreProc!= null ){
            mPreProc.run();
        }
        try{
            mProgressDialog = new ProgressDialog(mActivity);
            // mProgressDialog.setTitle(R.string.spinner_message);
            mProgressDialog.setMessage(mActivity.getString(R.string.spinner_message));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        catch(Exception e)
        {
            mProgressDialog = null;
        }
    }

    private boolean createBackup(String filename , boolean createBackup)
    {
        File f = new File(filename);

        if (f.exists()) {
            if (createBackup) {
                File backup = new File(filename + "~");
                if (backup.exists()) {
                    backup.delete();
                }

                // patch by matthias.gruenewald@googlemail.com
                // Renaming breaks tasker's file modified event feature
                // So we copy the file instead
                // f.renameTo(backup);
                try {
                    FileInputStream in = new FileInputStream(f);
                    FileOutputStream out = new FileOutputStream(backup);
                    byte[] buf = new byte[1024];
                    int i = 0;
                    while ((i = in.read(buf)) != -1) {
                        out.write(buf, 0, i);
                    }
                    in.close();
                    out.close();
                    if ( JotaTextEditor.sFroyo ){
                        ApiWrapper.scanFile(mActivity, new String[]{backup.getPath(),}, new String[]{"text/plain",});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                // patch by matthias.gruenewald@googlemail.com
                // Deleting the file before writing it breaks tasker's file
                // modified event, so just override the file contents
                // f.delete();
            }
        }
        return true;
    }

    private boolean saveFile(OutputStream os , String charset , String lb , CharSequence text)
    {

        try{
            BufferedWriter bw=null;
            bw = new BufferedWriter( new OutputStreamWriter( os , Charset.forName(charset) ) , 65536 );

            if ( charset.startsWith("UTF-16") || charset.startsWith("UTF-32") ){
                char bom = 0xFEFF;
                bw.write(bom);
            }

            int totallen = text.length();
            final int BUFSIZE=4096;
            int processed = 0;
            while( processed < totallen ){
                int len = Math.min(BUFSIZE, totallen - processed);
                String temptext = text.subSequence(processed, processed + len).toString();
                int pos0 = 0;
                while( pos0<len ){
                    int pos1 = temptext.indexOf('\n',pos0) ;
                    if ( pos1 ==  -1 ){
                        pos1 = len;
                        if ( pos0!=pos1 ){
                            bw.write(temptext, pos0, pos1-pos0);
                        }
                    }else{
                        if ( pos0!=pos1 ){
                            bw.write(temptext, pos0, pos1-pos0);
                        }
                        bw.write(lb);
                    }
                    pos0 = pos1 + 1;
                }
                processed += len;
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected String doInBackground(CharSequence... params)
    {
        String filename = (String)params[0] ;
        String charset = (String)params[1] ;
        String lb = (String)params[2] ;
        CharSequence text = params[3];
        boolean createBackup = "true".equals(params[4]);

        try{
            if ( createBackup( filename , createBackup ) ){
                if ( saveFile( new FileOutputStream( new File(filename) ), charset , lb , text) ){
                    if ( JotaTextEditor.sFroyo ){
                        ApiWrapper.scanFile(mActivity, new String[]{filename,}, new String[]{"text/plain",});
                    }
                    return filename;
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        // failsafe
        try{
            OutputStream os = mActivity.openFileOutput(RECOVERY_FILENAME, Context.MODE_PRIVATE);
            saveFile(os , charset , lb , text);

            SharedPreferences sp = mActivity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().putString( PREF_KEY_FILENAME , filename ).commit();

            NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = new Notification(
                    R.drawable.icon,
                    mActivity.getString(R.string.notify_title),
                    System.currentTimeMillis());

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            Intent intent = new Intent(mActivity , RecoveryActivity.class );
            PendingIntent contentIntent =
                  PendingIntent.getActivity(mActivity, 0, intent, 0);

            notification.setLatestEventInfo(
                    mActivity.getApplicationContext(),
                    mActivity.getString(R.string.notify_title),
                    mActivity.getString(R.string.notify_message),
                    contentIntent);

            notificationManager.notify(R.string.app_name, notification);
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onPostExecute(String result)
    {
        if (mProgressDialog != null ){
            try{
                mProgressDialog.dismiss();
            }
            catch( Exception e){}
            mProgressDialog = null;
        }
        if ( result != null ){
            if ( mPostProc!= null ){
                if ( !mSuppressMessage  ){
                    String name = new File(result).getName();
                    String message = mActivity.getString(R.string.toast_saved_message ,name );
                    Toast.makeText(mActivity, message , Toast.LENGTH_LONG).show();
                }
                mPostProc.run();
            }
        }else{
            Toast.makeText(mActivity, R.string.toast_save_failed, Toast.LENGTH_LONG).show();
        }
        mActivity = null;
    }
}
