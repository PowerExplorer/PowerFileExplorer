package jp.sblo.pandora.jota;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import net.gnu.explorer.R;
import android.util.Log;

public class SettingsToolbarActivity extends Activity  {

    private ListView mListView;
    private ArrayList<Integer> mData;
    private ToolAdapter mAdapter;
    private ImageButton mAddButton;
    private CheckBox mUseToolbar;
    private CheckBox mToolbarBigButton;
    private CheckBox mHideInLandscape;

    private String[] mSummarys;
    private int[] mFunctions;
    private String[] mToolNames;
    private String[] mToolNamesTrimed;
    private int mFunctionLen;
    private SharedPreferences mSp;

    final private static int TOOL_MAX = 50;
    final private static String KEY_TOOLBAR = "TOOLBAR_%03d";

    final private static int[] DEFAULT_TOOLBAR = {
        jp.sblo.pandora.jota.text.TextView.FUNCTION_SAVE,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_COPY,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_CUT,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_PASTE,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_UNDO,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_REDO,
        jp.sblo.pandora.jota.text.TextView.FUNCTION_SEARCH,
		jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_ALL,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSp = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.settings_toolbar);

        mFunctionLen = SettingsShortcutActivity.TBL_TOOLNAME.length - 1;
        mSummarys = new String[mFunctionLen];
        mFunctions = new int[mFunctionLen];
        mToolNames = new String[mFunctionLen];
        mToolNamesTrimed = new String[mFunctionLen];

        for (int i = 0;i < mFunctionLen;i++) {
            mFunctions[i] = SettingsShortcutActivity.TBL_FUNCTION[i + 1];
            mToolNames[i] = SettingsShortcutActivity.getToolbarLabel(this, i + 1);
            mToolNamesTrimed[i] = SettingsShortcutActivity.getToolbarLabel(this, i + 1).trim();
            mSummarys[i] = getString(SettingsShortcutActivity.TBL_SUMMARY[i + 1]);
        }

        setTitle(R.string.menu_pref_toolbar);
        mListView = (ListView)findViewById(R.id.list);
        mData = readToolbarSettings(this);
        mAdapter = new ToolAdapter(getApplicationContext(), R.layout.settings_toolbar_row, R.id.list, mData);
        mListView.setAdapter(mAdapter);

