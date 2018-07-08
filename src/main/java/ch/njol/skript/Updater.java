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
package ch.njol.skript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ch.njol.skript.Updater.ResponseEntry;
import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Version;

/**
 * Skript's new updater, which uses Github API.
 */
public class Updater {
	
	public static final String RELEASES_URL  = "https://api.github.com/repos/SkriptLang/Skript/releases";
	
	@Nullable
	private static Gson gson;
	private static boolean gsonUnavailable;
	
	static {
		// Only initialize GSON if available
		if (Skript.classExists("com.google.gson.Gson"))
			gson = new Gson();
		else
			gsonUnavailable = true;
	}
	
	final static AtomicReference<String> error = new AtomicReference<>();
	public static volatile UpdateState state = UpdateState.NOT_STARTED;
	
	public final static List<ResponseEntry> infos = new ArrayList<>();
	public final static AtomicReference<ResponseEntry> latest = new AtomicReference<>();
	
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
	public final static Message m_custom_version = new Message("updater.custom version");
	
	@Nullable
	static Task checkerTask;
	
	public final static AtomicReference<CommandSender> executor = new AtomicReference<>();
	
	public enum UpdateState {
		
		NOT_STARTED,
		
		CHECKING,
		
		RUNNING_LATEST,
		
		RUNNING_CUSTOM,
		
		UPDATE_AVAILABLE,
		
		DOWNLOADING,
		
		DOWNLOADED,
		
		ERROR
	}
	
	/**
	 * Github API response for GSON deserialization.
	 */
	@NonNullByDefault(value = false)
	public class ResponseEntry {
		public String url;
	    public String assets_url;
	    public String upload_url;
	    public String html_url;
	    public int id;
	    public String tag_name;
	    public String target_commitish;
	    public String name;
	    public boolean draft;
	    
	    public boolean prerelease;
	    public String created_at;
	    public String published_at;
	    
	    public class AssetsEntry {
	    	public int size;
	    	public int download_count;
	    	public String browser_download_url;
	    }
	    
	    public List<AssetsEntry> assets;
	    public String body; // Description of release
	    
	    @Override
	    public String toString() {
	    	return tag_name;
	    }
	    
	    public class Author {
	    	public String login;
	    	public int id;
	    }
	    
	    public Author author;
	}
	
	@SuppressWarnings("null")
	public static void start() {
		Skript.debug("Initializing updater");
		
		if (gsonUnavailable) // Something wrong with GSON...
			return;
		
		long period;
		if (SkriptConfig.checkForNewVersion.value())
			period = SkriptConfig.updateCheckInterval.value().getTicks_i();
		else
			period = -1;
		if (checkerTask == null || !checkerTask.isAlive())
			checkerTask = new CheckerTask(Skript.getInstance(), period);
		Skript.info("Starting updater thread");
		checkerTask.setNextExecution(0); // Execute it now!
	}
	
	public static List<ResponseEntry> deserialize(String str) {
		assert str != null : "Cannot deserialize null string";
		@SuppressWarnings("serial")
		Type listType = new TypeToken<List<ResponseEntry>>() {}.getType();
		assert gson != null;
		List<ResponseEntry> responses = gson.fromJson(str, listType);
		assert responses != null;
		
		return responses;
	}
	
	public static class CheckerTask extends Task {
		
		/**
		 * @param plugin
		 * @param delay
		 */
		public CheckerTask(Plugin plugin, long period) {
			super(plugin, 1, period, true); // This is asyncronous task
			CommandSender sender = executor.get();
			if (sender == null)
				sender = Bukkit.getConsoleSender();
			assert sender != null;
			this.sender = sender;
		}

		private CommandSender sender;
		
		public String tryLoadReleases(URL url) throws IOException, SocketTimeoutException {
			//Skript.debug("Trying to load releases from " + url + "...");
			Scanner scan = null;
			try {
				scan = new Scanner(url.openStream(), "UTF-8");
				String out = scan.useDelimiter("\\A").next();
				if (out == null)
					throw new IOException("Null output from scanner!");
				return out;
			} finally {
				//Skript.debug("Closing scanner NOW!");
				if (scan != null)
					scan.close();
			}
		}
		
		public boolean performUpdate(List<ResponseEntry> releases) {
			ResponseEntry current = null;
			String ver = Skript.getInstance().getDescription().getVersion();
			boolean allowPrereleases = SkriptConfig.updateToPrereleases.value();
			ResponseEntry update = null;
			for (ResponseEntry release : releases) {
				if (ver.endsWith(release.tag_name)) {
					Skript.debug("Found current release: " + release);
					current = release;
					break;
				}
				if (update == null && (allowPrereleases || !release.prerelease))
					update = release; // If update is not found and pre-release rules are matched: set update
			}
			if (current == null) { // Non-baseline version. Fail gracefully!
				state = UpdateState.RUNNING_CUSTOM;
				return false;
			}
			if (update == null) { // No update available
				state = UpdateState.RUNNING_LATEST;
				return false;
			}
			
			latest.set(update);
			infos.clear();
			infos.addAll(releases);
			return true;
		}
		
