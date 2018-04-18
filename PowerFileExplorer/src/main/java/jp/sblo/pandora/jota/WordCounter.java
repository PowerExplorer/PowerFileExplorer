package jp.sblo.pandora.jota;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sblo.pandora.jota.Search.Record;
import jp.sblo.pandora.jota.text.TextView;
import android.text.Editable;

public class WordCounter {

    public static class Result
    {
        public int words;
        public int charactrers;
        public int lines;
        public int logicallines;
    }

    public static int countPatterns( String pat , CharSequence text )
    {
        Pattern pattern = Pattern.compile( pat );
        Matcher m = pattern.matcher( text );
        int i=0;
        while ( m.find() ){
            Record record = new Record();
            record.start = m.start();
            record.end = m.end();
            i++;
        }
        return i;

    }


    public static Result count( TextView textview )
    {
        Result result = new Result();

        Editable text = (Editable)textview.getText();
        result.charactrers = text.length();
        result.lines = textview.getLineCount();
        result.logicallines = countPatterns( "\n" , text ) + 1;
        result.words = countPatterns( "\\w+" , text);
        return result;
    }


}
