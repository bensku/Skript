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
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.bukkitutil.EnchantmentIds;
import ch.njol.yggdrasil.Fields;

public class ColorRGB extends Color {

	private final org.bukkit.Color bukkit;
	private final DyeColor dye;
	private ChatColor chat;

	@SuppressWarnings("null")
	public ColorRGB(int red, int green, int blue) {
		this.bukkit = org.bukkit.Color.fromBGR(blue, green, red);
		this.dye = DyeColor.getByColor(bukkit);
		Optional<SkriptColor> color = SkriptColor.fromDyeColor(dye);
		if (color.isPresent())
			this.chat = color.get().asChatColor();
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

	@Override
	public String getName() {
		return "RED:" + bukkit.getRed() + ", GREEN:" + bukkit.getGreen() + ", BLUE" + bukkit.getBlue();
	}

}
