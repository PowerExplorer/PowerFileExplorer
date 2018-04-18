package net.gnu.util;

import java.io.File;
import java.util.Comparator;
import android.util.*;

public class SortFileSizeIncrease implements Comparator<File> {
	@Override
	public int compare(final File p1, final File p2) {
//		if (p1.isFile() == p1.isDirectory()) {
//			Log.d("SortFileSizeIncrease", p1.getAbsolutePath());
//		}
		final boolean isDirectory1 = p1.isDirectory();
		final boolean isDirectory2 = p2.isDirectory();
		if (!isDirectory1 && !isDirectory2) {
			final long length1 = p1.length();
			final long length2 = p2.length();
			if (length1 < length2) {
				return -1;
			} else if (length1 > length2) {
				return 1;
			} else {
				return 0;//(int)(length1 - length2);
			}
		} else if (isDirectory1 && isDirectory2) {
			final String[] list1 = p1.list();
			final int length1 = ((list1 == null) ? 0 : list1.length);
			final String[] list2 = p2.list();
			final int length2 = ((list2 == null) ? 0 : list2.length);
			return (length1 - length2);
		} else if (!isDirectory1 && isDirectory2) {
			return 1;
		} else {
			return -1;
		}
	}
}

