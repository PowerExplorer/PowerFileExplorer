package jp.sblo.pandora.jota;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import net.gnu.explorer.R;

public class FileSelectorActivity extends ListActivity {

	public static final String INTENT_DIRPATH = "DIRPATH";
	public static final String INTENT_FILENAME = "FILENAME";
	public static final String INTENT_FILEPATH = "FILEPATH";
	public static final String INTENT_MODE = "MODE";
    final public static String INTENT_EXTENSION = "EXT";
    final public static String INTENT_INIT_PATH = "INIPATH";
    public static final String INTENT_CHARSET = "CHARSET";
    public static final String INTENT_LINEBREAK = "LINEBREAK";
	public static final String MODE_OPEN = "OPEN";
    public static final String MODE_SAVE = "SAVE";
    public static final String MODE_DIR = "DIR";
    public static final String MODE_FONT = "FONT";
    public static final String MODE_PICTURE = "PICTURE";
    public static final String INTENT_TITLE = "TITLE";


	private String mMode=null;
    private String m_strDirPath;
    private String m_strFileName;
	private List<String> items = null;
    private Button mBtnOK;
    private Button mBtnCancel;
    private TextView mTxtFilePath;
    private EditText mEdtFileName;
    private String[] mExtension = null;
    private Spinner mCharsetSpinnerOpen;
    private Spinner mCharsetSpinnerSave;
    private Spinner mLinebreakSpinner;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);


        mBtnOK = (Button)findViewById(R.id.btnOK);
        mBtnCancel = (Button)findViewById(R.id.btnCancel);
        mTxtFilePath = (TextView)findViewById(R.id.txtFilePath);
        mEdtFileName = (EditText)findViewById(R.id.edtFileName);

        mCharsetSpinnerOpen = (Spinner)findViewById(R.id.spinner_charset_open);
        mCharsetSpinnerSave = (Spinner)findViewById(R.id.spinner_charset_save);
        mLinebreakSpinner = (Spinner)findViewById(R.id.spinner_linebreak);

        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);

        Bundle extras = intent.getExtras();

        if (extras != null) {
            mMode = extras.getString(INTENT_MODE);
            mExtension = (String[]) extras.get(INTENT_EXTENSION);
            m_strDirPath = extras.getString(INTENT_INIT_PATH);

            String charset = extras.getString(INTENT_CHARSET);
            if ( charset != null ){
                selectItemOfSpinner( mCharsetSpinnerOpen , charset );
                selectItemOfSpinner( mCharsetSpinnerSave , charset );
            }

            int linebreak = extras.getInt(INTENT_LINEBREAK , -1);
            mLinebreakSpinner.setSelection(linebreak+1);
        }

        File file = new File ( m_strDirPath );
        if ( !file.isDirectory() ){
            m_strDirPath = file.getParent();
            m_strFileName = file.getName();
        }

        if ( mMode == null ){
            Log.e("JotaFileSelector", "No MODE parameter specified");
            finish();
            return;
        }

        if ( MODE_OPEN.equals(mMode)  ){
            setTitle(R.string.fs_title_open);
            mBtnOK.setVisibility(View.GONE);
            mEdtFileName.setVisibility(View.GONE);
            mEdtFileName.setEnabled(false);
            mCharsetSpinnerOpen.setVisibility(View.VISIBLE);
            mCharsetSpinnerSave.setVisibility(View.GONE);
            mLinebreakSpinner.setVisibility(View.GONE);

        } else if( MODE_SAVE.equals(mMode) ) {
            setTitle(R.string.fs_title_save);
            mEdtFileName.setEnabled(true);
            mEdtFileName.setText(m_strFileName);
            mCharsetSpinnerOpen.setVisibility(View.GONE);
            mCharsetSpinnerSave.setVisibility(View.VISIBLE);
            mLinebreakSpinner.setVisibility(View.VISIBLE);

        } else if ( MODE_DIR.equals(mMode)){
            setTitle(R.string.fs_title_dir);
            mEdtFileName.setEnabled(false);
            mEdtFileName.setVisibility(View.GONE);
            mCharsetSpinnerOpen.setVisibility(View.GONE);
            mCharsetSpinnerSave.setVisibility(View.GONE);
            mLinebreakSpinner.setVisibility(View.GONE);

        } else if ( MODE_FONT.equals(mMode)){
            setTitle(R.string.fs_title_font);
            mEdtFileName.setEnabled(false);
            mEdtFileName.setVisibility(View.GONE);
            mCharsetSpinnerOpen.setVisibility(View.GONE);
            mCharsetSpinnerSave.setVisibility(View.GONE);
            mLinebreakSpinner.setVisibility(View.GONE);

        } else if ( MODE_PICTURE.equals(mMode)){
            setTitle(R.string.fs_title_picture);
            mEdtFileName.setEnabled(false);
            mEdtFileName.setVisibility(View.GONE);
            mCharsetSpinnerOpen.setVisibility(View.GONE);
            mCharsetSpinnerSave.setVisibility(View.GONE);
            mLinebreakSpinner.setVisibility(View.GONE);

        }else{
            Log.e("JotaFileSelector", "MODE parameter must be OPEN , SAVE , FONT or DIR.");
            finish();
            return;
        }


        mEdtFileName.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if( MODE_SAVE.equals(mMode) ) {
                    if ( s.toString().indexOf('/')<0 ){
                        String strFilePath;
                        if( m_strDirPath.equals("/")) {
                            strFilePath = "/" + s;
                        } else {
                            strFilePath = m_strDirPath + "/" + s;
                        }

                        File f = new File(strFilePath);
                        mBtnOK.setEnabled( !f.isDirectory() );
                    }else{
                        mBtnOK.setEnabled( false );
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
        });
        mEdtFileName.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ( (keyCode == KeyEvent.KEYCODE_ENTER  || keyCode == KeyEvent.KEYCODE_DPAD_CENTER )
                        && event.getAction() == KeyEvent.ACTION_UP ) {
                    if ( mBtnOK.isEnabled() ){
                        mBtnOK.performClick();
                    }
                }
                return false;
            }
        });
