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
package ch.njol.skript.hooks.biomes;

import java.io.IOException;

import org.bukkit.block.Biome;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.biomes.BiomeMapUtil.To19Mapping;
import ch.njol.skript.util.EnumUtils;
import ch.njol.skript.util.VisualEffect;

/**
 * Hook for multi version biomes.
 */
public class BiomeHook extends Hook<Skript> {
	
	@SuppressWarnings("null")
	public static BiomeHook instance;
	@SuppressWarnings("null")
	public static EnumUtils<To19Mapping> util19; // Can be null, but doesn't matter; when used, always not null
	// SO, be careful when using this

	public BiomeHook() throws IOException {}
	
	@Override
	protected boolean init() {
		instance = this;
		
		return true;
	}

	@Override
	public String getName() {
		return "Skript";
	}
	
	@SuppressWarnings("null")
	@Override
	protected void loadClasses() throws IOException {
		if (Skript.isRunningMinecraft(1, 9)) {// Load only if running 1.9+
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
			util19 = new EnumUtils<>(To19Mapping.class, "biomes");
		}
	}
	
}
