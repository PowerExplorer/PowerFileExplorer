package jp.sblo.pandora.jota.text;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;

import jp.sblo.pandora.jota.IS01FullScreen;
import jp.sblo.pandora.jota.JotaTextEditor;
import net.gnu.explorer.R;
import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class EditText extends TextView{



    private JotaTextWatcher mTextWatcher;
    private WeakReference<ShortcutListener> mShortcutListener;
    private int mShortcutCtrlKey = 0;
    private int mShortcutAltKey = 0;
    private HashMap<Integer,Integer> mShortcuts;
    private int mDpadCenterFunction = FUNCTION_CENTERING;
    private boolean mCtrlPreIme = false;

    private static Boolean sIWnnFlag = null;

    public EditText(Context context) {
        this(context, null);
        init(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
        init(context);
    }

    public EditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        setFocusable(true);
        setFocusableInTouchMode(true);

        setFastScrollEnabled(true);

        // change width of the caret
        setCaretThick( context.getResources().getDimension(R.dimen.caret_thick) );

        // set my Editable
        setEditableFactory( JotaEditableFactory.getInstance() );

        // set IME options
        if ( JotaTextEditor.sHoneycomb ){
            setImeOptions(EditorInfo.IME_ACTION_DONE|EditorInfo.IME_FLAG_NO_FULLSCREEN|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        }else{
            setImeOptions(EditorInfo.IME_ACTION_DONE|0x80000000|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        }
    }


    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
     */
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    /**
     * Convenience for {@link Selection#selectAll}.
     */
    public void selectAll() {
        Selection.selectAll(getText());
    }

    /**
     * Convenience for {@link Selection#extendSelection}.
     */
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.e( "keycode=","keycode="+keyCode );

        int keycode = event.getKeyCode();

        if ( event.getAction() == KeyEvent.ACTION_DOWN ){
            switch(keycode){
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    return doFunction(mDpadCenterFunction);
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        int keycode = event.getKeyCode();
        // ALT + KEYDOWN
        int meta = (int)event.getMetaState();
        boolean alt = (meta & mShortcutAltKey)!=0 ; // || (altstate!=0);      // one of meta keies is pressed , or , Alt key is locked
        boolean ctrl = (meta & mShortcutCtrlKey)!=0 ; // one of meta keies is pressed

        if ( mCtrlPreIme && ctrl ){
            if (event.getAction() == KeyEvent.ACTION_DOWN ){
                if (doShortcut(keycode)){
                    return true;
                }
            }else if (event.getAction() == KeyEvent.ACTION_UP){
                return true;
            }
        }
        if ( alt && event.getAction() == KeyEvent.ACTION_DOWN ){
            if (doShortcut(keycode)){
                if ( sIWnnFlag == null ){
                    // for IS01 w/iWnn
                    // iWnn eats ALT key so we needs to reset ime.
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null){
                        try {
                            Class<?> c = imm.getClass();
                            Field f = c.getDeclaredField("mCurId");
                            f.setAccessible(true);
                            String immId = (String)f.get(imm);
                            if ( "jp.co.omronsoft.iwnnime/.iWnnIME".equals(immId)
                              || "net.gorry.android.input.nicownng/.NicoWnnGJAJP".equals(immId) ){
                                sIWnnFlag = true;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                if ( sIWnnFlag != null && sIWnnFlag ){
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null){
                        imm.restartInput(this);
                    }
                }
                if ( (meta & KeyEvent.META_ALT_ON) != 0  ){
                    TextKeyListener.clearMetaKeyState(getEditableText(),KeyEvent.META_ALT_ON);
                }
                return true;
            }
        }
        if ( alt && event.getAction() == KeyEvent.ACTION_UP ){
            if (isShortcut(keycode)){
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int keycode = event.getKeyCode();
        if ( !mCtrlPreIme ){
            // CTRL + KEYDOWN
            int meta = (int)event.getMetaState();
            boolean ctrl = (meta & mShortcutCtrlKey)!=0 ; // one of meta keies is pressed

            if ( ctrl ){
                if (event.getAction() == KeyEvent.ACTION_DOWN ){
                    Log.d("=================>", ""+keycode);
                    if (doShortcut(keycode)){
                        return true;
                    }
                }else if (event.getAction() == KeyEvent.ACTION_UP){
                    return true;
                }
            }
        }
        if ( IS01FullScreen.isIS01orLynx() ){
            if ( keycode == KeyEvent.KEYCODE_PAGE_UP ||
                 keycode == KeyEvent.KEYCODE_PAGE_DOWN ){
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean doFunction( int function ){
        boolean result = doFunction_( function );
        return result;
    }


    public boolean doFunction_( int function ){
        ShortcutListener sl = mShortcutListener.get();

        switch ( function) {
            case FUNCTION_SELECT_ALL:
            case FUNCTION_CUT:
            case FUNCTION_COPY:
            case FUNCTION_UNDO:
            case FUNCTION_REDO:
            case FUNCTION_PASTE:
            case FUNCTION_WORDWRAP:
            case FUNCTION_SHOWIME:
            case FUNCTION_CURSOR_LEFT:
            case FUNCTION_CURSOR_RIGHT:
            case FUNCTION_CURSOR_UP:
            case FUNCTION_CURSOR_DOWN:
            case FUNCTION_PAGE_UP:
            case FUNCTION_PAGE_DOWN:
            case FUNCTION_HOME:
            case FUNCTION_END:
            case FUNCTION_TOP:
            case FUNCTION_BOTTOM:
            case FUNCTION_PARENTHESIS:
            case FUNCTION_CURLY:
            case FUNCTION_BRACKETS:
            case FUNCTION_XMLBRACE:
            case FUNCTION_CCOMMENT:
            case FUNCTION_DOUBLEQUOTE:
            case FUNCTION_SINGLEQUOTE:
            case FUNCTION_KAGIKAKKO:
            case FUNCTION_NIJUKAGI:
            case FUNCTION_SELECT:
            case FUNCTION_SELECT_WORD:
            case FUNCTION_KILLLINE:
                return doCommand(function);

            case FUNCTION_ENTER:
                return onKeyDown(KeyEvent.KEYCODE_ENTER ,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
            case FUNCTION_TAB:
                return onKeyDown(KeyEvent.KEYCODE_TAB ,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_TAB));
            case FUNCTION_DEL:
                return onKeyDown(KeyEvent.KEYCODE_DEL ,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));
            case FUNCTION_FORWARD_DEL:
            {
                int key = JotaTextKeyListener.getForwardDelKeycode();
                return onKeyDown( key ,new KeyEvent(KeyEvent.ACTION_DOWN,key));
            }

            case FUNCTION_CENTERING:
                return centerCursor();

            case FUNCTION_SAVE:
            case FUNCTION_SAVEAS:
            case FUNCTION_DIRECTINTENT:
            case FUNCTION_OPEN:
            case FUNCTION_NEWFILE:
            case FUNCTION_SEARCH:
            case FUNCTION_JUMP:
            case FUNCTION_PROPERTY:
            case FUNCTION_HISTORY:
            case FUNCTION_OPENAPP:
            case FUNCTION_SHARE:
            case FUNCTION_SHAREFILE:
            case FUNCTION_INSERT:
            case FUNCTION_QUIT:
            case FUNCTION_SEARCHAPP:
            case FUNCTION_FONTUP:
            case FUNCTION_FONTDOWN:
            case FUNCTION_SELECT_BLOCK:
            case FUNCTION_SELECT_LINE:
            case FUNCTION_LAUNCH_BY_SL4A:
            case FUNCTION_MENU:
                if (sl != null) {
                    return sl.onCommand(function);
                }
                break;

            case FUNCTION_CONTEXTMENU:
                showContextMenu();
                return true;
            case FUNCTION_NONE:
                return false;
        }
        return false;
    }


    public boolean doShortcut(int keycode) {

        Integer ss = mShortcuts.get(keycode);

        if (ss!=null && ss.intValue() != EditText.FUNCTION_NONE ) {
            return doFunction( ss.intValue() );
        }
        return false;
    }

    public boolean isShortcut(int keycode) {

        Integer ss = mShortcuts.get(keycode);

        if (ss!=null && ss.intValue() != EditText.FUNCTION_NONE ) {
            return true;
        }
        return false;
    }

    public void setDocumentChangedListener( JotaDocumentWatcher watcher )
    {
        mTextWatcher = new JotaTextWatcher( watcher );
        // set text watcher
        addTextChangedListener(mTextWatcher);
    }
    public boolean isChanged()
    {
        if ( mTextWatcher != null ){
            return mTextWatcher.isChanged();
        }else{
            return false;
        }
    }
    public void setChanged( boolean changed ){
        if ( mTextWatcher != null ){
            mTextWatcher.setChanged( changed );
        }
        super.setChanged( changed );
    }

    public void setShortcutListener( ShortcutListener sl )
    {
        mShortcutListener = new WeakReference<ShortcutListener>(sl);
    }

    public interface ShortcutListener {
        boolean onCommand(int keycode);
    }

    public void setShortcutMetaKey(int altkey,int ctrlkey) {
        mShortcutAltKey = altkey;
        mShortcutCtrlKey = ctrlkey;
        ArrowKeyMovementMethod.setCtrlKey(ctrlkey);
    }

    public void setShortcutSettings( HashMap<Integer,Integer> s )
    {
        mShortcuts = s;
    }

    public void setUseVolumeKey( boolean useVolumeKey )
    {
        ArrowKeyMovementMethod.setUseVolumeKey(useVolumeKey);
    }

    public void setDpadCenterFunction( int function )
    {
        mDpadCenterFunction = function;
    }

    public void setAutoIndent( boolean autoIndent )
    {
        JotaTextKeyListener.setAutoIndent(autoIndent);
    }
    public void setKeycodes( int forwarddel , int home , int end )
    {
        JotaTextKeyListener.setForwardDelKeycode(forwarddel);
        ArrowKeyMovementMethod.setHomeEndKeycode(home, end);
    }

    public void setCtrlPreIme(boolean val)
    {
        mCtrlPreIme = val;
    }
}
