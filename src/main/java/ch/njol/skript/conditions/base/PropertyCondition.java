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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public abstract class PropertyCondition<T> extends Condition implements Checker<T> {
	
	@SuppressWarnings("null")
	private Expression<? extends T> expr;
	
	/**
	 * @param c
	 * @param property
	 * @param type must be plural
	 */
	public static void register(final Class<? extends Condition> c, final String property, final String type) {
		Skript.registerCondition(c, "%" + type + "% (is|are) " + property, "%" + type + "% (isn't|is not|aren't|are not) " + property);
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = (Expression<? extends T>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public final boolean check(final Event e) {
		return expr.check(e, this, isNegated());
	}
	
	@Override
	public abstract boolean check(T t);
	
	protected abstract String getPropertyName();
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return expr.toString(e, debug) + (expr.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + getPropertyName();
	}
	
}
