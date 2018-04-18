package jp.sblo.pandora.jota;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import net.gnu.explorer.R;

public class RecoveryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String recoveryname = getRecoveryFileName(this);
        if ( "".equals(recoveryname) ){
            finish();
            return;
        }

        new AlertDialog.Builder(this)
            .setIcon(R.drawable.icon)
            .setTitle(R.string.label_crash_title)
            .setMessage(R.string.label_crash_message)
            .setPositiveButton(R.string.label_overwrite,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        copyFile(recoveryname);
                        finish();
                    }
                })
            .setNeutralButton(R.string.label_sdroot,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        String dst = new File(recoveryname).getName();
                        String sdroot = Environment.getExternalStorageDirectory().getPath();

                        String dstname = sdroot+ "/" + dst;

                        while( new File(dstname).exists() ){
                            dst = "~" + dst;
                            dstname = sdroot+ "/" + dst;
                        }
                        copyFile(dstname);

                        finish();
                    }
                })
            .setNegativeButton(R.string.label_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
            .show();
    }

    private boolean copyFile(String dstname)
    {
        File dst = new File(dstname);
        try {
            dst.getParentFile().mkdirs();
            InputStream in = openFileInput(TextSaveTask.RECOVERY_FILENAME);
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
            in.close();
            out.close();
            cleanUp();
            String name = new File(dstname).getName();
            String message = this.getString(R.string.toast_saved_message ,name );
            Toast.makeText(this, message , Toast.LENGTH_LONG).show();
            if ( JotaTextEditor.sFroyo ){
                ApiWrapper.scanFile(this, new String[]{dstname,}, new String[]{"text/plain",});
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static String getRecoveryFileName(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(TextSaveTask.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString( TextSaveTask.PREF_KEY_FILENAME , "" );
    }

    private void cleanUp()
    {
        SharedPreferences sp = getSharedPreferences(TextSaveTask.PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove( TextSaveTask.PREF_KEY_FILENAME  ).commit();

        deleteFile( TextSaveTask.RECOVERY_FILENAME );

    }

}
