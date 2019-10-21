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

import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.variables.Variables;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.Fields;

public class ColorRGB implements Color {
	
	private org.bukkit.Color bukkit;
	@Nullable
	private DyeColor dye;
	
	public ColorRGB(int red, int green, int blue) {
		this.bukkit = org.bukkit.Color.fromRGB(
			Math2.fit(0, red, 255),
			Math2.fit(0, green, 255),
			Math2.fit(0, blue, 255));
		this.dye = DyeColor.getByColor(bukkit);
	}
	
	@SuppressWarnings("null")
	@Override
	public org.bukkit.Color asBukkitColor() {
		return bukkit;
	}
	
	@Override
	@Nullable
	public DyeColor asDyeColor() {
		return dye;
	}
	
	@Override
	public String getName() {
		return "RED:" + bukkit.getRed() + ", GREEN:" + bukkit.getGreen() + ", BLUE" + bukkit.getBlue();
	}
	
	
	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this, Variables.yggdrasil);
	}
	
	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		org.bukkit.Color b = fields.getObject("bukkit", org.bukkit.Color.class);
		DyeColor d = fields.getObject("dye", DyeColor.class);
		if (b == null)
			return;
		if (d == null)
			dye = DyeColor.getByColor(b);
		else
			dye = d;
		bukkit = b;
	}
}
