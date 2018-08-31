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
package ch.njol.skript.conditions.base;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;

public abstract class PossessionCondition<T> extends SingleCondition<T> {
	
	/**
	 * @param c
	 * @param property
	 * @param type must be plural
	 */
	public static void register(final Class<? extends Condition> c, final String property, final String type) {
		Skript.registerCondition(c,
				"%" + type + "% (has|have) " + property,
				"%" + type + "% (doesn't|does not|do not|don't) have " + property);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (expr.isSingle())
			return expr.toString(e, debug) + (isNegated() ? " doesn't have " : " has ") + getPropertyName();
		else
			return expr.toString(e, debug) + (isNegated() ? " don't have " : " have ") + getPropertyName();
	}
	
}
