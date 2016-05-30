/*
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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.hooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.njol.skript.util.VisualEffect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.util.ParticleEffect;

/**
 *	Hook for better particle effects.
 */
public class EffectLibHook extends Hook<EffectLib> {
	
	public EffectLibHook() throws IOException {}
	
	public static final Map<Integer,ParticleEffect> ID_MAP = new HashMap<Integer,ParticleEffect>();
	
	@Override
	protected boolean init() {
		VisualEffect.EFFECT_LIB = true;
		
		for (ParticleEffect eff : ParticleEffect.values()) {
			ID_MAP.put(eff.getId(), eff);
		}
		
		return true; // We don't really initialize anything... for now
	}
	
	@Override
	protected void loadClasses() throws IOException {
		
	}
	
	@Override
	public String getName() {
		return "EffectLib";
	}
}
