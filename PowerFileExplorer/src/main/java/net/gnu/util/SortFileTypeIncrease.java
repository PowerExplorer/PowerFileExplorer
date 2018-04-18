//package net.gnu.util;
//
//import java.util.*;
//import java.io.*;
//
//public class SortFileTypeIncrease implements Comparator<File> {
//	@Override
//	public int compare(final File p1, final File p2) {
//		if (!p1.isDirectory() && !p2.isDirectory()) {
//			final String namef1 = p1.getName().toLowerCase();
//			int lastIndexOf = namef1.lastIndexOf(".");
//			final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");
//
//			final String namef2 = p2.getName().toLowerCase();
//			lastIndexOf = namef2.lastIndexOf(".");
//			final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");
//			if (type1.equals(type2)) {
//				return namef1.compareToIgnoreCase(namef2);
//			} else {
//				return type1.compareToIgnoreCase(type2);
//			}
//		} else if (p1.isDirectory() && p2.isDirectory()) {
//			return p1.getName().compareToIgnoreCase(p2.getName());
//		} else if (!p1.isDirectory() && p2.isDirectory()) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//}
//
