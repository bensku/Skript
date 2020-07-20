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
package ch.njol.skript.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class ParseLogHandler extends LogHandler {
	
	@Nullable
	private LogEntry error = null;
	
	private final List<LogEntry> log = new ArrayList<>();
	
	@Override
	public LogResult log(final LogEntry entry) {
		if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
			final LogEntry e = error;
			if (e == null || entry.getQuality() > e.getQuality()) {
				error = entry;
				if (e != null)
					e.discarded("overridden by '" + entry.getMessage() + "' (" + ErrorQuality.get(entry.getQuality()) + " > " + ErrorQuality.get(e.getQuality()) + ")");
			}
		} else {
			log.add(entry);
		}
		return LogResult.CACHED;
	}
	
	boolean printedErrorOrLog = false;
	
	@Override
	public void onStop() {
		if (!printedErrorOrLog && Skript.testing())
			SkriptLogger.LOGGER.warning("Parse log wasn't instructed to print anything at " + SkriptLogger.getCaller());
	}
	
	public void error(final String error, final ErrorQuality quality) {
		log(new LogEntry(SkriptLogger.SEVERE, quality, error));
	}
	
	/**
	 * Clears all log messages except for the error
	 */
	public void clear() {
		for (final LogEntry e : log)
			e.discarded("cleared");
		log.clear();
	}
	
	/**
	 * Prints the retained log, but no errors
	 */
	public void printLog() {
		printedErrorOrLog = true;
		stop();
		SkriptLogger.logAll(log);
		if (error != null)
			error.discarded("not printed");
	}
	
	public void printError() {
		printError(null);
	}
	
	/**
	 * Prints the best error or the given error if no error has been logged.
	 * 
	 * @param def Error to log if no error has been logged so far, can be null
	 */
	public void printError(final @Nullable String def) {
		printedErrorOrLog = true;
		stop();
		final LogEntry error = this.error;
		if (error != null)
			SkriptLogger.log(error);
		else if (def != null)
			SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, ErrorQuality.SEMANTIC_ERROR, def));
		for (final LogEntry e : log)
			e.discarded("not printed");
	}
	
	public void printError(final String def, final ErrorQuality quality) {
		printedErrorOrLog = true;
		stop();
		final LogEntry error = this.error;
		if (error != null && error.quality >= quality.quality())
			SkriptLogger.log(error);
		else
			SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, def));
		for (final LogEntry e : log)
			e.discarded("not printed");
	}
	
	public int getNumErrors() {
		return error == null ? 0 : 1;
	}
	
	public boolean hasError() {
		return error != null;
	}
	
	@Nullable
	public LogEntry getError() {
		return error;
	}
	
}
