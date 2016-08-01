/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.eclipse.jdt.annotation.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Version;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Peter Güttinger
 */
public final class Updater {
	
	@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
	public final static class VersionInfo implements Comparable<VersionInfo> {
		final String name; // exact name, e.g. "2.0 (jar only)"
		final Version version;
		final String downloadURL;
		@Nullable
		@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "used in SkriptCommand")
		String changelog;
		@Nullable
		@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "used in SkriptCommand")
		Date date;
		
		VersionInfo(final String name, final Version version, final String downloadURL) {
			this.name = name;
			this.version = version;
			this.downloadURL = downloadURL;
		}
		
		@Override
		public String toString() {
			return version.toString();
		}
		
		@Override
		public int compareTo(final @Nullable VersionInfo o) {
			return version.compareTo(o == null ? null : o.version);
		}
	}
	
	/**
	 * Used to check for updates & get the file download links as required by the Bukkit guidelines
	 */
	private final static String filesURL = "https://api.curseforge.com/servermods/files?projectIds=32084";
	
	/**
	 * Used to get the changelogs and release dates of the newest files
	 */
	private final static String RSSURL = "http://dev.bukkit.org/server-mods/skript/files.rss";
	
	private final static DateFormat RFC2822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	
	public static enum UpdateState {
		NOT_STARTED, CHECK_IN_PROGRESS, CHECK_ERROR, CHECKED_FOR_UPDATE, DOWNLOAD_IN_PROGRESS, DOWNLOAD_ERROR, DOWNLOADED;
	}
	
	public final static ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	/**
	 * must be synchronised with {@link #stateLock}
	 */
	public static volatile UpdateState state = UpdateState.NOT_STARTED;
	final static AtomicReference<String> error = new AtomicReference<String>();
	
	public final static List<VersionInfo> infos = new ArrayList<VersionInfo>();
	public final static AtomicReference<VersionInfo> latest = new AtomicReference<VersionInfo>();
	
	// must be down here as they reference 'error' and 'latest' which are defined above
	public final static Message m_not_started = new Message("updater.not started");
	public final static Message m_checking = new Message("updater.checking");
	public final static Message m_check_in_progress = new Message("updater.check in progress");
	public final static FormattedMessage m_check_error = new FormattedMessage("updater.check error", error);
	public final static Message m_running_latest_version = new Message("updater.running latest version");
	public final static Message m_running_latest_version_beta = new Message("updater.running latest version (beta)");
	public final static FormattedMessage m_update_available = new FormattedMessage("updater.update available", latest, Skript.getVersion());
	public final static FormattedMessage m_downloading = new FormattedMessage("updater.downloading", latest);
	public final static Message m_download_in_progress = new Message("updater.download in progress");
	public final static FormattedMessage m_download_error = new FormattedMessage("updater.download error", error);
	public final static FormattedMessage m_downloaded = new FormattedMessage("updater.downloaded", latest);
	public final static Message m_internal_error = new Message("updater.internal error");
	
	@Nullable
	static Task checkerTask = null;
	
	static void start() {
		checkerTask = new Task(Skript.getInstance(), 0, true) {
			@SuppressWarnings("null")
			@Override
			public void run() {
				if (!SkriptConfig.checkForNewVersion.value())
					return;
				check(Bukkit.getConsoleSender(), SkriptConfig.automaticallyDownloadNewVersion.value(), true);
				final Timespan t = SkriptConfig.updateCheckInterval.value();
				if (t.getTicks_i() != 0)
					setNextExecution(t.getTicks_i());
			}
		};
	}
	
	/**
	 * @param sender Sender to receive messages
	 * @param download Whether to directly download the newest version if one is found
	 * @param isAutomatic
	 */
	static void check(final CommandSender sender, final boolean download, final boolean isAutomatic) {
		stateLock.writeLock().lock();
		try {
			if (state == UpdateState.CHECK_IN_PROGRESS || state == UpdateState.DOWNLOAD_IN_PROGRESS)
				return;
			state = UpdateState.CHECK_IN_PROGRESS;
		} finally {
			stateLock.writeLock().unlock();
		}
		if (!isAutomatic || Skript.logNormal())
			Skript.info(sender, "" + m_checking);
		Skript.newThread(new Runnable() {
			@Override
			public void run() {
				infos.clear();
				
				InputStream in = null;
				try {
					final URLConnection conn = new URL(filesURL).openConnection();
					conn.setRequestProperty("User-Agent", "Skript/v" + Skript.getVersion() + " (by Njol)");
					in = conn.getInputStream();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(in, conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding()));
					try {
						final String line = reader.readLine();
						if (line != null) {
							final JSONArray a = (JSONArray) JSONValue.parse(line);
							for (final Object o : a) {
								final Object name = ((JSONObject) o).get("name");
								if (!(name instanceof String) || !((String) name).matches("\\d+\\.\\d+(\\.\\d+)?( \\(jar( only)?\\))?"))// not the default version pattern to not match beta/etc. versions
									continue;
								final Object url = ((JSONObject) o).get("downloadUrl");
								if (!(url instanceof String))
									continue;
								
								final Version version = new Version(((String) name).contains(" ") ? "" + ((String) name).substring(0, ((String) name).indexOf(' ')) : ((String) name));
								if (version.compareTo(Skript.getVersion()) > 0) {
									infos.add(new VersionInfo((String) name, version, (String) url));
								}
							}
						}
					} finally {
						reader.close();
					}
					
					if (!infos.isEmpty()) {
						Collections.sort(infos);
						latest.set(infos.get(0));
					} else {
						latest.set(null);
					}
					
					getChangelogs(sender);
					
					final String message = infos.isEmpty() ? (Skript.getVersion().isStable() ? "" + m_running_latest_version : "" + m_running_latest_version_beta) : "" + m_update_available;
					if (isAutomatic && !infos.isEmpty()) {
						Skript.adminBroadcast(message);
					} else {
						Skript.info(sender, message);
					}
					
					if (download && !infos.isEmpty()) {
						stateLock.writeLock().lock();
						try {
							state = UpdateState.DOWNLOAD_IN_PROGRESS;
						} finally {
							stateLock.writeLock().unlock();
						}
						download_i(sender, isAutomatic);
					} else {
						stateLock.writeLock().lock();
						try {
							state = UpdateState.CHECKED_FOR_UPDATE;
						} finally {
							stateLock.writeLock().unlock();
						}
					}
				} catch (final IOException e) {
					stateLock.writeLock().lock();
					try {
						state = UpdateState.CHECK_ERROR;
						error.set(ExceptionUtils.toString(e));
						if (sender != null)
							Skript.error(sender, m_check_error.toString());
					} finally {
						stateLock.writeLock().unlock();
					}
				} catch (final Exception e) {
					if (sender != null)
						Skript.error(sender, m_internal_error.toString());
					Skript.exception(e, "Unexpected error while checking for a new version of Skript");
					stateLock.writeLock().lock();
					try {
						state = UpdateState.CHECK_ERROR;
						error.set(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					} finally {
						stateLock.writeLock().unlock();
					}
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (final IOException e) {}
					}
				}
			}
		}, "Skript update thread").start();
	}
	
	/**
	 * Gets the changelogs and release dates of the newest versions
	 * 
	 * @param sender
	 */
	final static void getChangelogs(final CommandSender sender) {
		return;
		InputStream in = null;
		InputStreamReader r = null;
		try {
			final URLConnection conn = new URL(RSSURL).openConnection();
			conn.setRequestProperty("User-Agent", "Skript/v" + Skript.getVersion() + " (by Njol)"); // Bukkit returns a 403 (forbidden) if no user agent is set
			in = conn.getInputStream();
			r = new InputStreamReader(in, conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding());
			final XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(r);
			
			infos.clear();
			VersionInfo current = null;
			
			outer: while (reader.hasNext()) {
				XMLEvent e = reader.nextEvent();
				if (e.isStartElement()) {
					final String element = e.asStartElement().getName().getLocalPart();
					if (element.equalsIgnoreCase("title")) {
						final String name = reader.nextEvent().asCharacters().getData().trim();
						for (final VersionInfo i : infos) {
							if (name.equals(i.name)) {
								current = i;
								continue outer;
							}
						}
						current = null;
					} else if (element.equalsIgnoreCase("description")) {
						if (current == null)
							continue;
						final StringBuilder cl = new StringBuilder();
						while ((e = reader.nextEvent()).isCharacters())
							cl.append(e.asCharacters().getData());
						current.changelog = "- " + StringEscapeUtils.unescapeHtml("" + cl).replace("<br>", "").replace("<p>", "").replace("</p>", "").replaceAll("\n(?!\n)", "\n- ");
					} else if (element.equalsIgnoreCase("pubDate")) {
						if (current == null)
							continue;
						synchronized (RFC2822) { // to make FindBugs shut up
							current.date = new Date(RFC2822.parse(reader.nextEvent().asCharacters().getData()).getTime());
						}
					}
				}
			}
		} catch (final IOException e) {
			stateLock.writeLock().lock();
			try {
				state = UpdateState.CHECK_ERROR;
				error.set(ExceptionUtils.toString(e));
				Skript.error(sender, m_check_error.toString());
			} finally {
				stateLock.writeLock().unlock();
			}
		} catch (final Exception e) {
			Skript.error(sender, m_internal_error.toString());
			Skript.exception(e, "Unexpected error while checking for a new version of Skript");
			stateLock.writeLock().lock();
			try {
				state = UpdateState.CHECK_ERROR;
				error.set(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
			} finally {
				stateLock.writeLock().unlock();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {}
			}
			if (r != null) {
				try {
					r.close();
				} catch (final IOException e) {}
			}
		}
	}
	
	/**
	 * Must set {@link #state} to {@link UpdateState#DOWNLOAD_IN_PROGRESS} prior to calling this
	 * 
	 * @param sender
	 * @param isAutomatic
	 */
	static void download_i(final CommandSender sender, final boolean isAutomatic) {
		assert sender != null;
		stateLock.readLock().lock();
		try {
			if (state != UpdateState.DOWNLOAD_IN_PROGRESS)
				throw new IllegalStateException();
		} finally {
			stateLock.readLock().unlock();
		}
		Skript.info(sender, "" + m_downloading);
//		boolean hasJar = false;
//		ZipInputStream zip = null;
		InputStream in = null;
		try {
			final URLConnection conn = new URL(latest.get().downloadURL).openConnection();
			in = conn.getInputStream();
			assert in != null;
			FileUtils.save(in, new File(Bukkit.getUpdateFolderFile(), "Skript.jar"));
//			zip = new ZipInputStream(conn.getInputStream());
//			ZipEntry entry;
////			boolean hasAliases = false;
//			while ((entry = zip.getNextEntry()) != null) {
//				if (entry.getName().endsWith("Skript.jar")) {
//					assert !hasJar;
//					save(zip, new File(Bukkit.getUpdateFolderFile(), "Skript.jar"));
//					hasJar = true;
//				}// else if (entry.getName().endsWith("aliases.sk")) {
////					assert !hasAliases;
////					saveZipEntry(zip, new File(Skript.getInstance().getDataFolder(), "aliases-" + latest.get().version + ".sk"));
////					hasAliases = true;
////				}
//				zip.closeEntry();
//				if (hasJar)// && hasAliases)
//					break;
//			}
			if (isAutomatic)
				Skript.adminBroadcast("" + m_downloaded);
			else
				Skript.info(sender, "" + m_downloaded);
			stateLock.writeLock().lock();
			try {
				state = UpdateState.DOWNLOADED;
			} finally {
				stateLock.writeLock().unlock();
			}
		} catch (final IOException e) {
			stateLock.writeLock().lock();
			try {
				state = UpdateState.DOWNLOAD_ERROR;
				error.set(ExceptionUtils.toString(e));
				Skript.error(sender, m_download_error.toString());
			} finally {
				stateLock.writeLock().unlock();
			}
		} catch (final Exception e) {
			Skript.exception(e, "Error while downloading the latest version of Skript");
			stateLock.writeLock().lock();
			try {
				state = UpdateState.DOWNLOAD_ERROR;
				error.set(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
			} finally {
				stateLock.writeLock().unlock();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {}
			}
		}
	}
	
	/**
	 * Must only be called if {@link #state} == {@link UpdateState#CHECKED_FOR_UPDATE} or {@link UpdateState#DOWNLOAD_ERROR}
	 * 
	 * @param sender
	 */
	public static void download(final CommandSender sender, final boolean isAutomatic) {
		assert sender != null;
		stateLock.writeLock().lock();
		try {
			if (state != UpdateState.CHECKED_FOR_UPDATE && state != UpdateState.DOWNLOAD_ERROR)
				throw new IllegalStateException("Must check for an update first");
			state = UpdateState.DOWNLOAD_IN_PROGRESS;
		} finally {
			stateLock.writeLock().unlock();
		}
		Skript.newThread(new Runnable() {
			@Override
			public void run() {
				download_i(sender, isAutomatic);
			}
		}, "Skript download thread").start();
	}
	
}
