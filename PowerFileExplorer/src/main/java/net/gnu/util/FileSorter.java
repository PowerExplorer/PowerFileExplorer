package net.gnu.util;

import java.util.Comparator;
import net.gnu.util.FileUtil;
import java.io.File;

public class FileSorter implements Comparator<File> {

	public static final int DIR_TOP = 0;
	public static final int DIR_BOTTOM = 1;

	public static final int NAME = 0;
	public static final int DATE = 1;
	public static final int SIZE = 2;
	public static final int TYPE = 3;

	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;

    private final int dirsOnTop;// = 0;
    private final int sort;// = 0;
	private final int asc;// = 1;
    
    public FileSorter(final int dir, final int sort, final int asc) {
        this.dirsOnTop = dir;
        this.asc = asc;
        this.sort = sort;
    }

    /**
     * Compares two elements and return negative, zero and positive integer if first argument is
     * less than, equal to or greater than second
     * @param file1
     * @param file2
     * @return
     */
    @Override
    public int compare(final File file1, final File file2) {

        if (dirsOnTop == DIR_TOP) {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (file2.isDirectory() && !file1.isDirectory()) {
                return 1;
            }
        } else {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return 1;
            } else if (file2.isDirectory() && !file1.isDirectory()) {
                return -1;
            }
        }
		final int res;
		String name1 = null;
		String name2 = null;
        if (sort == NAME) {
			res = (asc > 0) ? file1.getName().compareToIgnoreCase(file2.getName()) : file2.getName().compareToIgnoreCase(file1.getName());
			if (res == 0) {
				res = file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
			}
			return res;
        } else if (sort == DATE) {
            res = (asc > 0) ? Long.valueOf(file1.lastModified()).compareTo(file2.lastModified()) : Long.valueOf(file2.lastModified()).compareTo(file1.lastModified());
        } else if (sort == SIZE) {
            if (file1.isDirectory()) {
				final String[] list1 = file1.list();
				final int length1 = ((list1 == null) ? 0 : list1.length);
				final String[] list2 = file2.list();
				final int length2 = ((list2 == null) ? 0 : list2.length);
				res = (asc > 0) ? (length1 - length2) : (length2 - length1);
            } else {
				final long length1 = file1.length();
				final long length2 = file2.length();
				if (length1 < length2) {
					res = (asc > 0) ? -1 : 1;
				} else if (length1 > length2) {
					res = (asc > 0) ? 1 : -1;
				} else {
					res = 0;
				}
				//res = (asc > 0) ? Long.valueOf(file1.length()).compareTo(file2.length()) : Long.valueOf(file2.length()).compareTo(file1.length());
            }
        } else {
			name1 = file1.getName();
			name2 = file2.getName();
            final String ext_a = FileUtil.getExtension(name1);
			final String ext_b = FileUtil.getExtension(name2);
			res = (asc > 0) ? ext_a.compareTo(ext_b) : ext_b.compareTo(ext_a);
        }
		if (res == 0) {
			if (name1 == null) {
				name1 = file1.getName();
				name2 = file2.getName();
			}
			res = name1.compareToIgnoreCase(name2);
			if (res == 0) {
				res = file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
			}
		}
		return res;
    }
}

