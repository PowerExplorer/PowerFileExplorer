package net.gnu.p7zip;

import java.io.File;
import java.util.Set;
import net.gnu.p7zip.ZipEntry;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.TreeMap;

public class Zip {
	
	public final File file;
	public long zipSize = 0;
	public long unZipSize = 0;
	public final Map<String, ZipEntry> entries = new TreeMap<>();
	public int folderCount = 0;
	public int fileCount = 0;

	public Zip(File file)	{
		this.file = file;
	}
	
	public long[] folderSize(final ZipEntry ze) {
		final Collection<ZipEntry> values = entries.values();
		final String parent = ze.path + "/";
		long retLength = 0;
		long retZipLength = 0;
		for (ZipEntry e : values) {
			if (e.path.startsWith(parent)) {
				retLength += e.length;
				retZipLength += e.zipLength;
			}
		}
		return new long [] {retLength, retZipLength};
	}

	@Override
	public String toString() {
		return file.getAbsolutePath() + ", " + zipSize + ", " + unZipSize + ", " + fileCount + ", " + folderCount + ", " + entries;
	}
}
