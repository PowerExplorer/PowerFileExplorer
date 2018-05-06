package net.gnu.explorer;

import android.os.Parcelable;
import android.os.Parcel;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import net.gnu.util.FileUtil;

public class ZipEntry implements Comparable<ZipEntry>, Parcelable {
	
    public final String name;
    public final String path;
    public final String parentPath;
    public final boolean isDirectory;
    public final long lastModified;
	public final long zipLength;
	public final long length;
	
    public ZipEntry(final String name, final String path, final boolean isDirectory, final long length, final long zipLength, final long lastModified) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = path.substring(path.lastIndexOf("/") + 1);
		}
        this.path = path;
		this.isDirectory = isDirectory;
		this.length = length;
		this.zipLength = zipLength;
        this.lastModified = lastModified;
		final int index = path.lastIndexOf("/");
		this.parentPath = index > 0 ? path.substring(0, index) : "/";
    }

    public ZipEntry(final Parcel im) {
        name = im.readString();
        path = im.readString();
        isDirectory = im.readInt() != 0;
        length = im.readLong();
        zipLength = im.readLong();
        lastModified = im.readLong();
		final int index = path.lastIndexOf("/");
		this.parentPath = index > 0 ? path.substring(0, index) : "/";
    }

	public String[] list() {
		final String[] ret = new String[]{};
		return ret;
	}
	
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(final Parcel p1, final int p2) {
        p1.writeString(name);
        p1.writeString(path);
        p1.writeInt(isDirectory ? 1: 0);
        p1.writeLong(length);
        p1.writeLong(zipLength);
        p1.writeLong(lastModified);
    }

    public static final Parcelable.Creator<ZipEntry> CREATOR =
	new Parcelable.Creator<ZipEntry>() {
		public ZipEntry createFromParcel(final Parcel in) {
			return new ZipEntry(in);
		}

		public ZipEntry[] newArray(final int size) {
			return new ZipEntry[size];
		}
	};

    @Override
    public String toString() {
        return parentPath + ", dir " + isDirectory;
    }

	@Override
	public int compareTo(ZipEntry p1) {
		return path.compareTo(p1.path);
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o instanceof ZipEntry) {
			return path.equals(((ZipEntry)o).path);
		} else {
			return false;
		}
	}
}

class ZipListSorter implements Comparator<ZipEntry> {

	public static final int DIR_TOP = 0;
	public static final int DIR_BOTTOM = 1;

	public static final int NAME = 0;
	public static final int DATE = 1;
	public static final int SIZE = 2;
	public static final int TYPE = 3;

	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;

    private int dirsOnTop = 0;
    private int asc = 1;
    private int sort = 0;

    public ZipListSorter(int dir, int sort, int asc) {
        this.dirsOnTop = dir;
        this.asc = asc;
        this.sort = sort;
    }

    @Override
    public int compare(ZipEntry file1, ZipEntry file2) {

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

        if (sort == NAME) {
            // sort by name
            return asc * file1.name.compareToIgnoreCase(file2.name);
        } else if (sort == DATE) {
            // sort by last modified
            return asc * Long.valueOf(file1.lastModified).compareTo(file2.lastModified);
        } else if (sort == SIZE) {
            // sort by size
            if (!file1.isDirectory && !file2.isDirectory) {
				return asc * Long.valueOf(file1.length).compareTo(file2.length);
            } else {
                return asc * (file1.list().length - file2.list().length);
            }
        } else if (sort == TYPE) {
            // sort by type
            if (!file1.isDirectory && !file2.isDirectory) {

                final String name1 = file1.name;
				final String ext_a = FileUtil.getExtension(name1);
                final String name2 = file2.name;
				final String ext_b = FileUtil.getExtension(name2);

                final int res = asc * ext_a.compareTo(ext_b);
                if (res == 0) {
                    return asc * name1.compareToIgnoreCase(name2);
                }
                return res;
            } else {
                return file1.name.compareToIgnoreCase(file2.name);
            }
        }
        return 0;

    }
}


