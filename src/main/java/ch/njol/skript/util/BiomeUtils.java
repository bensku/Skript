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
package ch.njol.skript.util;

import ch.njol.skript.bukkitutil.BiomeMappings;
import ch.njol.skript.localization.Language;

import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class BiomeUtils {
	private BiomeUtils() {}
	
	@Nullable
	public static Biome parse(final String s) {
		return BiomeMappings.parse(s);
	}
	
	public static String toString(final Biome b, final int flags) {
		return BiomeMappings.toString(b, flags);
	}
	
	public static String getAllNames() { // This is hack for class loading order...
		return "Biome names; you can use F3 ingame";
		//return BiomeMappings.getAllNames();
	}
	
}
