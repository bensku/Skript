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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Config;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Version;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public class Language {
	
	/**
	 * Some flags
	 */
	public static final int F_PLURAL = 1, F_DEFINITE_ARTICLE = 2, F_INDEFINITE_ARTICLE = 4;
	
	/**
	 * masks out article flags - useful if the article has been added already (e.g. by an adjective)
	 */
	public static final int NO_ARTICLE_MASK = ~(F_DEFINITE_ARTICLE | F_INDEFINITE_ARTICLE);
	
	/**
	 * Name of the localised language
	 */
	private static String name = "english";

	private static final HashMap<String, String> english = new HashMap<>();

	@Nullable
	private static HashMap<String, String> localized = null;
	
	private static final HashMap<Plugin, Version> langVersion = new HashMap<>();
	
	public static String getName() {
		return name;
	}
	
	@Nullable
	private static String get_i(String key) {
		if (localized != null) {
			String s = localized.get(key);
			if (s != null)
				return s;
		}
		String s = english.get(key);
		if (s == null && Skript.testing())
			missingEntryError(key);
		return s;
	}
	
	/**
	 * Gets a string from the language file with the given key, or the english variant if the string is missing from the chosen language's file, or the key itself if the key does
	 * not exist.
	 * 
	 * @param key The message's key (case-insensitive)
	 * @return The requested message if it exists or the key otherwise
	 */
	public static String get(String key) {
		String s = get_i("" + key.toLowerCase(Locale.ENGLISH));
		return s == null ? "" + key.toLowerCase(Locale.ENGLISH) : s;
	}
	
	/**
	 * Equal to {@link #get(String)}, but returns null instead of the key if the key cannot be found.
	 * 
	 * @param key The message's key (case-insensitive)
	 * @return The requested message or null if it doesn't exist
	 */
	@Nullable
	public static String get_(String key) {
		return get_i("" + key.toLowerCase(Locale.ENGLISH));
	}
	
	public static void missingEntryError(String key) {
		Skript.error("Missing entry '" + key.toLowerCase(Locale.ENGLISH) + "' in the default english language file");
	}
	
	/**
	 * Gets a string and uses it as format in {@link String#format(String, Object...)}.
	 *
	 * @param args The arguments to pass to {@link String#format(String, Object...)}
	 * @return The formatted string
	 */
	public static String format(String key, Object... args) {
		key = "" + key.toLowerCase(Locale.ENGLISH);
		String value = get_i(key);
		if (value == null)
			return key;
		try {
			return "" + String.format(value, args);
		} catch (Exception e) {
			Skript.error("Invalid format string at '" + key + "' in the " + getName() + " language file: " + value);
			return key;
		}
	}
	
	/**
	 * Gets a localized string surrounded by spaces, or a space if the string is empty
	 *
	 * @return The message surrounded by spaces, a space if the entry is empty, or " "+key+" " if the entry is missing.
	 */
	public static String getSpaced(String key) {
		String s = get(key);
		if (s.isEmpty())
			return " ";
		return " " + s + " ";
	}
	
	@SuppressWarnings("null")
	private static final Pattern listSplitPattern = Pattern.compile("\\s*,\\s*");
	
	/**
	 * Gets a list of strings.
	 *
	 * @return a non-null String array with at least one element
	 */
	public static String[] getList(String key) {
		String s = get_i("" + key.toLowerCase(Locale.ENGLISH));
		if (s == null)
			return new String[] {key.toLowerCase(Locale.ENGLISH)};
		String[] r = listSplitPattern.split(s);
		assert r != null;
		return r;
	}
	
	/**
	 * @return Whether the given key exists in the <b>english</b> language file.
	 */
	public static boolean keyExists(String key) {
		return english.containsKey(key.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * @return whether the default English language is loaded.
	 */
	public static boolean isInitialized() {
		return !english.isEmpty();
	}

	/**
	 * Get if Skript uses the Localized language instead of English.
	 * @return True if Skript uses the Localized language elements, false otherwise
	 */
	public static boolean isUsingLocalizedLanguage() {
		return localized != null;
	}

	public static void loadDefault(SkriptAddon addon) {
		if (addon.getLanguageFileDirectory() == null)
			return;

		InputStream din = addon.plugin.getResource(addon.getLanguageFileDirectory() + "/english.lang");
		if (din == null)
			throw new IllegalStateException(addon + " is missing the required english.lang file!");

		HashMap<String, String> en;
		try {
			en = new Config(din, "english.lang", false, false, ":").toMap(".");
		} catch (Exception e) {
			throw Skript.exception(e, "Could not load " + addon + "'s default language file!");
		} finally {
			try {
				din.close();
			} catch (IOException ignored) {}
		}

		String v = en.get("version");
		if (v == null)
			Skript.warning("Missing version in english.lang");

		langVersion.put(addon.plugin, v == null ? Skript.getVersion() : new Version(v));
		en.remove("version");
		english.putAll(en);
		for (LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	public static boolean load(String name) {
		name = "" + name.toLowerCase();
		if (name.equals("english"))
			return true;

		localized = new HashMap<>();
		boolean exists = load(Skript.getAddonInstance(), name);
		for (SkriptAddon addon : Skript.getAddons()) {
			exists |= load(addon, name);
		}
		if (!exists) {
			localized = null;
			Language.name = "english";
			return false;
		}
		Language.name = name;

		for (LanguageChangeListener l : listeners)
			l.onLanguageChange();

		return true;
	}
	
	private static boolean load(SkriptAddon addon, String name) {
		if (addon.getLanguageFileDirectory() == null)
			return false;
		HashMap<String, String> l = load(addon.plugin.getResource(addon.getLanguageFileDirectory() + "/" + name + ".lang"), name);
		File f = new File(addon.plugin.getDataFolder(), addon.getLanguageFileDirectory() + File.separator + name + ".lang");
		try {
			if (f.exists())
				l.putAll(load(new FileInputStream(f), name));
		} catch (FileNotFoundException e) {
			assert false;
		}
		if (l.isEmpty())
			return false;
		if (!l.containsKey("version")) {
			Skript.error(addon + "'s language file " + name + ".lang does not provide a version number!");
		} else {
			try {
				Version v = new Version("" + l.get("version"));
				Version lv = langVersion.get(addon.plugin);
				assert lv != null; // set in loadDefault()
				if (v.isSmallerThan(lv))
					Skript.warning(addon + "'s language file " + name + ".lang is outdated, some messages will be english.");
			} catch (IllegalArgumentException e) {
				Skript.error("Illegal version syntax in " + addon + "'s language file " + name + ".lang: " + e.getLocalizedMessage());
			}
		}
		l.remove("version");
		if (localized != null)
			localized.putAll(l);
		else
			assert false : addon + "; " + name;
		return true;
	}
	
	private static HashMap<String, String> load(@Nullable InputStream in, String name) {
		if (in == null)
			return new HashMap<>();
		try (in) {
			return new Config(in, name + ".lang", false, false, ":").toMap(".");
		} catch (IOException e) {
			Skript.exception(e, "Could not load the language file '" + name + ".lang': " + ExceptionUtils.toString(e));
			return new HashMap<>();
		}
	}

	private static final List<LanguageChangeListener> listeners = new ArrayList<>();
	
	public enum LanguageListenerPriority {
		EARLIEST, NORMAL, LATEST;
	}
	
	private static final int[] priorityStartIndices = new int[LanguageListenerPriority.values().length];
	
	/**
	 * Registers a listener. The listener will immediately be called if a language has already been loaded.
	 * 
	 * @param listener the listener to register
	 */
	public static void addListener(LanguageChangeListener listener) {
		addListener(listener, LanguageListenerPriority.NORMAL);
	}
	
	public static void addListener(LanguageChangeListener listener, LanguageListenerPriority priority) {
		listeners.add(priorityStartIndices[priority.ordinal()], listener);
		for (int i = priority.ordinal() + 1; i < LanguageListenerPriority.values().length; i++)
			priorityStartIndices[i]++;

		if (isInitialized()) {
			listener.onLanguageChange();
		}
	}

}
