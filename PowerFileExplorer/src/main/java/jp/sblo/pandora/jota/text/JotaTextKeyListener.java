package jp.sblo.pandora.jota.text;

import jp.sblo.pandora.jota.JotaTextEditor;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;

public class JotaTextKeyListener extends TextKeyListener {
    private static TextKeyListener[] sInstance =
        new TextKeyListener[Capitalize.values().length * 2];

    private static boolean sAutoIndent=false;
    private static int KEYCODE_FORWARD_DEL  = 112;      // honeycomb's keycode

    public JotaTextKeyListener(Capitalize cap, boolean autotext) {
        super(cap, autotext);
    }

    public static TextKeyListener getInstance(boolean autotext, Capitalize cap) {
        int off = cap.ordinal() * 2 + (autotext ? 1 : 0);

        if (sInstance[off] == null) {
            sInstance[off] = new JotaTextKeyListener(cap, autotext);
        }

        return sInstance[off];
    }

    @Override
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(view, content, keyCode, event);

        // auto indent
        if ( sAutoIndent && keyCode == KeyEvent.KEYCODE_ENTER ){

            int a = Selection.getSelectionStart(content);
            int b = Selection.getSelectionEnd(content);
            if ( a == b ){

                // search head of previous line
                int prev = a-2;
                while( prev >=0 && content.charAt(prev)!='\n' ){
                    prev--;
                }
                prev ++;
                int pos = prev;
                while(  content.charAt(pos)==' ' || content.charAt(pos)=='\t' || content.charAt(pos)=='\u3000'){
                    pos++;
                }
                int len = pos-prev;
                if ( len > 0  ){
                    char [] dest = new char[len];
                    content.getChars(prev, pos, dest, 0);

                    content.replace(a,b, new String(dest) );
                    Selection.setSelection(content, a+len);
                }
            }
        }
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ){
            if (keyCode == KEYCODE_FORWARD_DEL ) {
                if ( (event.getMetaState() & 512) == 0 ){       // workaround for Galaxy Note
                    forwardDelete(view, content, keyCode, event);
                    return true;
                }
            }
        }
        return result;
    }
    static public void setAutoIndent( boolean autoIndent )
    {
        sAutoIndent = autoIndent;
    }
    static public void setForwardDelKeycode( int keycode )
    {
        KEYCODE_FORWARD_DEL = keycode;
    }
    static public int  getForwardDelKeycode(  )
    {
        return KEYCODE_FORWARD_DEL ;
    }

    /**
     * Performs the action that happens when you press the DEL key in
     * a TextView.  If there is a selection, deletes the selection;
     * otherwise, DEL alone deletes the character before the cursor,
     * if any;
     * ALT+DEL deletes everything on the line the cursor is on.
     *
     * @return true if anything was deleted; false otherwise.
     */
    public boolean forwardDelete(View view, Editable content, int keyCode,
                             KeyEvent event) {
        int selStart, selEnd;
        boolean result = true;

        {
            int a = Selection.getSelectionStart(content);
            int b = Selection.getSelectionEnd(content);

            selStart = Math.min(a, b);
            selEnd = Math.max(a, b);
        }

        if (selStart != selEnd) {
            content.delete(selStart, selEnd);
        } else {
            int to = TextUtils.getOffsetAfter(content, selEnd);

            if (to != selEnd) {
                content.delete(Math.min(to, selEnd), Math.max(to, selEnd));
            }
            else {
                result = false;
            }
        }

        if (result)
            adjustMetaAfterKeypress(content);

        return result;
    }

    final private static int META_SELECTING = JotaTextEditor.sHoneycomb?MetaKeyKeyListener.META_SELECTING:(1 << 16);

    public static final int getMetaStateSelecting(CharSequence text) {
        return MetaKeyKeyListener.getMetaState(text, META_SELECTING);
    }

}
