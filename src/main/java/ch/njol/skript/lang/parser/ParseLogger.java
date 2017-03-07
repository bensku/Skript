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
package ch.njol.skript.lang.parser;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.ParseLogHandler;

/**
 * Interface for logging during parsing.
 */
public interface ParseLogger {
	
	/**
	 * Submits a parse log handler. Errors will be displayed
	 * when enabling scripts, which allows them to be ordered.
	 * 
	 * It is not recommended to write anything to log after submitting it.
	 * @param log Log handler.
	 */
	void submitErrorLog(ParseLogHandler log);
	
	void submitParseLog(LogHandler log);
	
	void error(@Nullable String msg, ErrorQuality quality);
	
	void error(@Nullable String msg);
	
	void warning(@Nullable String msg);
	
	void info(@Nullable String msg);
	
	default void debug(@Nullable String msg) {
		if (Skript.debug())
			info(msg);
	}
	
	void log(@Nullable LogEntry entry);
	
	default void logAll(Collection<LogEntry> entries) {
		entries.forEach(entry -> log(entry));
	}
	
	/**
	 * Sets node for this parser instance.
	 * @param node Node.
	 */
	void setNode(@Nullable Node node);
	
	/**
	 * Gets node from this parser instance.
	 * @return Node or null, if there is no node.
	 */
	@Nullable
	Node getNode();
}
