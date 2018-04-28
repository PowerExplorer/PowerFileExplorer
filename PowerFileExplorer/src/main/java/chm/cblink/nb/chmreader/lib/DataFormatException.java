/**
 * Copyright (C) 2007 Rui Shen (rui.shen@gmail.com) All Right Reserved
 * File     : DataFormatException.java
 * Created	: 2007-3-1
 * ****************************************************************************
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA  02111-1307, USA.
 * *****************************************************************************
 */
package chm.cblink.nb.chmreader.lib;

import java.io.IOException;

public class DataFormatException extends IOException {

	public DataFormatException() {
		super();
	}

	public DataFormatException(String message) {
		super(message);
	}

}
