//package net.gnu.util;
//
//import java.io.File;
//import java.util.Comparator;
//
//public class FileFirstSortingIncrease implements Comparator<File> {
//
//	@Override
//	public int compare(File f1, File f2) {
//		if (!f1.isDirectory() && !f2.isDirectory() || f1.isDirectory() && f2.isDirectory()) {
//			return f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath());
//		} else if (!f1.isDirectory() && f2.isDirectory()) {
//			return -1;
//		} else {
//			return 1;
//		}
//	}
//}
//
//