//        mEdtFileName.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//                if ( hasFocus && mEdtFileName.isEnabled() ){
//                    String strFileName = mEdtFileName.getText().toString();
//                    if ( strFileName.indexOf('/') >= 0 ){
//                        mEdtFileName.setText("");
//                    }
//                }
//            }
//        });

        mBtnOK.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if( MODE_SAVE.equals(mMode) ) {
                    String strFileName = mEdtFileName.getText().toString();
                    String strFilePath;
                    if( m_strDirPath.equals("/")) {
                        strFilePath = "/" + strFileName;
                    } else {
                        strFilePath = m_strDirPath + "/" + strFileName;
                    }

                    if ( new File(strFilePath).exists() ){
                        new AlertDialog.Builder(FileSelectorActivity.this)
                        .setTitle(R.string.confirmation)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                returnSaveFilepath();
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, null)
                        .setCancelable(true)
                        .setMessage( getString(R.string.confirm_overwrite))
                        .show();
                    }else{
                        returnSaveFilepath();
                    }
                }else{
                    returnDirectory();
                }
            }

        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult( RESULT_CANCELED , null );
                finish();
            }

        });

        fillList();
	}

	private void returnSaveFilepath(){
        Intent intent = new Intent();

        String strFileName = mEdtFileName.getText().toString();
        intent.putExtra(INTENT_FILENAME, strFileName );
        intent.putExtra(INTENT_DIRPATH, m_strDirPath );

        if (mCharsetSpinnerSave.getSelectedItemPosition() != 0
                && mCharsetSpinnerSave.getSelectedItemPosition() != android.widget.AdapterView.INVALID_POSITION) {
            intent.putExtra(INTENT_CHARSET, (String)mCharsetSpinnerSave.getSelectedItem());
        }else{
            intent.putExtra(INTENT_CHARSET, "");
        }
        if ( mLinebreakSpinner.getSelectedItemPosition() != android.widget.AdapterView.INVALID_POSITION) {
            intent.putExtra(INTENT_LINEBREAK, mLinebreakSpinner.getSelectedItemPosition() - 1);
        }else{
            intent.putExtra(INTENT_LINEBREAK,  - 1);
        }

        String strFilePath;
        if( m_strDirPath.equals("/")) {
            strFilePath = "/" + strFileName;
        } else {
            strFilePath = m_strDirPath + "/" + strFileName;
        }
        intent.putExtra(INTENT_FILEPATH, strFilePath );
        setResult(RESULT_OK, intent);
        finish();
	}

    private void returnDirectory(){
        Intent intent = new Intent();

        intent.putExtra(INTENT_DIRPATH, m_strDirPath );
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		String strItem = (String)getListAdapter().getItem(position);

		if( strItem.equals("..") ) {
			// up to parent direcotry
			if( m_strDirPath.lastIndexOf("/") <= 0 ) {
				// root
				m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/") + 1 );
			} else {
				// sub directory
				m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/"));
			}
			fillList();
		} else if( strItem.substring(strItem.length() - 1 ).equals("/") ) {
			// into directory
			if( m_strDirPath.equals("/") ) {
				// root
				m_strDirPath += strItem;
			} else {
				// sub directory
				m_strDirPath = m_strDirPath + "/" + strItem;
			}
			m_strDirPath = m_strDirPath.substring(0, m_strDirPath.length() - 1 );
			fillList();
		} else {
            // file
		    if ( MODE_OPEN.equals(mMode) ||
		         MODE_FONT.equals(mMode) ||
		         MODE_PICTURE.equals(mMode)) {
    		    Intent intent = getIntent();
                String strFilePath;
                if( m_strDirPath.equals("/")) {
                    strFilePath = "/" + strItem;
                } else {
                    strFilePath = m_strDirPath + "/" + strItem;
                }
                intent.putExtra(INTENT_FILEPATH, strFilePath );
                if (mCharsetSpinnerOpen.getSelectedItemPosition() != 0
                        && mCharsetSpinnerOpen.getSelectedItemPosition() != android.widget.AdapterView.INVALID_POSITION) {
                    intent.putExtra(INTENT_CHARSET, (String)mCharsetSpinnerOpen.getSelectedItem());
                }else{
                    intent.putExtra(INTENT_CHARSET, "");
                }
                setResult(RESULT_OK, intent);
                finish();
		    }else if ( MODE_SAVE.equals(mMode)){
    			mEdtFileName.setText(strItem);
    			mEdtFileName.requestFocus();
		    }else if ( MODE_DIR.equals(mMode)){
		        // nop
		    }
		}

	}

	private void fillList()
	{
		File[] files = new File(m_strDirPath).listFiles();
		if( files == null ) {
			Toast.makeText(FileSelectorActivity.this, getString(R.string.fs_access_denied),
				       Toast.LENGTH_SHORT).show();

			// up to parent direcotry
            if( m_strDirPath.lastIndexOf("/") <= 0 ) {
                // root
                m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/") + 1 );
            } else {
                // sub directory
                m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/"));
            }
			fillList();

			return;
		}

		mTxtFilePath.setText(m_strDirPath);


		if( items != null ) {
			items.clear();
		}
		items = new ArrayList<String>();

		if( !m_strDirPath.equals("/") ) {
			items.add("..");
		}

		for( File file : files ) {
			if( file.isDirectory() ) {
				items.add(file.getName() + "/" );
			} else {
                if (mExtension != null) {
                    String name = file.getName();
                    String smallname = name.toLowerCase();
                    for (String ext : mExtension) {
                        if (smallname.endsWith(ext)) {
                            items.add(name);
                            break;
                        }
                    }
                } else {
                    items.add(file.getName());
                }
			}
		}

		Collections.sort( items , new Comparator<String>(){
            public int compare(String object1, String object2) {

                if ( "..".equals(object1) ){
                    return -1;
                }
                if ( "..".equals(object2) ){
                    return 1;
                }

                boolean p1 = object1.endsWith("/");
                boolean p2 = object2.endsWith("/");

                if ( p1 && !p2 ){
                    return -1;
                }
                if ( !p1 && p2 ){
                    return 1;
                }
                return object1.compareToIgnoreCase(object2);
            }
		});
        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.file_row, items);
		setListAdapter(fileList);
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // BACK key short press => move to parent directory. if root finish the activity.
        // BACK key long  press => finish the activity.
        if ( keyCode == KeyEvent.KEYCODE_BACK ){
            if (event.getRepeatCount() == 0 && !m_strDirPath.equals("/") ) {
                if( m_strDirPath.lastIndexOf("/") <= 0 ) {
                    m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/") + 1 );
                } else {
                    m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/"));
                }
                fillList();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectItemOfSpinner( Spinner spinner , String s )
    {
        SpinnerAdapter adapter = spinner.getAdapter();
        int max = adapter.getCount();
        for( int i=0;i<max;i++ ){
            String item = (String)adapter.getItem(i);
            if ( s.equals(item) ){
                spinner.setSelection(i);
                break;
            }
        }
        return;
    }


}
