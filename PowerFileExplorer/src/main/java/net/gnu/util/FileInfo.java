//package net.gnu.util;
//
//import java.io.File;
//import java.util.Comparator;
//
//public class FileInfo implements Comparable<FileInfo>,  Comparator<FileInfo> {
//	
//	public File file;
//	public int group;
//	public long length;
//	public String path;
//
//	public FileInfo() {
//	}
//
//	public FileInfo(File file) {
//		this.file = file;
//		length = file.length();
//		path = file.getAbsolutePath();
//	}
//	
//	public FileInfo(File file, int group) {
//		this.file = file;
//		this.group = group;
//		length = file.length();
//		path = file.getAbsolutePath();
//	}
//
//	@Override
//	public int compare(FileInfo p1, FileInfo p2) {
//		FileInfo ff1 = (FileInfo) p1;
//		FileInfo ff2 = (FileInfo) p2;
//		return ff1.path.compareTo(ff2.path);
//	}
//
//	@Override
//	public int compareTo(FileInfo p1) {
//		FileInfo ff = (FileInfo) p1;
//		return path.compareTo(ff.path);
//	}
//}
