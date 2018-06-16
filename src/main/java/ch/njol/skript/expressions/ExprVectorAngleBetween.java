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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.VectorMath;

/**
 * @author bi0qaw
 */
@Name("Vectors - Angle between")
@Description("Gets the angle between two vectors")
@Examples({"send \"%angle between vector 1, 0, 0 and vector 0, 1, 1%\""})
@Since("2.2-dev28")
public class ExprVectorAngleBetween extends SimpleExpression<Float> {
	
	static {
		Skript.registerExpression(ExprVectorAngleBetween.class, Float.class, ExpressionType.SIMPLE, "angle between %vector% and %vector%");
	}
	
	@SuppressWarnings("null")
	private Expression<Vector> first, second;
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "angle between " + first.toString() + " and " + second.toString();
	}
	
	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		first = (Expression<Vector>) expressions[0];
		second = (Expression<Vector>) expressions[1];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected Float[] get(Event event) {
		Vector v1 = first.getSingle(event);
		Vector v2 = second.getSingle(event);
		if (v1 == null || v2 == null) {
			return null;
		}
		return new Float[]{v1.angle(v2) * (float) VectorMath.RAD_TO_DEG};
	}
}
