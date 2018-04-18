package net.gnu.explorer;

import java.util.Comparator;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.content.pm.*;

class ProcessInfo implements Parcelable {
	final String path;
	final String label;
	final String packageName;
	final int status;
	final long size;
	final int pid;
	boolean isSystemApp = true;
	boolean isUpdatedSystemApp = false;
	boolean isInternal = true;
	boolean isExternalAsec = false;
	ActivityManager.RunningAppProcessInfo runningAppProcessInfo;

	public ProcessInfo(ActivityManager.RunningAppProcessInfo r, String label,
					   String packageName, int status, int pid, long size, ApplicationInfo ai) {
		this.label = label;
		this.packageName = packageName;
		this.status = status;
		this.size = size;
		this.pid = pid;
		this.runningAppProcessInfo = r;

		if (ai != null) {
			path = ai.sourceDir;
			isSystemApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
			isUpdatedSystemApp = (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
			isInternal = (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0;
			isExternalAsec = (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
		} else {
			path = "";
		}
	}

	public ProcessInfo(Parcel im) {
		path = im.readString();
		label = im.readString();
		status = im.readInt();
		packageName = im.readString();
		pid = im.readInt();
		size = im.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p1, int p2) {
		p1.writeString(path);
		p1.writeString(label);
		p1.writeInt(status);
		p1.writeString(packageName);
		p1.writeInt(pid);
		p1.writeLong(size);
	}

	public static final Parcelable.Creator<ProcessInfo> CREATOR = new Parcelable.Creator<ProcessInfo>() {
		public ProcessInfo createFromParcel(Parcel in) {
			return new ProcessInfo(in);
		}

		public ProcessInfo[] newArray(int size) {
			return new ProcessInfo[size];
		}
	};

//	@Override
//	public boolean equals(Object o) {
//		return packageName.equals(((ProcessInfo)o).packageName);
//	}

//	@Override
//	public String toString() {
//		return packageName + "_" + label + ": " + size;
//	}
}

public class ProcessSorter implements Comparator<ProcessInfo> {

	public static final int BY_LABEL = 0;
	public static final int BY_PID = 1;
	public static final int BY_SIZE = 2;
	// public static final int BY_EXTENSION = 3;
	public static final int BY_STATUS = 4;

	public static final int ASC = 1;
	public static final int DESC = -1;

	private int asc = ASC;
	private int sort = BY_LABEL;

	public ProcessSorter(int sort, int asc) {
		this.asc = asc;
		this.sort = sort;
	}

	@Override
	public int compare(ProcessInfo file1, ProcessInfo file2) {
		if (sort == BY_LABEL) {
			return asc * file1.label.compareToIgnoreCase(file2.label);
		} else if (sort == BY_PID) {
			return asc * (file1.pid - file2.pid);
		} else if (sort == BY_SIZE) {
			// Log.d("sort", f1 + "= " + f1.length() + ", " + f2 + "=" +
			// f2.length());
			return asc * ((int) file1.size - (int) file2.size);
			// } else if(sort == BY_EXTENSION){
			// return asc * (file1.importance - file2.importance);
		} else if (sort == BY_STATUS) {
			return asc
				* (file1.status - (file2.status));
		}
		return 0;
	}
}
