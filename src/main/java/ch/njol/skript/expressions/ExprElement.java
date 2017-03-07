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
package ch.njol.skript.expressions;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Element of")
@Description({"The first, last or a random element of a set, e.g. a list variable.",
		"See also: <a href='#ExprRandom'>random</a>"})
@Examples("give a random element out of {free items::*} to the player")
@Since("2.0")
public class ExprElement extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprElement.class, Object.class, ExpressionType.PROPERTY, "(-1¦[the] first|1¦[the] last|0¦[a] random) element [out] of %objects%");
	}
	
	private int element;
	
	@SuppressWarnings("null")
	private Expression<?> expr;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		element = parseResult.mark;
		return true;
	}
	
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		final Object o;
		if (element == -1) {
			final Iterator<?> iter = expr.iterator(e);
			if (iter == null || !iter.hasNext())
				return null;
			o = iter.next();
		} else if (element == 1) {
			final Object[] os = expr.getArray(e);
			if (os.length == 0)
				return null;
			o = os[os.length - 1];
		} else {
			final Object[] os = expr.getArray(e);
			if (os.length == 0)
				return null;
			o = CollectionUtils.getRandom(os);
		}
		final Object[] r = (Object[]) Array.newInstance(getReturnType(), 1);
		r[0] = o;
		return r;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (element == 0 ? "a " : "the ") + (element == -1 ? "first" : element == 1 ? "last" : "random") + " element of " + expr.toString(e, debug);
	}
	
}
