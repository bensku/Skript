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
package ch.njol.skript.util;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;

@SuppressWarnings("null")
public class SkriptColor extends Color {

	// DyeColor.LIGHT_GRAY on 1.13, DyeColor.SILVER on earlier, so we grab by RGB.
	public static final SkriptColor LIGHT_GREY = new SkriptColor("LIGHT_GREY", DyeColor.getByColor(org.bukkit.Color.fromRGB(0x9D9D97)), ChatColor.GRAY);
	public static final SkriptColor DARK_GREY = new SkriptColor("DARK_GREY", DyeColor.GRAY, ChatColor.DARK_GRAY);
	public static final SkriptColor WHITE = new SkriptColor("WHITE", DyeColor.WHITE, ChatColor.WHITE);
	public static final SkriptColor BLACK = new SkriptColor("BLACK", DyeColor.BLACK, ChatColor.BLACK);
	
	public static final SkriptColor LIGHT_PURPLE = new SkriptColor("LIGHT_PURPLE", DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
	public static final SkriptColor DARK_PURPLE = new SkriptColor("DARK_PURPLE", DyeColor.PURPLE, ChatColor.DARK_PURPLE);
	
	public static final SkriptColor LIGHT_CYAN = new SkriptColor("LIGHT_CYAN", DyeColor.LIGHT_BLUE, ChatColor.AQUA);
	public static final SkriptColor DARK_BLUE = new SkriptColor("DARK_BLUE", DyeColor.BLUE, ChatColor.DARK_BLUE);
	public static final SkriptColor DARK_CYAN = new SkriptColor("DARK_CYAN", DyeColor.CYAN, ChatColor.DARK_AQUA);
	public static final SkriptColor BROWN = new SkriptColor("BROWN", DyeColor.BROWN, ChatColor.BLUE);
	
	public static final SkriptColor DARK_GREEN = new SkriptColor("DARK_GREEN", DyeColor.GREEN, ChatColor.DARK_GREEN);
	public static final SkriptColor LIGHT_GREEN = new SkriptColor("LIGHT_GREEN", DyeColor.LIME, ChatColor.GREEN);
	
	public static final SkriptColor YELLOW = new SkriptColor("YELLOW", DyeColor.YELLOW, ChatColor.YELLOW);
	public static final SkriptColor ORANGE = new SkriptColor("ORANGE", DyeColor.ORANGE, ChatColor.GOLD);
	
	public static final SkriptColor DARK_RED = new SkriptColor("DARK_RED", DyeColor.RED, ChatColor.DARK_RED);
	public static final SkriptColor LIGHT_RED = new SkriptColor("LIGHT_RED", DyeColor.PINK, ChatColor.RED);
	
	final static Map<String, SkriptColor> names = new HashMap<>();
	final static Set<SkriptColor> colors = new HashSet<>();
	public final static String LANGUAGE_NODE = "colors";
	private ChatColor chat;
	private DyeColor dye;

	static {
		for (SkriptColor color : values())
			colors.add(color);
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				names.clear();
				for (SkriptColor color : values()) {
					String node = LANGUAGE_NODE + "." + color.getName();
					color.adjective = new Adjective(node + ".adjective");
					for (String name : Language.getList(node + ".names"))
						names.put(name.toLowerCase(), color);
				}
			}
		});
	}

	@Nullable
	Adjective adjective;
	private final String name;

	private SkriptColor(String name, DyeColor dye, ChatColor chat) {
		this.name = name;
		this.chat = chat;
		this.dye = dye;
	}

	@Override
	public org.bukkit.Color asBukkitColor() {
		return dye.getColor();
	}

	// currently only used by SheepData
	public Adjective getAdjective() {
		return adjective;
	}

	@Override
	public ChatColor asChatColor() {
		return chat;
	}

	@Override
	public DyeColor asDyeColor() {
		return dye;
	}

	@Override
	public String getName() {
		return Streams.findLast(names.entrySet().stream()
				.filter(entry -> entry.getValue().equals(this))
				.map(entry -> entry.getKey()))
				.orElse(name);
	}

	@Override
	public String getFormattedChat() {
		return "" + chat;
	}

	@Deprecated
	@Override
	public byte getWoolData() {
		return dye.getWoolData();
	}

	@Deprecated
	@Override
	public byte getDyeData() {
		return (byte) (15 - dye.getWoolData());
	}
	
	public static SkriptColor[] values() {
		return new SkriptColor[] {LIGHT_GREY, DARK_GREY, WHITE, BLACK, LIGHT_PURPLE, DARK_PURPLE, LIGHT_CYAN, DARK_BLUE, DARK_CYAN, BROWN, DARK_GREEN, LIGHT_GREEN, YELLOW, ORANGE, DARK_RED, LIGHT_RED};
	}
	
	public static Optional<SkriptColor> valueOf(String name) {
		return colors.stream()
				.filter(color -> color.getName().equalsIgnoreCase(name))
				.findAny();
	}

	/**
	 * @param name The name of the color defined by Skript's .lang files.
	 * @return Optional if any Skript Color matched up with the defined name
	 */
	public static Optional<SkriptColor> fromName(String name) {
		return names.entrySet().stream()
				.filter(entry -> entry.getKey().equals(name))
				.map(entry -> entry.getValue())
				.findAny();
	}

	/**
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	public static Optional<SkriptColor> fromDyeColor(DyeColor dye) {
		return colors.stream()
				.filter(color -> color.asDyeColor().equals(dye))
				.findAny();
	}

	/**
	 * @deprecated Magic numbers
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	@Deprecated
	public static Optional<SkriptColor> fromDyeData(short data) {
		if (data < 0 || data >= 16)
			return Optional.empty();
		return colors.stream()
				.filter(color -> color.getWoolData() == 15 - data)
				.findAny();
	}

	/**
	 * @deprecated Magic numbers
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	@Deprecated
	public static Optional<SkriptColor> fromWoolData(short data) {
		if (data < 0 || data >= 16)
			return Optional.empty();
		return colors.stream()
				.filter(color -> color.getWoolData() == data)
				.findAny();
	}

	@Override
	public String toString() {
		return adjective == null ? "" + name : adjective.toString(-1, 0);
	}

}