		@Override
		public void run() {
			//Skript.debug("Beginning update checking");
			state = UpdateState.CHECKING;
			
			URL url = null;
			try {
				url = new URL(RELEASES_URL); // Create URL
			} catch (MalformedURLException e) {
				Skript.info(sender, "" + m_internal_error);
				e.printStackTrace();
				return;
			}
			assert url != null;
			
			int maxTries = SkriptConfig.updaterDownloadTries.value();
			int tries = 0;
			String response = null;
			while (response == null) {
				try {
					response = tryLoadReleases(url);
				} catch (SocketTimeoutException e) {
					//Skript.debug("Socket timeout in updater, but we can probably try again!");
					// Do nothing here, we'll just try again...
				} catch (IOException e) {
					error.set(ExceptionUtils.toString(e));
					Skript.info(sender, "" + m_check_error);
				}
				
				tries++;
				if (tries >= maxTries && response == null) {
					error.set("Can't reach update server");
					Skript.info(sender, "" + m_check_error);
					state = UpdateState.ERROR;
					return;
				}
			}
			assert response != null;
			
			List<ResponseEntry> entries = deserialize(response);
			
			if (performUpdate(entries)) { // Check if we're running latest release...
				state = UpdateState.UPDATE_AVAILABLE;
				infos.addAll(entries);
				latest.set(entries.get(0));
				
				Skript.info(sender, "" + m_update_available);
				if (SkriptConfig.automaticallyDownloadNewVersion.value()) {
					// TODO automatic downloading
				}
			} else {
				switch (state) {
					case RUNNING_LATEST:
						Skript.info(sender, "" + m_running_latest_version);
						break;
					case RUNNING_CUSTOM:
						Skript.info(sender, "" + m_custom_version);
						break;
						//$CASES-OMITTED$
					default:
						Skript.error(sender, "" + m_internal_error);
						Thread.dumpStack();
				}
			}
		}
	}
	
	public static class DownloaderTask extends Task {
		
		/**
		 * @param plugin
		 * @param delay
		 */
		public DownloaderTask(Plugin plugin) {
			super(plugin, 0, true); // This is asyncronous task
			CommandSender sender = executor.get();
			if (sender == null)
				sender = Bukkit.getConsoleSender();
			assert sender != null;
			this.sender = sender;
		}

		private CommandSender sender;
		
		@Override
		public void run() {
			ResponseEntry update = latest.get();
			URL url;
			try {
				url = new URL(update.assets.get(0).browser_download_url);
			} catch (MalformedURLException e) { // This happens if Github API is broken
				Skript.info(sender, "" + m_internal_error);
				e.printStackTrace();
				return;
			}
			assert url != null;
			
			// Validate new release (only bensku can make auto-updateable releases for security reasons)
			if (update.author.id != 4330456) {
				Skript.exception("Unauthorized Skript release! Author " + update.author.login + " (" + update.author.id + ") is not recognized.");
				return;
			}
			
			// Attempt to open connection on new jar
			int maxTries = SkriptConfig.updaterDownloadTries.value();
			int tries = 0;
			ReadableByteChannel ch = null;
			while (ch == null) {
				try {
					ch = Channels.newChannel(url.openStream());
				} catch (SocketTimeoutException e) {
					Skript.debug("Socket timeout in updater, but we can probably try again!");
					// Do nothing here, we'll just try again...
				} catch (IOException e) {
					error.set(ExceptionUtils.toString(e));
					Skript.info(sender, "" + m_check_error);
				}
				
				
				tries++;
				if (tries >= maxTries && ch == null) {
					error.set("Can't reach update server");
					Skript.info(sender, "" + m_check_error);
					state = UpdateState.ERROR;
					return;
				}
			}
			assert ch != null;
			
			// Attempt to transfer data from network to file
			try {
				FileChannel file = FileChannel.open(Paths.get("skript-update.jar"), StandardOpenOption.CREATE);
				file.transferFrom(ch, 0, 1024 * 1024 * 10); // 10 mb is max file size for safety reasons
				file.close();
			} catch (IOException e) {
				error.set(ExceptionUtils.toString(e));
				Skript.info(sender, "" + m_check_error);
			}
			
			// Close connection, no matter what
			try {
				ch.close();
			} catch (IOException e) {
				Skript.exception(e);
			}
		}
	}
}
