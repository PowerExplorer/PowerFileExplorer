//package net.gnu.util;
//
//import java.util.*;
//import java.io.*;
//
//public class SortFolderNameFirstDecrease implements Comparator<File> {
//
//	@Override
//	public int compare(final File f1, final File f2) {
//		if ((!f1.isDirectory() && !f2.isDirectory()) || (f1.isDirectory() && f2.isDirectory())) {
//			return f2.getName().compareToIgnoreCase(f1.getName());
//		} else if (!f1.isDirectory() && f2.isDirectory()) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//}
