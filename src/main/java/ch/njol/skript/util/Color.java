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

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import ch.njol.skript.localization.Adjective;
import ch.njol.yggdrasil.YggdrasilSerializable;

public interface Color extends YggdrasilSerializable {
	
	/**
	 * @return The Bukkit Color of this Color.
	 */
	org.bukkit.Color asBukkitColor();
	
	/**
	 * @return The Bukkit ChatColor of this Color.
	 */
	ChatColor asChatColor();
	
	/**
	 * @return The Bukkkit DyeColor of this Color.
	 */
	DyeColor asDyeColor();
	
	/**
	 * @return The name of the Skript Color.
	 */
	String getName();
	
	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The wool byte data of this color
	 */
	@Deprecated
	byte getWoolData();
	
	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The dye byte of this color
	 */
	@Deprecated
	byte getDyeData();

	/**
	 * @return The ChatColor but formated. Must be returned as a ChatColor String.
	 */
	String getFormattedChat();
}
