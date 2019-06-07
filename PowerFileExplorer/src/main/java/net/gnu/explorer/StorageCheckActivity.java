package net.gnu.explorer;

import android.app.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.annotation.*;
import android.content.pm.*;
import android.widget.*;
import android.view.*;
import net.gnu.explorer.R;
import android.*;
import com.afollestad.materialdialogs.*;
import android.support.v7.app.*;
import com.amaze.filemanager.activities.*;
import com.amaze.filemanager.ui.dialogs.*;

public class StorageCheckActivity extends ThemedActivity {
	
	protected static final int REQUEST_WRITE_EXTERNAL = 77;
	protected static final int REQUEST_WRITE_MEDIA = 78;
	
    protected static String[] PERMISSIONS_STORAGE = {
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_MEDIA_STORAGE,
	};
	
    protected boolean checkStorage = true;
	private MaterialDialog materialDialog;
	
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
            materialDialog = GeneralDialogCreation.showBasicDialog(this,
											 new String[]{getString(R.string.granttext),
												 getString(R.string.grantper),
												 getString(R.string.grant),
												 getString(R.string.cancel),
												 null});
            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityCompat.requestPermissions(StorageCheckActivity.this, 
														  PERMISSIONS_STORAGE, 
														  REQUEST_WRITE_EXTERNAL);
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

	@Override
	protected void onDestroy() {
		//Log.d("StorageCheckActivity", "onDestroy");
		super.onDestroy();
		if (materialDialog != null) {
			materialDialog.dismiss();
			materialDialog = null;
		}
	}
	
}
