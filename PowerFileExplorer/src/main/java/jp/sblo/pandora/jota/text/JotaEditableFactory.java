package jp.sblo.pandora.jota.text;

import android.text.Editable;

public class JotaEditableFactory extends Editable.Factory
{
    private static Editable.Factory sInstance = new JotaEditableFactory();

    /**
     * Returns the standard Editable Factory.
     */
    public static Editable.Factory getInstance() {
        return sInstance;
    }

    /**
     * Returns a new SpannedStringBuilder from the specified
     * CharSequence.  You can override this to provide
     * a different kind of Spanned.
     */
    public Editable newEditable(CharSequence source) {
        if ( source instanceof SpannableStringBuilder ){
            return (Editable)source;
        }
        return new SpannableStringBuilder(source);
    }

}
