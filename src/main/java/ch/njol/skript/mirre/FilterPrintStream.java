/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.mirre;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Filter;

import ch.njol.skript.command.Commands;

public class FilterPrintStream extends PrintStream {

	
	public FilterPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
	}

	public FilterPrintStream(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public FilterPrintStream(OutputStream out) {
		super(out);
	}
	
	@Override
	public synchronized void println(String string){
		if(Commands.suppressUnknownCommandMessage && string.contains("Unknown command. Type")){
			Commands.suppressUnknownCommandMessage = false;
			return;
		}
		super.println(string);
	}

}
