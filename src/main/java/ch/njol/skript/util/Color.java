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

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Serializer;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

public abstract class Color implements YggdrasilSerializable {

	/**
	 * @return The Bukkit Color of this Color.
	 */
	public abstract org.bukkit.Color asBukkitColor();

	/**
	 * @return The ChatColor but formated. Must be returned as a ChatColor String.
	 */
	public abstract String getFormattedChat();

	/**
	 * @return The Bukkit ChatColor of this Color.
	 */
	public abstract ChatColor asChatColor();

	/**
	 * @return The Bukkkit DyeColor of this Color.
	 */
	public abstract DyeColor asDyeColor();

	/**
	 * @return The name of the Skript Color.
	 */
	public abstract String getName();

	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The wool byte data of this color
	 */
	@Deprecated
	public abstract byte getWoolData();

	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The dye byte of this color
	 */
	@Deprecated
	public abstract byte getDyeData();

}
