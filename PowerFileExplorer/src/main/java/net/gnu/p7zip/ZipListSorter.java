package net.gnu.p7zip;

import java.util.Comparator;
import net.gnu.util.FileUtil;

public class ZipListSorter implements Comparator<ZipEntry> {

	public static final int DIR_TOP = 0;
	public static final int DIR_BOTTOM = 1;

	public static final int NAME = 0;
	public static final int DATE = 1;
	public static final int SIZE = 2;
	public static final int TYPE = 3;

	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;

    private final int dirsOnTop;
    private final int asc;
    private final int sort;

    public ZipListSorter(final int dir, final int sort, final int asc) {
        this.dirsOnTop = dir;
        this.asc = asc;
        this.sort = sort;
    }

    @Override
    public int compare(final ZipEntry file1, final ZipEntry file2) {

        if (dirsOnTop == DIR_TOP) {
            if (file1.isDirectory && !file2.isDirectory) {
                return -1;
            } else if (file2.isDirectory && !file1.isDirectory) {
                return 1;
            }
        } else {
            if (file1.isDirectory && !file2.isDirectory) {
                return 1;
            } else if (file2.isDirectory && !file1.isDirectory) {
                return -1;
            }
        }
		
		final int res;
        if (sort == NAME) {
            res = (asc > 0) ? file1.name.compareToIgnoreCase(file2.name) : file2.name.compareToIgnoreCase(file1.name);
			if (res == 0) {
				res = file1.path.compareToIgnoreCase(file2.path);
			}
			return res;
        } else if (sort == DATE) {
            res = (asc > 0) ? Long.valueOf(file1.lastModified).compareTo(file2.lastModified) : Long.valueOf(file2.lastModified).compareTo(file1.lastModified);
        } else if (sort == SIZE) {
            if (file1.isDirectory) {
				res = (asc > 0) ? (file1.list.size() - file2.list.size()) : (file2.list.size() - file1.list.size());
            } else {
				res = (asc > 0) ? Long.valueOf(file1.length).compareTo(file2.length) : Long.valueOf(file2.length).compareTo(file1.length);
            }
        } else {
			final String ext_a = FileUtil.getExtension(file1.name);
			final String ext_b = FileUtil.getExtension(file2.name);
			res = (asc > 0) ? ext_a.compareTo(ext_b) : ext_b.compareTo(ext_a);
        }
		if (res == 0) {
			res = file1.name.compareToIgnoreCase(file2.name);
			if (res == 0) {
				res = file1.path.compareToIgnoreCase(file2.path);
			}
		}
		return res;
    }
}
