//package net.gnu.util;
//
//import java.util.*;
//import java.io.*;
//
//public class SortFileTypeDecrease implements Comparator<File> {
//	@Override
//	public int compare(final File p1, final File p2) {
//		if (!p1.isDirectory() && !p2.isDirectory()) {
//			final String namef1 = p1.getName();
//			int lastIndexOf = namef1.lastIndexOf(".");
//			final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");
//			
//			final String namef2 = p2.getName();
//			lastIndexOf = namef2.lastIndexOf(".");
//			final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");
//			
//			if (type2.equals(type1)) {
//				return namef2.compareToIgnoreCase(namef1);
//			} else {
//				return type2.compareToIgnoreCase(type1);
//			}
//		} else if (p1.isDirectory() && p2.isDirectory()) {
//			return p2.getName().compareToIgnoreCase(p1.getName());
//		} else if (!p1.isDirectory() && p2.isDirectory()) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//}
//
