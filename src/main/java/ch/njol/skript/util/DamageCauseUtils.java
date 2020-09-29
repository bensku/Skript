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

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class DamageCauseUtils {
	private DamageCauseUtils() {}
	
	private final static EnumUtils<DamageCause> util = new EnumUtils<>(DamageCause.class, "damage causes");
	
	@Nullable
	public static DamageCause parse(final String s) {
		return util.parse(s);
	}
	
	public static String toString(final DamageCause dc, final int flags) {
		return util.toString(dc, flags);
	}
	
	public static String getAllNames() {
		return util.getAllNames();
	}
	
}
