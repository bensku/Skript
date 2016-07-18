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

import ch.njol.skript.util.VisualEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;


/**
 * Hook for better particle effects.
 */
public abstract class ParticlesPlugin<P extends Plugin> extends Hook<P> {

    @Nullable
    public static ParticlesPlugin<?> plugin;

    public ParticlesPlugin() throws IOException {
    }

    public abstract void playEffect(final @Nullable Player[] ps, final Location l, final int count, final int radius, final VisualEffect.Type type,
                                    final @Nullable Object data, float speed, float dX, float dY, float dZ, final @Nullable Color color);
}
