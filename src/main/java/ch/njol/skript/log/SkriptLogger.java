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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.LogHandler.LogResult;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptLogger {
	
	@SuppressWarnings("null")
	public final static Level SEVERE = Level.SEVERE;
	
	@Nullable
	private static Node node = null;
	
	private static Verbosity verbosity = Verbosity.NORMAL;
	
	static boolean debug;
	
	@SuppressWarnings("null")
	public final static Level DEBUG = Level.INFO; // CraftBukkit 1.7+ uses the worst logging library I've ever encountered
//			new Level("DEBUG", Level.INFO.intValue()) {
//				private final static long serialVersionUID = 8959282461654206205L;
//			};
	
	@SuppressWarnings("null")
	public final static Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // cannot use Bukkit in tests
	
	private final static HandlerList handlers = new HandlerList();
	
	/**
	 * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link RetainingLogHandler}());</tt>
	 * 
	 * @return A newly created RetainingLogHandler
	 */
	public static RetainingLogHandler startRetainingLog() {
		return startLogHandler(new RetainingLogHandler());
	}
	
	/**
	 * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link ParseLogHandler}());</tt>
	 * 
	 * @return A newly created ParseLogHandler
	 */
	public static ParseLogHandler startParseLogHandler() {
		return startLogHandler(new ParseLogHandler());
	}
	
	/**
	 * Starts a log handler.
	 * <p>
	 * This should be used like this:
	 * 
	 * <pre>
	 * LogHandler log = SkriptLogger.startLogHandler(new ...LogHandler());
	 * try {
	 * 	doSomethingThatLogsMessages();
	 * 	// do something with the logged messages
	 * } finally {
	 * 	log.stop();
	 * }
	 * </pre>
	 * 
	 * @return The passed LogHandler
	 * @see #startParseLogHandler()
	 * @see #startRetainingLog()
	 * @see BlockingLogHandler
	 * @see CountingLogHandler
	 * @see ErrorDescLogHandler
	 * @see FilteringLogHandler
	 * @see RedirectingLogHandler
	 */
	public static <T extends LogHandler> T startLogHandler(final T h) {
		handlers.add(h);
		return h;
	}
	
	static void removeHandler(final LogHandler h) {
		if (!handlers.contains(h))
			return;
		if (!h.equals(handlers.remove())) {
			int i = 1;
			while (!h.equals(handlers.remove()))
				i++;
			LOGGER.severe("[Skript] " + i + " log handler" + (i == 1 ? " was" : "s were") + " not stopped properly! (at " + getCaller() + ") [if you're a server admin and you see this message please file a bug report at https://github.com/bensku/skript/issues if there is not already one]");
		}
	}
	
	static boolean isStopped(final LogHandler h) {
		return !handlers.contains(h);
	}
	
	@Nullable
	static StackTraceElement getCaller() {
		for (final StackTraceElement e : new Exception().getStackTrace()) {
			if (!e.getClassName().startsWith(SkriptLogger.class.getPackage().getName()))
				return e;
		}
		return null;
	}
	
	public static void setVerbosity(final Verbosity v) {
		verbosity = v;
		if (v.compareTo(Verbosity.DEBUG) >= 0)
			debug = true;
	}
	
	public static void setNode(final @Nullable Node node) {
		SkriptLogger.node = node == null || node.getParent() == null ? null : node;
	}
	
	@Nullable
	public static Node getNode() {
		return node;
	}
	
	/**
	 * Logging should be done like this:
	 * 
	 * <pre>
	 * if (Skript.logNormal())
	 * 	Skript.info(&quot;this information is displayed on verbosity normal or higher&quot;);
	 * </pre>
	 * 
	 * @param level
	 * @param message
	 * @see Skript#info(String)
	 * @see Skript#warning(String)
	 * @see Skript#error(String)
	 * @see Skript#logNormal()
	 * @see Skript#logHigh()
	 * @see Skript#logVeryHigh()
	 * @see Skript#debug()
	 */
	public static void log(final Level level, final String message) {
		log(new LogEntry(level, message, node));
	}
	
	public static void log(final @Nullable LogEntry entry) {
		if (entry == null)
			return;
		if (Skript.testing() && node != null && node.debug())
			System.out.print("---> " + entry.level + "/" + ErrorQuality.get(entry.quality) + ": " + entry.getMessage() + " ::" + LogEntry.findCaller());
		for (final LogHandler h : handlers) {
			final LogResult r = h.log(entry);
			switch (r) {
				case CACHED:
					return;
				case DO_NOT_LOG:
					entry.discarded("denied by " + h);
					return;
				case LOG:
					continue;
			}
		}
		entry.logged();
		LOGGER.log(entry.getLevel(), "[Skript] " + entry.getMessage());
	}
	
	public static void logAll(final Collection<LogEntry> entries) {
		for (final LogEntry entry : entries) {
			if (entry == null)
				continue;
			log(entry);
		}
	}
	
	public static void logTracked(final Level level, final String message, final ErrorQuality quality) {
		log(new LogEntry(level, quality.quality(), message, node, true));
	}
	
	/**
	 * Checks whether messages should be logged for the given verbosity.
	 * 
	 * @param minVerb minimal verbosity
	 * @return Whether messages should be logged for the given verbosity.
	 */
	public static boolean log(final Verbosity minVerb) {
		return minVerb.compareTo(verbosity) <= 0;
	}
	
	public static boolean debug() {
		return debug;
	}
	
}
