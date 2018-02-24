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
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Rotate around XYZ")
@Description("Rotates a vector around x, y, or z axis by some degrees")
@Examples({"rotate {_v} around x-axis by 90",
		"rotate {_v} around y-axis by 90",
		"rotate {_v} around z-axis by 90"})
@Since("2.2-dev28")
public class EffVectorRotateXYZ extends Effect {
	static {
		Skript.registerEffect(EffVectorRotateXYZ.class, "rotate %vectors% around (1¦x|2¦y|3¦z)(-| )axis by %number% [degrees]");
	}

	private final static Character[] axes = new Character[]{'x', 'y', 'z'};

	@SuppressWarnings("null")
	private Expression<Vector> vectors;
	@SuppressWarnings("null")
	private Expression<Number> number;
	private int mark;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(final Expression<?>[] exprs, int i, final Kleenean kleenean, final SkriptParser.ParseResult parseResult) {
		vectors = (Expression<Vector>) exprs[0];
		number = (Expression<Number>) exprs[1];
		mark = parseResult.mark;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected void execute(final Event event) {
		Number n = number.getSingle(event);
		if (n == null) {
			return;
		}
		switch (mark) {
			case 1:
				for (Vector v : vectors.getAll(event)) {
					VectorMath.rotX(v, n.doubleValue());
				}
				break;
			case 2:
				for (Vector v : vectors.getAll(event)) {
					VectorMath.rotY(v, n.doubleValue());
				}
				break;
			case 3:
				for (Vector v : vectors.getAll(event)) {
					VectorMath.rotZ(v, n.doubleValue());
				}
				break;
		}
	}

	@Override
	public String toString(final @Nullable Event event, final boolean b) {
		return "rotate " + vectors.toString() + " around " + axes[mark] + "-axis";
	}
}
