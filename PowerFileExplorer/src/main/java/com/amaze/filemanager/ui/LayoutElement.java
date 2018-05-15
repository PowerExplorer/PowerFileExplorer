/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.ui;

import android.os.Parcel;
import android.os.Parcelable;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.utils.OpenMode;
import java.io.File;

public class LayoutElement implements Parcelable {

    //private static final String CURRENT_YEAR = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

    //private BitmapDrawable imageId;
    public final String name;
    public final String path;
    public final String permissions;
    public final String symlink;
    //private String lengthStr;
    public final boolean isDirectory;
    public final long lastModified,length;
//    private String lastModifiedStr = "";
    //private boolean header;
    //same as hfile.modes but different than openmode in Main.java
    private OpenMode mode = OpenMode.FILE;
	public final BaseFile bf;

    public LayoutElement(/*BitmapDrawable imageId, */final String name, final String path, final String permissions,
                          final String symlink, /*String lengthStr, */final long length, /*boolean header, */final long lastModified, final boolean isDirectory) {
        //this.imageId = imageId;
        this.name = name;
        this.path = path;
        this.permissions = permissions.trim();
        this.symlink = symlink.trim();
        //this.lengthStr = lengthStr;
        //this.header = header;
        this.length = length;
        this.isDirectory = isDirectory;
		this.lastModified = lastModified;
		bf = generateBaseFile();
//        if (!lastModifiedStr.trim().equals("")) {
//            this.lastModified = Long.parseLong(lastModifiedStr);
////            this.lastModifiedStr = Futils.getdate(this.lastModified,CURRENT_YEAR);
//        }
    }

	public LayoutElement(BaseFile f) {
		this.name = f.getName();
        this.path = f.getPath();
		//Log.d("le", name+", "+path);
        //this.permissions = permissions.trim();
		final boolean canRead = f.canRead();
		final boolean canWrite = f.canWrite();
		String st = "";
		this.isDirectory = f.isDirectory();
		if (isDirectory) {
			if (canWrite) {
				st = "drw";
			} else if (canRead) {
				st = "dr-";
			} else {
				st = "d--";
			}
		} else {
			if (canWrite) {
				st = "-rw";
			} else if (canRead) {
				st = "-r-";
			} else {
				st = "---";
			}
		}
		this.permissions = st;
        //this.symlink = symlink.trim();
        //this.lengthStr = lengthStr;
        //this.header = header;
        this.length = f.f.length();
        this.lastModified = f.f.lastModified();
		this.symlink = "";
		bf = f;
	}

	public LayoutElement(File f) {
		this.name = f.getName();
        this.path = f.getAbsolutePath();
        //this.permissions = permissions.trim();
		final boolean canRead = f.canRead();
		final boolean canWrite = f.canWrite();
		String st = "";
		this.isDirectory = f.isDirectory();
		if (isDirectory) {
			if (canWrite) {
				st = "drw";
			} else if (canRead) {
				st = "dr-";
			} else {
				st = "d--";
			}
		} else {
			if (canWrite) {
				st = "-rw";
			} else if (canRead) {
				st = "-r-";
			} else {
				st = "---";
			}
		}
		this.permissions = st;
        //this.symlink = symlink.trim();
        //this.lengthStr = lengthStr;
        //this.header = header;
        this.length = f.length();
        this.lastModified = f.lastModified();
		this.symlink = "";
		bf = generateBaseFile();
	}

    public LayoutElement(Parcel im) {
        name = im.readString();
        path = im.readString();
        permissions = im.readString();
        symlink = im.readString();
        isDirectory = im.readInt() != 0;
        lastModified = im.readLong();
        //int i = im.readInt();
        //header = i != 0;
        // don't save bitmaps in parcel, it might exceed the allowed transaction threshold
        //Bitmap bitmap = (Bitmap) im.readParcelable(getClass().getClassLoader());
        // Convert Bitmap to Drawable:
        //imageId = new BitmapDrawable(bitmap);
//        lastModifiedStr = im.readString();
        //lengthStr = im.readString();
        length = im.readLong();
		bf = generateBaseFile();
    }

    public BaseFile generateBaseFile() {
        BaseFile baseFile=new BaseFile(path, permissions, lastModified, length, isDirectory);
        baseFile.setMode(mode);
        baseFile.setName(name);
        return baseFile;
    }
	
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p1, int p2) {
        p1.writeString(name);
        p1.writeString(path);
        p1.writeString(permissions);
        p1.writeString(symlink);
        p1.writeInt(isDirectory ?1: 0);
        p1.writeLong(lastModified);
        //p1.writeInt(header ? 1 : 0);
        //p1.writeParcelable(imageId.getBitmap(), p2);
//        p1.writeString(lastModifiedStr);
        //p1.writeString(lengthStr);
        p1.writeLong(length);
    }

    public static final Parcelable.Creator<LayoutElement> CREATOR =
	new Parcelable.Creator<LayoutElement>() {
		public LayoutElement createFromParcel(Parcel in) {
			return new LayoutElement(in);
		}

		public LayoutElement[] newArray(int size) {
			return new LayoutElement[size];
		}
	};

//    public Drawable getImageId() {
//        return imageId;
//    }

//    public void setImageId(BitmapDrawable imageId) {
//		this.imageId = imageId;
//	}

//    public String getPath() {
//        return path;
//    }
//
//    public String getName() {
//        return name;
//    }
//
    public OpenMode getMode() {
        return mode;
    }

    public void setMode(OpenMode mode) {
        this.mode = mode;
    }

//    public boolean isDirectory() {
//        return isDirectory;
//    }

//    public String lengthStr() {
//        return lengthStr;
//    }

//    public long length() {
//        return length;
//    }

//    public String lastModifiedStr() {
//        return lastModifiedStr;
//    }

//    public long lastModified() {
//        return lastModified;
//    }
//
//    public String getPermissions() {
//        return permissions;
//    }
//
    public String getSymlink() {
        return symlink;
    }

    public boolean hasSymlink() {
        return getSymlink() != null && getSymlink().length() != 0;
    }

    @Override
    public String toString() {
        return name + "\n" + path;
    }

	@Override
	public boolean equals(Object o) {
		if (o instanceof LayoutElement) {
			return path.equals(((LayoutElement)o).path) && name.equals(((LayoutElement)o).name);
		} else {
			return false;
		}
	}
	
}
