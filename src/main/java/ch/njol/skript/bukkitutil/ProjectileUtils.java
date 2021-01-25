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
package ch.njol.skript.bukkitutil;

import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
public abstract class ProjectileUtils {
	private ProjectileUtils() {}
	
	@Nullable
	public static Object getShooter(final @Nullable Projectile p) {
		if (p == null)
			return null;
		return p.getShooter();
	}
	
	public static void setShooter(final Projectile p, final @Nullable Object shooter) {
		p.setShooter((ProjectileSource) shooter);
	}
	
}
