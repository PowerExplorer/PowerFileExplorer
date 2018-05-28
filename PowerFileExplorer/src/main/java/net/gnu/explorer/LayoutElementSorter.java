/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.gnu.explorer;

import com.amaze.filemanager.ui.LayoutElement;

import java.util.Comparator;
import net.gnu.util.FileUtil;

public class LayoutElementSorter implements Comparator<LayoutElement> {

	public static final int DIR_TOP = 0;
	public static final int DIR_BOTTOM = 1;

	public static final int NAME = 0;
	public static final int DATE = 1;
	public static final int SIZE = 2;
	public static final int TYPE = 3;

	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;

    private final int dirsOnTop;// = 0;
    private final int asc;// = 1;
    private final int sort;// = 0;

    public LayoutElementSorter(final int dir, final int sort, final int asc) {
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
    public int compare(final LayoutElement file1, final LayoutElement file2) {

//        File f1;
//		if (!file1.hasSymlink()) {
//			f1 = new File(file1.path);
//		} else {
//			f1 = new File(file1.getSymlink());
//		}
//
//		File f2;
//		if (!file2.hasSymlink()) {
//			f2 = new File(file2.path);
//		} else {
//			f2 = new File(file1.getSymlink());
//		}

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
				res = (asc > 0) ? (file1.bf.f.list().length - file2.bf.f.list().length) : (file2.bf.f.list().length - file1.bf.f.list().length);
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
