/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.VectorMath;

/**
 * @author bi0qaw
 */
@Name("Vectors - Rotate around vector")
@Description("Rotates a vector around another vector")
@Examples({"rotate {_v} around vector 1, 0, 0 by 90"})
@Since("2.2-dev28")
public class EffVectorRotateAroundAnother extends Effect {

	static {
		Skript.registerEffect(EffVectorRotateAroundAnother.class, "rotate %vectors% around %vector% by %number% [degrees]");
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;
	@SuppressWarnings("null")
	private Expression<Number> number;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int i, final Kleenean kleenean, final SkriptParser.ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		number = (Expression<Number>) exprs[2];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected void execute(final Event event) {
		Vector v2 = second.getSingle(event);
		Number n = number.getSingle(event);
		if (v2 == null || n == null) {
			return;
		}
		for (Vector v1 : first.getArray(event)) {
			VectorMath.rot(v1, v2, n.doubleValue());
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "rotate " + first.toString(e, debug) + " around " + second.toString(e, debug);
	}
}
