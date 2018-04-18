package jp.sblo.pandora.jota;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.sblo.pandora.jota.text.ArrowKeyMovementMethod;
import jp.sblo.pandora.jota.text.SpannableStringBuilder;
import jp.sblo.pandora.jota.text.style.ForegroundColorSpan;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.Spannable;
import android.text.TextUtils;

public class KeywordHighlght {

    private static final String PATH     = Environment.getExternalStorageDirectory() + "/.jota/keyword/";
    private static final String USERPATH     = Environment.getExternalStorageDirectory() + "/.jota/keyword/user/";
    private static final String EXT      = "conf";
    private static final String ASSET_PATH     = "keyword";
    private static final String COLOR_PATH     = "colorsetting."+EXT;

    public Pattern pattern;
    public int color;

    static ArrayList<KeywordHighlght> sList = new ArrayList<KeywordHighlght>();
    static ArrayList<ForegroundColorSpan> sFcsList = new ArrayList<ForegroundColorSpan>();
    static HashMap<String,Integer> sColorMap = new HashMap<String,Integer>();
    static int mLastStart = 0;
    static int mLastEnd = 0;


    private KeywordHighlght( String regexp , int _color )
    {
        pattern = Pattern.compile(regexp,Pattern.DOTALL);
        color = _color;
    }

    static private void addKeyword( String regexp , int color )
    {
        if ( color != 0 && !TextUtils.isEmpty(regexp) ){
            try{
                sList.add( new KeywordHighlght(regexp , color|0xFF000000) );
            }
            catch( PatternSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    static private void clearKeyword( )
    {
        sList.clear();
    }

    static public boolean needHighlight()
    {
        return sList.size()!=0;
    }

    static public void refresh()
    {
        mLastStart = mLastEnd = -1;
    }

    static public void setHighlight( SpannableStringBuilder buf, int start , int end )
    {
        if ( mLastStart == start && mLastEnd == end ){
            return;
        }
        mLastStart = start;
        mLastEnd = end;

        for( ForegroundColorSpan o: sFcsList )
        {
            buf.removeSpan(o);
            o.recycle();
        }
        sFcsList.clear();

        start = ArrowKeyMovementMethod.findBlockStart(buf, start);
        end = ArrowKeyMovementMethod.findBlockEnd(buf, end);
        if ( end+1 < buf.length() ){
            end++;
        }

        CharSequence target = buf.subSequence(start, end);
        for( KeywordHighlght syn : sList ){
            try{
                Matcher m= syn.pattern.matcher(target);

                while (m.find()) {
                    int matchstart = start+m.start();
                    int matchend = start+m.end();
                    if ( matchstart!=matchend ){
                        boolean found = false;
                        for( ForegroundColorSpan fcs : sFcsList ){
                            if ( fcs.isLapped(matchstart, matchend)){
                                found = true;
                                break;
                            }
                        }
                        if ( !found ){
                            ForegroundColorSpan fgspan = ForegroundColorSpan.obtain(syn.color,matchstart,matchend);
                            buf.setSpan(fgspan, matchstart, matchend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sFcsList.add(fgspan);
                        }
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    static private void loadColorSettings()
    {
        sColorMap.clear();
        String path = USERPATH + COLOR_PATH;

        File f = new File(path);
        if (!f.exists() ){
            path = PATH + COLOR_PATH;
            f = new File(path);
            if (!f.exists() ){
                return;
            }
        }

        // parse ini file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while( (line = br.readLine()) != null ){
                line = line.replaceAll( "^#.*$" , "" );
                line = line.replaceAll( "^//.*$" , "" );
                line = line.replaceAll( "[ \\t]+$", "" );

                int separator = line.indexOf('=');
                if ( separator!=-1 ){
                    String head = line.substring(0, separator);
                    String body = line.substring(separator+1);

                    try{
                        int color = Integer.parseInt(body, 16);
                        sColorMap.put(head , color );
                    }
                    catch(Exception e){}
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( br != null ){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static File getKeywordFile(String path,String ext)
    {
        File dir = new File(path);
        File[] files = dir.listFiles();
        if ( files != null ){
            for( File f : files ){
                if ( f.isFile() ){
                    String name = f.getName();
                    String exts[] = name.split("\\.");
                    int len = exts.length-1;
                    if ( len>0 && EXT.equals(exts[len])){
                        for( int i=0;i<len;i++){
                            if ( ext.equalsIgnoreCase(exts[i])){
                                return f;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    static public boolean loadHighlight(Context context, String filename )
    {
        clearKeyword();
        if (filename == null){
            return false;
        }
        int point = filename.lastIndexOf(".");
        if (point == -1) {
            return false;
        }
        String ext = filename.substring(point + 1);

        // create direcotry
        new File(USERPATH).mkdirs();
        if ( !new File(PATH + COLOR_PATH).exists() ){
            extractFromAssets( context );
        }

        File f = getKeywordFile(USERPATH,ext);
        if ( f==null ){
            f = getKeywordFile(PATH,ext);
            if ( f==null ){
                return false;
            }
        }
        loadColorSettings();

        // parse ini file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while( (line = br.readLine()) != null ){
                line = line.replaceAll( "^//.*$" , "" );
                line = line.replaceAll( "[ \\t]+$", "" );

                int separator = line.indexOf('=');
                if ( separator!=-1 ){
                    String head = line.substring(0, separator);
                    String body = line.substring(separator+1);

                    Integer color = sColorMap.get(head);
                    if ( color!=null ){
                        addKeyword( body , color.intValue() );
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( br != null ){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        sColorMap.clear();
        return true;
    }

    static public void extractFromAssets( Context context)
    {
        AssetManager am = context.getAssets();
        byte[] buf = new byte[4096];

        try {
            // create direcotry
            new File(USERPATH).mkdirs();

            // remove all files except directory..
            File dir = new File(PATH);
            File[] files = dir.listFiles();
            if ( files != null ){
                for( File f : files ){
                    if ( f.isFile() ){
                        f.delete();
                    }
                }
            }
            // extarct files from assets.
            String[] list = am.list(ASSET_PATH);
            for( String filename : list ){
                File ofile = new File(PATH  + filename);
                InputStream in = am.open(ASSET_PATH + "/"+ filename);
                OutputStream out = new FileOutputStream(ofile);
                try{
                    int len;
                    while( (len = in.read(buf))>0 ){
                        out.write(buf, 0, len);
                    }
                }
                catch(Exception e){}
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
