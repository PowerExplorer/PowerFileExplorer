package net.gnu.explorer;

import java.util.*;
import java.io.*;
import net.gnu.util.*;
import android.os.Parcel;
import android.os.Parcelable;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;

class AppInfo implements Parcelable {

	final String label;
	final String path;
	final String packageName;
	final String version;
	final String size;
	final long longDate;
	final long longSize;
	final String date;

	boolean isSystemApp;
	boolean isUpdatedSystemApp;
	boolean isInternal;
	boolean isExternalAsec;

	public AppInfo(final PackageManager packageManager, final PackageInfo p) {
		final ApplicationInfo ai = p.applicationInfo;

		this.path = ai.sourceDir;
		this.label = ai.loadLabel(packageManager).toString();
		this.packageName = p.packageName;
		this.version = p.versionName + "";

		this.isSystemApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		this.isUpdatedSystemApp = (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
		this.isInternal = (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0;
		this.isExternalAsec = (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;

		final File f = new File(path);
		this.longSize = f.length();
		this.size = Util.nf.format(longSize) + " B";
		this.longDate = f.lastModified();
		this.date = Util.dtf.format(longDate);
	}

	public AppInfo(Parcel im) {
		this.label = im.readString();
		this.path = im.readString();
		this.packageName = im.readString();
		this.version = im.readString();
		this.longDate = im.readLong();
		this.longSize = im.readLong();
		this.isSystemApp = im.readInt() != 0;

		this.size = Util.nf.format(longSize) + " B";
		this.date = Util.dtf.format(longDate);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p1, int p2) {
		p1.writeString(label);
		p1.writeString(path);
		p1.writeString(packageName);
		p1.writeString(version);
		p1.writeLong(longDate);
		p1.writeLong(longSize);
		p1.writeInt(isSystemApp ? 1 : 0);
	}

	public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
		public AppInfo createFromParcel(Parcel in) {
			return new AppInfo(in);
		}

		public AppInfo[] newArray(int size) {
			return new AppInfo[size];
		}
	};

	@Override
	public boolean equals(Object o) {
		return packageName.equals(((AppInfo)o).packageName);
	}

	@Override
	public String toString() {
		return label + ": " + packageName;
	}

	@Override
	public int hashCode() {
		return packageName.hashCode();
	}
}


public class AppListSorter implements Comparator<AppInfo> {

	public static final int BY_LABEL = 0;
	public static final int BY_DATE = 1;
	public static final int BY_SIZE = 2;
	//public static final int BY_EXTENSION = 3;
	public static final int BY_PACKAGE = 4;

	public static final int ASC = 1;
	public static final int DESC = -1;

	private int asc = 1;
	private int sort = 0;
	private boolean rootMode;

	public AppListSorter(int sort, int asc, boolean rootMode) {
		this.asc = asc;
		this.sort = sort;
		this.rootMode = rootMode;
	}

	@Override
	public int compare(final AppInfo file1, final AppInfo file2) {

		final File f1 = new File(file1.path);
		final File f2 = new File(file2.path);

		if (sort == BY_LABEL) {
			return asc * file1.label.compareToIgnoreCase(file2.label);
		} else if (sort == BY_DATE) {
			return asc
				* Long.valueOf(file1.longDate).compareTo(
				Long.valueOf(file2.longDate));
		} else if (sort == BY_SIZE) {
			// Log.d("sort", f1 + "= " + f1.length() + ", " + f2 + "=" +
			// f2.length());
			if (f1.isFile() && f2.isFile()) {
				return asc
					* Long.valueOf(file1.longSize).compareTo(
					Long.valueOf(file2.longSize));
			} else {
				return asc
					* file1.label
					.compareToIgnoreCase(file2.label);
			}
		} else if (sort == BY_PACKAGE) {
			// Log.d("sort", f1 + ", " + f2);
			if (f1.isFile() && f2.isFile()) {
				final String ext_a = file1.packageName;
				final String ext_b = file2.packageName;
				final int res = asc * ext_a.compareToIgnoreCase(ext_b);
				return res;
			} else {
				return file1.label.compareToIgnoreCase(file2.label);
			}
		}
		return 0;
	}
}

