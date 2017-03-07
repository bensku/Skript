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
package ch.njol.skript.hooks.particles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.ParticlesPlugin;
import ch.njol.skript.util.VisualEffect;
import ch.njol.skript.util.VisualEffect.Type;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.util.ParticleEffect;

public class EffectLibHook extends ParticlesPlugin<EffectLib> {
	
	public static final Map<String,Object> ID_MAP = new HashMap<>(); // Map of effect name identifiers
	
	public EffectLibHook() throws IOException {}
	
	@Override
	protected boolean init() {
		VisualEffect.EFFECT_LIB = true;
		
		for (ParticleEffect eff : ParticleEffect.values()) {
			ID_MAP.put(eff.getName(), eff);
		}
		
		ParticlesPlugin.plugin = this;
		
		return true; // We don't really initialize anything... for now
	}
	
	@SuppressWarnings("null")
	@Override
	protected void loadClasses() throws IOException {
		Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
	}
	
	@Override
	public String getName() {
		return "EffectLib";
	}
	
	@Override
	public void playEffect(final @Nullable Player[] ps, final Location l, final int count, final int radius, final VisualEffect.Type type,
			final @Nullable Object data, float speed, float dX, float dY, float dZ, final @Nullable Color color) {
		EffectLibUtil.playEffect(ps, l, count, radius, type, data, speed, dX, dY, dZ, color);
	}
}
