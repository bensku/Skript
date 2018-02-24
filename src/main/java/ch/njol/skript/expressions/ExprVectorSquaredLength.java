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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.util.Vector;

/**
 * @author bi0qaw
 */
@Name("Vectors - Squared length")
@Description("Gets the squared length of a vector")
@Examples({"send \"%squared length of vector 1, 2, 3%\""})
@Since("2.2-dev28")
public class ExprVectorSquaredLength extends SimplePropertyExpression<Vector, Double> {
	static {
		Skript.registerExpression(ExprVectorSquaredLength.class, Double.class, ExpressionType.SIMPLE,
				"squared length of %vector%", "%vector%['s] squared length");
	}

	@SuppressWarnings({"null", "unused"})
	@Override
	public Double convert(final Vector vector) {
		if (vector == null) return null;
		return vector.lengthSquared();
	}

	@Override
	protected String getPropertyName() {
		return "squared length of vector";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}
}
