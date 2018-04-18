//package net.gnu.util;
//
//import java.io.*;
//import java.util.*;
//
//public class SortFileDateDecrease implements Comparator<File> {
//	@Override
//	public int compare(final File p1, final File p2) {
//		if ((!p1.isDirectory() && !p2.isDirectory()) || (p1.isDirectory() && p2.isDirectory())) {
//			final long lastModified1 = p1.lastModified();
//			final long lastModified2 = p2.lastModified();
//			if (lastModified1 < lastModified2) {
//				return 1;
//			} else if (lastModified1 > lastModified2) {
//				return -1;
//			} else {
//				return p2.getName().compareToIgnoreCase(p1.getName());
//			}
//		} else if (!p1.isDirectory() && p2.isDirectory()) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//}
