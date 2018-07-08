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
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Create from XYZ")
@Description("Creates a vector from an x, y and z value")
@Examples({"set {_v} to vector 0, 1, 0"})
@Since("2.2-dev28")
public class ExprVectorFromXYZ extends SimpleExpression<Vector> {
	static {
		Skript.registerExpression(ExprVectorFromXYZ.class, Vector.class, ExpressionType.SIMPLE, "[new] vector [(from|at|to)] %number%,[ ]%number%(,[ ]| and )%number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> x, y, z;

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number x = this.x.getSingle(event);
		Number y = this.y.getSingle(event);
		Number z = this.z.getSingle(event);
		if ( x == null || y == null || z == null) {
			return null;
		}
		return new Vector[] {new Vector(x.doubleValue(), y.doubleValue(), z.doubleValue())};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "vector from x " + x.toString() + ", y " + y.toString() + ", z " + z.toString();
	}

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		x = (Expression<Number>) expressions[0];
		y = (Expression<Number>) expressions[1];
		z = (Expression<Number>) expressions[2];
		return true;
	}
}
