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

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.VisualEffect;
import ch.njol.skript.util.VisualEffect.Type;
import de.slikey.effectlib.util.ParticleEffect;
import de.slikey.effectlib.util.ParticleEffect.ParticleData;
import de.slikey.effectlib.util.ParticleEffect.ItemData;
import de.slikey.effectlib.util.ParticleEffect.BlockData;

public class EffectLibUtil {
	
	@SuppressWarnings("deprecation")
	public static void playEffect(final @Nullable Player[] ps, final Location l, final int count, final int radius, final VisualEffect.Type type,
			final @Nullable Object data, float speed, float dX, float dY, float dZ, final @Nullable Color color) {
		ParticleEffect eff = (ParticleEffect) EffectLibHook.ID_MAP.get(type.getMinecraftName());
		Object bukkitData = type.getData(data, l);
		
		ParticleData pData = null;
		if (bukkitData instanceof MaterialData) {
			if (type == Type.ITEM_CRACK)
				pData = new ItemData(((MaterialData) bukkitData).getItemType(), ((MaterialData) bukkitData).getData());
			else
				pData = new BlockData(((MaterialData) bukkitData).getItemType(), ((MaterialData) bukkitData).getData());
		} else if (bukkitData instanceof Material) {
			pData = new BlockData((Material) bukkitData, (byte) 0);
		}
		
		if (ps == null)
			eff.display(pData, l, color, radius, dX, dY, dZ, speed, count);
		else
			eff.display(pData, dX, dY, dZ, speed, count, l, ps);
	}
}
