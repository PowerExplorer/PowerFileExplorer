package net.gnu.explorer;

import android.support.v7.app.AppCompatActivity;

import com.amaze.filemanager.utils.AppConfig;
import com.amaze.filemanager.utils.color.ColorPreference;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.utils.provider.UtilitiesProviderInterface;
import com.amaze.filemanager.utils.theme.AppTheme;
import com.amaze.filemanager.utils.theme.AppThemeManager;
import android.os.*;
import com.amaze.filemanager.activities.*;
import android.support.v4.app.*;
import com.afollestad.materialdialogs.*;
import android.view.*;
import android.*;
import com.amaze.filemanager.ui.dialogs.*;
import android.content.pm.*;
import android.util.*;
import android.support.design.widget.*;
import android.widget.*;
import android.support.annotation.*;
import net.gnu.androidutil.*;

public class StorageCheckActivity extends ThemedActivity {
	
	protected static final int REQUEST_WRITE_EXTERNAL = 77;

    protected static String[] PERMISSIONS_STORAGE = {
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_MEDIA_STORAGE,
//		Manifest.permission.ACCESS_WIFI_STATE,
//		Manifest.permission.CHANGE_WIFI_STATE,
//		Manifest.permission.ACCESS_NETWORK_STATE,
//		Manifest.permission.CHANGE_NETWORK_STATE,
//		Manifest.permission.INTERNET,
//		Manifest.permission.BLUETOOTH,
//		Manifest.permission.BLUETOOTH_ADMIN,
//		Manifest.permission.RECORD_AUDIO,
//		Manifest.permission.CAMERA,
//		Manifest.permission.INSTALL_SHORTCUT,
//		Manifest.permission.UNINSTALL_SHORTCUT,
//		Manifest.permission.SET_WALLPAPER,
//		Manifest.permission.GET_TASKS,
//		Manifest.permission.REORDER_TASKS,
//		Manifest.permission.KILL_BACKGROUND_PROCESSES,
//		Manifest.permission.WAKE_LOCK,
	};
	
    protected boolean checkStorage = true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requesting storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStorage)
            if (!checkStoragePermission())
                requestStoragePermission();
    }

    public boolean checkStoragePermission() {
        // Verify that all required contact permissions have been granted.
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
			== PackageManager.PERMISSION_GRANTED;
    }

    protected void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
																Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            final MaterialDialog materialDialog = GeneralDialogCreation.showBasicDialog(this,
																						new String[]{getString(R.string.granttext),
																							getString(R.string.grantper),
																							getString(R.string.grant),
																							getString(R.string.cancel),
																							null});
            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityCompat.requestPermissions(StorageCheckActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL);
						materialDialog.dismiss();
					}
				});
            materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
            materialDialog.setCancelable(false);
            materialDialog.show();

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
		if (requestCode == REQUEST_WRITE_EXTERNAL) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //refreshDrawer();
            } else {
                Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
}
