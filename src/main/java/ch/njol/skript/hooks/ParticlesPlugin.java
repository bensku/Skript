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
package ch.njol.skript.hooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.particles.EffectLibHook;
import ch.njol.skript.util.VisualEffect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.util.ParticleEffect;
import de.slikey.effectlib.util.ParticleEffect.BlockData;
import de.slikey.effectlib.util.ParticleEffect.ItemData;
import de.slikey.effectlib.util.ParticleEffect.ParticleData;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import ch.njol.skript.util.VisualEffect.Type;

/**
 *	Hook for better particle effects.
 */
public abstract class ParticlesPlugin<P extends Plugin> extends Hook<P> {
	
	public ParticlesPlugin() throws IOException {}
	
	@Nullable
	public static ParticlesPlugin<?> plugin;
	
	public abstract void playEffect(final @Nullable Player[] ps, final Location l, final int count, final int radius, final VisualEffect.Type type,
			final @Nullable Object data, float speed, float dX, float dY, float dZ, final @Nullable Color color);
}
