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
package ch.njol.skript.bukkitutil;

import java.util.Arrays;

import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.biomes.BiomeHook;
import ch.njol.skript.util.EnumUtils;

/**
 * 1.8 to 1.9 biome name mappings. 1.9 names make no sense.
 * Should be 1.8 compatible, if it's not there is a bug.
 * @author bensku
 */
public abstract class BiomeMappings {
	
	private final static EnumUtils<Biome> util = new EnumUtils<>(Biome.class, "biomes");
	
	private final static boolean mapFor19 = Skript.isRunningMinecraft(1, 9);
	
	@SuppressWarnings("null")
	public static @Nullable Biome parse(final String name) {
		if (!mapFor19) return util.parse(name);
		
		Biome mapping = Biome.valueOf(name);
		return mapping != null ? mapping : null;
	}
	
	@SuppressWarnings("null")
	public static String toString(final Biome biome, final int flags) {
		return biome.toString();
	}
	
	@SuppressWarnings("null")
	public static String getAllNames() {
		return Arrays.toString(Biome.values());
	}
}