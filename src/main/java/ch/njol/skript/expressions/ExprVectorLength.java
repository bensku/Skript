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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Length")
@Description("Gets or sets the length of a vector")
@Examples({"send \"%standard length of vector 1, 2, 3%\"",
		"set {_v} to vector 1, 2, 3",
		"set standard length of {_v} to 2",
		"send \"%standard length of {_v}%\""})
@Since("2.2-dev28")
public class ExprVectorLength extends SimplePropertyExpression<Vector, Double> {
	static {
		Skript.registerExpression(ExprVectorLength.class, Double.class, ExpressionType.PROPERTY, "(vector|standard|normal) length of %vector%", "%vector%['s] (vector|standard|normal) length");
	}

	@Override
	@SuppressWarnings({"unused", "null"})
	public Double convert(final Vector vector) {
		if (vector == null) {
			return null;
		}
		return vector.length();
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.SET) {
			return new Class[]{Number.class};
		}
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) {
		assert delta != null;
		final Vector v = getExpr().getSingle(e);
		if (v == null)
			return;
		double n = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (n < 0 && v.lengthSquared() < n * n) {
					v.zero();
				} else {
					double l = n + v.length();
					v.normalize().multiply(l);
				}
				getExpr().change(e, new Vector[]{v}, Changer.ChangeMode.SET);
				break;
			case SET:
				if (n < 0) {
					v.zero();
				} else {
					v.normalize().multiply(n);
				}
				getExpr().change(e, new Vector[]{v}, Changer.ChangeMode.SET);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "length of vector";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
}
