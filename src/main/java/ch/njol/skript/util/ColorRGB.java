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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.yggdrasil.YggdrasilSerializable;

public class ColorRGB implements Color {
	
	private org.bukkit.Color bukkit;
	private ChatColor chat;
	private DyeColor dye;
	
	private ColorRGB(DyeColor dye, ChatColor chat, org.bukkit.Color bukkit) {
		this.bukkit = bukkit;
		this.chat = chat;
		this.dye = dye;
	}
	
	@SuppressWarnings("null")
	@Override
	public org.bukkit.Color asBukkitColor() {
		return dye.getColor();
	}
	
	@Override
	public String getFormattedChat() {
		return "" + chat;
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
		return "RED:" + bukkit.getRed() + ", GREEN:" + bukkit.getGreen() + ", BLUE" + bukkit.getBlue();
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
	
	/**
	 * @param name The name of the color defined by Skript's .lang files.
	 * @return Optional if any Skript Color matched up with the defined name
	 */
	@Nullable
	public static org.bukkit.Color from(int red, int green, int blue) {
		return org.bukkit.Color.fromBGR(blue, green, red);
	}
	
}
