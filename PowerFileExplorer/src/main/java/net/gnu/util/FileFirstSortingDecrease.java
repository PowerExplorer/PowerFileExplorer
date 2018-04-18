//package net.gnu.util;
//import java.util.*;
//import java.io.*;
//
//public class FileFirstSortingDecrease implements Comparator<File> {
//
//	@Override
//	public int compare(File f1, File f2) {
//		if (!f1.isDirectory() && !f2.isDirectory() || f1.isDirectory() && f2.isDirectory()) {
//			return f2.getAbsolutePath().compareToIgnoreCase(f1.getAbsolutePath());
//		} else if (!f1.isDirectory() && f2.isDirectory()) {
//			return -1;
//		} else {
//			return 1;
//		}
//	}
//}
