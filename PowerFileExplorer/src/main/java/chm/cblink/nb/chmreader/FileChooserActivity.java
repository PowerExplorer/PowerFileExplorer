//package chm.cblink.nb.chmreader;
//
//import android.content.Intent;
//import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import com.github.angads25.filepicker.controller.DialogSelectionListener;
//import com.github.angads25.filepicker.model.DialogConfigs;
//import com.github.angads25.filepicker.model.DialogProperties;
//import com.github.angads25.filepicker.view.FilePickerDialog;
//
//import java.io.File;
//
//public class FileChooserActivity extends AppCompatActivity implements DialogSelectionListener {
//    ImageView imageViewChooseFile;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_file_chooser);
//        imageViewChooseFile = (ImageView) findViewById(R.id.first_choose);
//
////        Intent showFile = new Intent(this, MainActivity.class);
////        showFile.putExtra("fileName", Environment.getExternalStorageDirectory().getAbsolutePath() +"/Power.chm");
////        startActivity(showFile);
//    }
//
//    public FilePickerDialog getFilePickerDialog() {
//        DialogProperties properties = new DialogProperties();
//        properties.selection_mode = DialogConfigs.SINGLE_MODE;
//        properties.selection_type = DialogConfigs.FILE_SELECT;
//        properties.root = new File(DialogConfigs.DEFAULT_DIR);
//        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
//        properties.extensions = new String[]{".chm"};
//
//        FilePickerDialog dialog = new FilePickerDialog(this, properties);
//        dialog.setDialogSelectionListener(this);
//        dialog.setTitle("Select a File");
//        return dialog;
//    }
//
//    @Override
//    public void onSelectedFilePaths(String[] files) {
//        if(files.length > 0){
//            Intent showFile = new Intent(this, MainActivity.class);
//            showFile.putExtra("fileName", files[0]);
//            startActivity(showFile);
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        showFileChoose(null);
//        return true;
//    }
//
//
//    public void showFileChoose(View view) {
//        getFilePickerDialog().show();
//    }
//}
