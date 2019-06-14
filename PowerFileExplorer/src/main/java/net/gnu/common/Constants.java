package net.gnu.common;
import android.graphics.*;

public class Constants {
	
	public static int SELECTED_IN_LIST = 0xFFFEF8BA;//0xFFFFF0A0
	public static int BASE_BACKGROUND = 0xFFFFFFE8;
	public static int TEXT_COLOR = 0xff404040;
	public static int BASE_BACKGROUND_LIGHT = 0xFFFFFFE8;
	public static int TEXT_COLOR_DARK = 0xff404040;
	public static int BASE_BACKGROUND_DARK = 0xff303030;
	public static int TEXT_COLOR_LIGHT = 0xfff0f0f0;
	public static int IN_DATA_SOURCE_2 = 0xFFFFF8D9;
	public static int IS_PARTIAL = 0xFFFFF0CF;
	public static final int LIGHT_GREY = 0xff909090;
	public static int DIR_COLOR = Color.BLACK;
	public static int DOT = 0xffa0a0a0;
	public static int FILE_COLOR = Color.BLACK;
	public static int DIVIDER_COLOR = 0xff707070;
	
	public static final String PREVIOUS_SELECTED_FILES = "net.gnu.explorer.selectedFiles";

	public static final String ALL_SUFFIX = "*";
	public static final String ALL_SUFFIX_TITLE = "Select Files/Folders";
	public static final String ZIP_SUFFIX = ".zpaq; .7z; .bz2; .bzip2; .tbz2; .tbz; .001; .gz; .gzip; .tgz; .tar; .dump; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi; .odt; .ods; .odp; .docx; .xlsx; .pptx; .epub; .apm; .ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi; .chq; .msi; .msp; .doc; .xls; .ppt; .cpio; .cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat; .hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit; .ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub; .nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2; .qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi; .vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz";
	public static final String ZIP_TITLE = "Compressed file (" + ZIP_SUFFIX + ")";
	public static final int FILES_REQUEST_CODE = 13;
	public static final int SAVETO_REQUEST_CODE = 14;
	public static final int STARDICT_REQUEST_CODE = 16;
	public static final boolean MULTI_FILES = true;
	public static final int OUTLINE_REQUEST_CODE = 15;

	/**
	 * Select multi files and folders
	 */
	public static final String EXTRA_MULTI_SELECT = "org.openintents.extra.MULTI_SELECT";//"multiFiles";

    public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

    public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

    public static final String ACTION_MULTI_SELECT = "org.openintents.action.MULTI_SELECT";

    public static final String ACTION_SEARCH_STARTED = "org.openintents.action.SEARCH_STARTED";

    public static final String ACTION_SEARCH_FINISHED = "org.openintens.action.SEARCH_FINISHED";

    public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

    public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";

    public static final String EXTRA_WRITEABLE_ONLY = "org.openintents.extra.WRITEABLE_ONLY";

    public static final String EXTRA_SEARCH_INIT_PATH = "org.openintents.extra.SEARCH_INIT_PATH";

    public static final String EXTRA_SEARCH_QUERY = "org.openintents.extra.SEARCH_QUERY";
    //public static final String EXTRA_DIR_PATH = "org.openintents.extra.DIR_PATH";
    public static final String EXTRA_ABSOLUTE_PATH = "org.openintents.extra.ABSOLUTE_PATH";
    public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";
    public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";
    public static final String EXTRA_DIRECTORIES_ONLY = "org.openintents.extra.DIRECTORIES_ONLY";
    public static final String EXTRA_DIALOG_FILE_HOLDER = "org.openintents.extra.DIALOG_FILE";
    public static final String EXTRA_IS_GET_CONTENT_INITIATED = "org.openintents.extra.ENABLE_ACTIONS";
    public static final String EXTRA_FILENAME = "org.openintents.extra.FILENAME";
    public static final String EXTRA_FROM_OI_FILEMANAGER = "org.openintents.extra.FROM_OI_FILEMANAGER";
	

	public static final String HEAD_TABLE = 
	"</head>\n"
	+ "<body bgcolor=\"#FFFFF0\" text=\"#000000\" link=\"#0000ff\" vlink=\"#0000ff\">\n"
	+ "<div align=\"center\">\n"
	+ "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100.0%;border-collapse:collapse\">\n";

	public static final String HTML_STYLE = 
	"<html>\n"
	+ "<head>\n"
	+ "<meta http-equiv=\"Content-Language\" content=\"en-us\" />\n"
	+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"

	+ "<style type=\"text/css\">\n" 
	+ "@font-face {\n"
	+ "    font-family: DejaVuSerifCondensed;\n"
	+ "    src: url(\"file:///android_asset/fonts/DejaVuSerifCondensed.ttf\");\n"
	+ "}\n"
	+ "td {\n"
	+ "		vertical-align: top; border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt;\n" 
	+ "}\n"
	+ "body {\n"
	+ "    font-family: DejaVuSerifCondensed;\n"
	+ "    font-size: small;\n"
	// + "    text-align: justify;"
	+ "}"
	+ "</style>\n";

	public static final String EMPTY_HEAD = 
	HTML_STYLE
	+ "</head>\n"
	+ "<body bgcolor=\"#FFFFF0\" text=\"#000000\" link=\"#0000ff\" vlink=\"#0000ff\">\n";
	public static final String TD1_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	public static final String TD2_CENTER = "<td width='76%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	public static final String TD3_CENTER = "<td width='4%' align='center' valign='middle' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	public static final String TD1_LEFT = "<td>";// width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	public static final String TD2_LEFT = "<td>";// width='76%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	public static final String TD3_LEFT = "<td>";// width='4%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\n";
	
	public static final String DOC_FILES_SUFFIX =
	".doc; .docx; .txt; .html; .odt; .rtf; .epub; .fb2; .pdf; .pps; .ppt; .pptx; .xls; .xlsx; " +
	".ods; .odp; .pub; .vsd; .htm; .xml; .xhtml; .java; .c; .cpp; .h; .md; .lua; .sh; bat; .list; .depend; .js; .jsp; .mk; .config; .configure; .machine; .asm; .css; .desktop; .inc; .shtm; .shtml; .i; .plist; .pro; .py; .s; .xpm; .ini";
	public static final String TRANSLATE_FILES_SUFFIX =
	".doc; .docx; .txt; .html; .odt; .rtf; .epub; .fb2; .pdf; .pps; .ppt; .pptx; .xls; .xlsx; " +
	".ods; .odp; .htm; .xhtml; .shtm; .shtml;";
	public static final String ORI_SUFFIX_TITLE = "Origin Document (" + DOC_FILES_SUFFIX + ")";
	public static final String MODI_SUFFIX_TITLE = "Modified Document (" + DOC_FILES_SUFFIX + ")";
	public static final String TXT_SUFFIX = ".txt";
	public static final String TXT_SUFFIX_TITLE = "Dictionary Text (" + TXT_SUFFIX + ")";
	public static final String IFO_SUFFIX = ".ifo";
	public static final String IFO_SUFFIX_TITLE = "Dictionary File (" + IFO_SUFFIX + ")";
	
	
}
