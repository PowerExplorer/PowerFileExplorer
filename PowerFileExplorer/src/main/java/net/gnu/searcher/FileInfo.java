package net.gnu.searcher;

import java.io.File;
import java.util.Comparator;

public class FileInfo implements Comparable<FileInfo>,  Comparator<FileInfo> {
	
	public static boolean asc = true;
	public final File file;
	public int group;
	public final long length;
	public final String path;

	public FileInfo(final File file) {
		this.file = file;
		length = file.length();
		path = file.getAbsolutePath();
	}
	
	public FileInfo(final File file, final int group) {
		this.file = file;
		this.group = group;
		length = file.length();
		path = file.getAbsolutePath();
	}

	@Override
	public int compare(final FileInfo p1, final FileInfo p2) {
		return asc ? p1.path.compareTo(p2.path) : p2.path.compareTo(p1.path);
	}

	@Override
	public int compareTo(final FileInfo p1) {
		return asc ? path.compareTo(p1.path) : p1.path.compareTo(path);
	}
}