        mAddButton = (ImageButton)findViewById(R.id.addtoolbar);
        mAddButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(SettingsToolbarActivity.this)
						.setTitle(R.string.label_toolbar_select)
						.setItems(mSummarys , new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								mData.add(Integer.valueOf(mFunctions[which]));
								mAdapter.notifyDataSetChanged();
								writeToolbarSettings();
								checkAddButton();
							}
						})
						.show();

				}
			});
        checkAddButton();

        mUseToolbar = (CheckBox)findViewById(R.id.usetoolbar);
        boolean usetoolbar = mSp.getBoolean(SettingsActivity.KEY_SHOW_TOOLBAR, true);
        mUseToolbar.setChecked(usetoolbar);
        mUseToolbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mSp.edit().putBoolean(SettingsActivity.KEY_SHOW_TOOLBAR, isChecked).commit();
				}
			});

        mToolbarBigButton = (CheckBox)findViewById(R.id.bigbutton);
        boolean toolbarBigButton = mSp.getBoolean(SettingsActivity.KEY_TOOLBAR_BIGBUTTON, false);
        mToolbarBigButton.setChecked(toolbarBigButton);
        mToolbarBigButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mSp.edit().putBoolean(SettingsActivity.KEY_TOOLBAR_BIGBUTTON, isChecked).commit();
				}
			});

        mHideInLandscape = (CheckBox)findViewById(R.id.hidelandscape);
        boolean hideLnadscape = mSp.getBoolean(SettingsActivity.KEY_TOOLBAR_HIDE_LANDSCAPE, false);
        mHideInLandscape.setChecked(hideLnadscape);
        mHideInLandscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mSp.edit().putBoolean(SettingsActivity.KEY_TOOLBAR_HIDE_LANDSCAPE, isChecked).commit();
				}
			});

    }

    private void checkAddButton() {
        mAddButton.setEnabled(mData.size() < TOOL_MAX);
    }

    private void writeToolbarSettings() {
        Editor editor = mSp.edit();

        for (int i=0;i < TOOL_MAX;i++) {
            String key = String.format(KEY_TOOLBAR, i);
            editor.remove(key);
        }
        final int size = mData.size();
		for (int i=0;i < size;i++) {
            String key = String.format(KEY_TOOLBAR, i);
            editor.putInt(key, mData.get(i).intValue());
        }
        editor.commit();
    }

    static public void writeDefaultToolbarSettings(Context context) {
		Log.d("SettingsToolbarActivity", "writeDefaultToolbarSettings");
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key0 = String.format(KEY_TOOLBAR, 0);

        if (sp.getInt(key0, jp.sblo.pandora.jota.text.TextView.FUNCTION_NONE) == jp.sblo.pandora.jota.text.TextView.FUNCTION_NONE) {
            Editor editor = sp.edit();

            for (int i=0;i < TOOL_MAX;i++) {
                String key = String.format(KEY_TOOLBAR, i);
                editor.remove(key);
            }
            for (int i=0;i < DEFAULT_TOOLBAR.length ;i++) {
                String key = String.format(KEY_TOOLBAR, i);
                editor.putInt(key, DEFAULT_TOOLBAR[i]);
            }
            editor.commit();
        }
    }

    static public ArrayList<Integer> readToolbarSettings(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i=0;i < TOOL_MAX;i++) {
            String key = String.format(KEY_TOOLBAR, i);
            int f = sp.getInt(key, jp.sblo.pandora.jota.text.TextView.FUNCTION_NONE);
            if (f != jp.sblo.pandora.jota.text.TextView.FUNCTION_NONE) {
				//if (context instanceof Main) {
				result.add(Integer.valueOf(f));
				//} else if (f != jp.sblo.pandora.jota.text.TextView.FUNCTION_QUIT) {
				//result.add(f);
				//}
            } else {
                break;
            }
        }
        return result;
    }

    class ToolAdapter extends ArrayAdapter<Integer> {

        class ViewHolder {
            TextView title;
            TextView summary;
            ImageButton up;
            ImageButton down;
            ImageButton remove;
        }

        public ToolAdapter(Context context, int resource, int textViewResourceId, ArrayList<Integer> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View view;
            ViewHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();

            } else {
                view = View.inflate(getContext() , R.layout.settings_toolbar_row , null);

                holder = new ViewHolder();
                holder.title = (TextView)view.findViewById(R.id.title);
                holder.summary = (TextView)view.findViewById(R.id.summary);
                holder.up = (ImageButton)view.findViewById(R.id.up);
                holder.down = (ImageButton)view.findViewById(R.id.down);
                holder.remove = (ImageButton)view.findViewById(R.id.remove);

                view.setTag(holder);
            }
            int function = getItem(position);

            setFunctionName(holder, function);

            holder.up.setVisibility(position == 0 ?View.INVISIBLE: View.VISIBLE);
            holder.down.setVisibility(position == getCount() - 1 ?View.INVISIBLE: View.VISIBLE);

            holder.up.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer function = mData.get(position);
						mData.remove(position);
						mData.add(position - 1, function);
						notifyDataSetChanged();
						writeToolbarSettings();

					}
				});
            holder.down.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer function = mData.get(position);
						mData.remove(position);
						mData.add(position + 1, function);
						notifyDataSetChanged();
						writeToolbarSettings();

					}
				});
            holder.remove.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer function = mData.get(position);
						mData.remove(position);
						notifyDataSetChanged();
						checkAddButton();
						writeToolbarSettings();

					}
				});

            return view;
        }
        private void setFunctionName(ViewHolder holder, int function) {
            for (int i=0;i < mFunctionLen;i++) {
                if (mFunctions[i] == function) {
                    String title = mToolNamesTrimed[i];
                    holder.title.setText(title);
                    String summary = mSummarys[i];
                    if (summary.equalsIgnoreCase(title)) {
                        summary = "";
                    }
                    holder.summary.setText(summary);
                    break;
                }
            }
        }
    }

}
