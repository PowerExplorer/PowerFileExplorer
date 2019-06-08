package jp.sblo.pandora.jota;

import java.util.HashMap;

import jp.sblo.pandora.jota.text.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import net.gnu.explorer.R;

public class SettingsShortcutActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String KEY_SHORTCUT                    = "SHORTCUT_ASSIGN";

    static class DefineShortcut {
        int key;
        String name;
        int function;

        public DefineShortcut( int k, String n , int f){
            key = k;
            name = n;
            function = f;
        }
    };


    public final static int[] TBL_SUMMARY = {
        R.string.no_assign,
        R.string.selectAll,
        R.string.menu_edit_undo,
        R.string.copy,
        R.string.cut,
        R.string.paste,
        R.string.menu_direct,
        R.string.menu_file_save,
        R.string.enter,
        R.string.tab,
        R.string.backspace,
        R.string.trackball_centering,
        R.string.label_search,
        R.string.label_open_file,
        R.string.label_new_file,
        R.string.label_redo,
        R.string.trackball_contextmenu,
        R.string.menu_edit_jump,
        R.string.label_forward_del,
        R.string.label_cursor_left,
        R.string.label_cursor_right,
        R.string.label_cursor_up,
        R.string.label_cursor_down,
        R.string.label_page_up,
        R.string.label_page_down,
        R.string.label_home,
        R.string.label_end,
        R.string.label_top,
        R.string.label_bottom,
        R.string.menu_file_property,
        R.string.menu_file_history,
        R.string.menu_file_view,
        R.string.menu_share,
        R.string.menu_file_share,
        R.string.menu_insert,
        R.string.menu_file_quit,
        R.string.menu_search_byintent,
        R.string.label_word_wrap,
        R.string.show_ime,
        R.string.menu_font_size_up,
        R.string.menu_font_size_down,
        R.string.menu_select_line,
        R.string.menu_select_block,
        R.string.menu_parenthesis,
        R.string.menu_curly,
        R.string.menu_brackets,
        R.string.menu_xmlbrace,
        R.string.menu_ccomment,
        R.string.menu_doublequote,
        R.string.menu_singlequote,
        R.string.single_quote,
        R.string.double_quote,
        R.string.menu_select,
        R.string.menu_select_word,
        R.string.label_launch_by_sl4a,
        R.string.menu_menu,
        R.string.label_kill_line,
        R.string.menu_file_saveas,
    };

    public final static int[] TBL_FUNCTION = {
        TextView.FUNCTION_NONE,
        TextView.FUNCTION_SELECT_ALL,
        TextView.FUNCTION_UNDO,
        TextView.FUNCTION_COPY,
        TextView.FUNCTION_CUT,
        TextView.FUNCTION_PASTE,
        TextView.FUNCTION_DIRECTINTENT,
        TextView.FUNCTION_SAVE,
        TextView.FUNCTION_ENTER,
        TextView.FUNCTION_TAB,
        TextView.FUNCTION_DEL,
        TextView.FUNCTION_CENTERING,
        TextView.FUNCTION_SEARCH,
        TextView.FUNCTION_OPEN,
        TextView.FUNCTION_NEWFILE,
        TextView.FUNCTION_REDO,
        TextView.FUNCTION_CONTEXTMENU,
        TextView.FUNCTION_JUMP,
        TextView.FUNCTION_FORWARD_DEL,
        TextView.FUNCTION_CURSOR_LEFT,
        TextView.FUNCTION_CURSOR_RIGHT,
        TextView.FUNCTION_CURSOR_UP,
        TextView.FUNCTION_CURSOR_DOWN,
        TextView.FUNCTION_PAGE_UP,
        TextView.FUNCTION_PAGE_DOWN,
        TextView.FUNCTION_HOME,
        TextView.FUNCTION_END,
        TextView.FUNCTION_TOP,
        TextView.FUNCTION_BOTTOM,
        TextView.FUNCTION_PROPERTY,
        TextView.FUNCTION_HISTORY,
        TextView.FUNCTION_OPENAPP,
        TextView.FUNCTION_SHARE,
        TextView.FUNCTION_SHAREFILE,
        TextView.FUNCTION_INSERT,
        TextView.FUNCTION_QUIT,
        TextView.FUNCTION_SEARCHAPP,
        TextView.FUNCTION_WORDWRAP,
        TextView.FUNCTION_SHOWIME,
        TextView.FUNCTION_FONTUP,
        TextView.FUNCTION_FONTDOWN,
        TextView.FUNCTION_SELECT_LINE,
        TextView.FUNCTION_SELECT_BLOCK,
        TextView.FUNCTION_PARENTHESIS,
        TextView.FUNCTION_CURLY,
        TextView.FUNCTION_BRACKETS,
        TextView.FUNCTION_XMLBRACE,
        TextView.FUNCTION_CCOMMENT,
        TextView.FUNCTION_DOUBLEQUOTE,
        TextView.FUNCTION_SINGLEQUOTE,
        TextView.FUNCTION_KAGIKAKKO,
        TextView.FUNCTION_NIJUKAGI,
        TextView.FUNCTION_SELECT,
        TextView.FUNCTION_SELECT_WORD,
        TextView.FUNCTION_LAUNCH_BY_SL4A,
        TextView.FUNCTION_MENU,
        TextView.FUNCTION_KILLLINE,
        TextView.FUNCTION_SAVEAS,
    };

    public final static String[] TBL_TOOLNAME = {
        "none",
        "Select All",
        "Undo",
        "Copy",
        " Cut ",
        "Paste",
        "Direct Intent",
        "Save",
        "Enter",
        "Tab",
        " BS ",
        "Centering",
        "Search",
        "Open",
        " New ",
        "Redo",
        "Menu",
        "Jump",
        " Del ",
        "Left",
        "Right",
        " Up ",
        "Down",
        "Pgup",
        "PgDn",
        "Home",
        "End",
        "Top",
        "Bottom",
        "Property",
        "History",
        "Open App",
        "Share",
        "Share File",
        "Insert",
        "Quit",
        "Search App",
        "Word Wrap",
        "Show IME",
        "Font+",
        "Font-",
        "Sel Line",
        "Sel Block",
        " ( ) ",
        " { } ",
        " [ ] ",
        " < /> ",
        " /* */ ",
        " \" \" ",
        " ' ' ",
        " \u300c \u300d ",
        " \u300e \u300f ",
        "Select",
        "Sel Word",
        "SL4A",
        "Menu",
        "Kill-Line",
        "Save As",
    };

    public String getFunctionName(int func)
    {
        if ( 0<= func && func < TBL_SUMMARY.length ){
            return getString( TBL_SUMMARY[func] );
        }
        return "";
    }

    private static final DefineShortcut[] TBL_SHORTCUT = new DefineShortcut[]{
        new DefineShortcut( KeyEvent.KEYCODE_A ,"A" , TextView.FUNCTION_SELECT_ALL ),
        new DefineShortcut( KeyEvent.KEYCODE_B ,"B" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_C ,"C" , TextView.FUNCTION_COPY ),
        new DefineShortcut( KeyEvent.KEYCODE_D ,"D" , TextView.FUNCTION_DIRECTINTENT ),
        new DefineShortcut( KeyEvent.KEYCODE_E ,"E" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_F ,"F" , TextView.FUNCTION_SEARCH ),
        new DefineShortcut( KeyEvent.KEYCODE_G ,"G" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_H ,"H" , TextView.FUNCTION_DEL ),
        new DefineShortcut( KeyEvent.KEYCODE_I ,"I" , TextView.FUNCTION_TAB ),
        new DefineShortcut( KeyEvent.KEYCODE_J ,"J" , TextView.FUNCTION_JUMP ),
        new DefineShortcut( KeyEvent.KEYCODE_K ,"K" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_L ,"L" , TextView.FUNCTION_CENTERING ),
        new DefineShortcut( KeyEvent.KEYCODE_M ,"M" , TextView.FUNCTION_ENTER ),
        new DefineShortcut( KeyEvent.KEYCODE_N ,"N" , TextView.FUNCTION_NEWFILE ),
        new DefineShortcut( KeyEvent.KEYCODE_O ,"O" , TextView.FUNCTION_OPEN ),
        new DefineShortcut( KeyEvent.KEYCODE_P ,"P" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_Q ,"Q" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_R ,"R" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_S ,"S" , TextView.FUNCTION_SAVE ),
        new DefineShortcut( KeyEvent.KEYCODE_T ,"T" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_U ,"U" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_V ,"V" , TextView.FUNCTION_PASTE ),
        new DefineShortcut( KeyEvent.KEYCODE_W ,"W" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_X ,"X" , TextView.FUNCTION_CUT ),
        new DefineShortcut( KeyEvent.KEYCODE_Y ,"Y" , TextView.FUNCTION_REDO ),
        new DefineShortcut( KeyEvent.KEYCODE_Z ,"Z" , TextView.FUNCTION_UNDO ),
        new DefineShortcut( KeyEvent.KEYCODE_1 ,"1" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_2 ,"2" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_3 ,"3" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_4 ,"4" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_5 ,"5" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_6 ,"6" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_7 ,"7" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_8 ,"8" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_9 ,"9" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_0 ,"0" , TextView.FUNCTION_NONE ),
        new DefineShortcut( KeyEvent.KEYCODE_DEL ,"Del" , TextView.FUNCTION_FORWARD_DEL ),
    };

    private PreferenceScreen mPs = null;
    private PreferenceManager mPm = getPreferenceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPm = getPreferenceManager();

        mPs = mPm.createPreferenceScreen(this);
        setTitle(R.string.label_customize_shortcut);

        int len = TBL_SUMMARY.length;
        String[] tbl_summary =  new String[ len ];
        String[] tbl_function =  new String[ len ];
        for( int i=0;i<len;i++ ){
            tbl_summary[i] = getString(TBL_SUMMARY[i]);
            tbl_function[i] = Integer.toString(TBL_FUNCTION[i]);
        }

        for( DefineShortcut sd : TBL_SHORTCUT )
        {
            final ListPreference pr = new ListPreference(this);
            pr.setKey(KEY_SHORTCUT + sd.key );
            pr.setTitle( sd.name );
            pr.setEntries(tbl_summary);
            pr.setEntryValues(tbl_function);
            mPs.addPreference(pr);
        }
        setPreferenceScreen(mPs);
        setSummary();
    }

    static public HashMap<Integer,Integer> loadShortcuts(Context context)
    {
        HashMap<Integer,Integer> result = new HashMap<Integer,Integer>();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        for( DefineShortcut sd : TBL_SHORTCUT )
        {
            String key = KEY_SHORTCUT + sd.key;
            String strfunction = sp.getString(key, "0" );
            int function = Integer.parseInt(strfunction);
            result.put(Integer.valueOf(sd.key), Integer.valueOf(function));
        }
        return result;
    }

    static public void writeDefaultShortcuts(Context context)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        for( DefineShortcut sd : TBL_SHORTCUT )
        {
            String key = KEY_SHORTCUT + sd.key;
            if ( !sp.contains(key) ){
                editor.putString(key, Integer.toString(sd.function) );
            }
        }
        editor.commit();
    }

    private void setSummary()
    {
        int prlen = mPs.getPreferenceCount();
        for( int i=0;i<prlen ;i++ ){
            Preference pr = mPs.getPreference(i);
            if ( pr instanceof ListPreference ){
                ListPreference lpr = (ListPreference)pr;
                lpr.setSummary(lpr.getEntry());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPs.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPs.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public static String getToolbarLabel(Context context,int i)
    {
        if ( i == TextView.FUNCTION_KAGIKAKKO ){
            return context.getResources().getString(R.string.single_quote);
        }else if ( i == TextView.FUNCTION_NIJUKAGI ){
            return context.getResources().getString(R.string.double_quote);
        }else{
            return SettingsShortcutActivity.TBL_TOOLNAME[i];
        }
    }
}
