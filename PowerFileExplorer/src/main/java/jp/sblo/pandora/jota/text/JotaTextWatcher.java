package jp.sblo.pandora.jota.text;

import android.text.Editable;
import android.text.TextWatcher;

public class JotaTextWatcher implements TextWatcher {

    private boolean mChanged=false;
    private JotaDocumentWatcher mDocumentWatcher;

    public JotaTextWatcher( JotaDocumentWatcher documentwatcher )
    {
        mDocumentWatcher = documentwatcher;
    }

//    @Override
    public void afterTextChanged(Editable s)
    {
        setChanged( true );
    }

//    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

//    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void setChanged( boolean changed )
    {
        boolean shouldCallback = ( mChanged != changed );
        mChanged = changed;
        if ( shouldCallback ){
            mDocumentWatcher.onChanged( );
        }
    }

    public boolean isChanged()
    {
        return mChanged;
    }

}
