package jp.sblo.pandora.jota.text;

import android.emoji.EmojiFactory;
import android.graphics.Bitmap;

import jp.sblo.pandora.jota.JotaTextEditor;


public class EmojiFactoryWrapper {

    Object emojiFactoryInstance = null;

    public EmojiFactoryWrapper(){
        // TODO:NPreviewç¨æ«å®ã³ã¼ã
        // NPreviewä»¥éãªããEmojiFactoryãä½ããªã
        if (!JotaTextEditor.sNorLater){
            emojiFactoryInstance = EmojiFactory.newAvailableInstance();
        }
    }

    public Bitmap getBitmapFromAndroidPua(int code){
        if ( emojiFactoryInstance  != null ){
            EmojiFactory ef = (EmojiFactory)emojiFactoryInstance;
            return ef.getBitmapFromAndroidPua(code);
        }else{
            return null;
        }
    }

    public int getMinimumAndroidPua(){
        if ( emojiFactoryInstance  != null ){
            EmojiFactory ef = (EmojiFactory)emojiFactoryInstance;
            return ef.getMinimumAndroidPua();
        }else{
            return -1;
        }
    }

    public int getMaximumAndroidPua(){
        if ( emojiFactoryInstance  != null ){
            EmojiFactory ef = (EmojiFactory)emojiFactoryInstance;
            return ef.getMaximumAndroidPua();
        }else{
            return -1;
        }
    }

}
