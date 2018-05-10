package net.gnu.p7zip;

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
    public long lastModified;
	public final long zipLength;
	public long length;
	public List<ZipEntry> list = new LinkedList<>();

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

//	public String[] list() {
//		final String[] ret = new String[]{};
//		return ret;
//	}

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

    public static final Parcelable.Creator<ZipEntry> CREATOR = new Parcelable.Creator<ZipEntry>() {
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
		return -path.compareTo(p1.path);
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




