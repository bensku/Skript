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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Coordinate")
@Description("Gets or sets the x, y or z coordinate of a vector")
@Examples({"set {_v} to vector 1, 2, 3",
		"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
		"add 1 to x of {_v}",
		"add 2 to y of {_v}",
		"add 3 to z of {_v}",
		"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
		"set x of {_v} to 1",
		"set y of {_v} to 2",
		"set z of {_v} to 3",
		"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",})
@Since("2.2-dev28")
public class ExprVectorXYZ extends SimplePropertyExpression<Vector, Number> {
	private final static String[] axes = {"xx", "yy", "zz"};

	static {
		Skript.registerExpression(ExprVectorXYZ.class, Number.class, ExpressionType.PROPERTY, "(0¦x|1¦y|2¦z) of %vector%");
	}

	private int axis;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}

	@Override
	public Double convert(final Vector v) {
		return axis == 0 ? v.getX() : axis == 1 ? v.getY() : v.getZ();
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if ((mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) && getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), Changer.ChangeMode.SET, Vector.class))
			return new Class[]{Number.class};
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) throws UnsupportedOperationException {
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
				if (axis == 0) {
					v.setX(v.getX() + n);
				} else if (axis == 1) {
					v.setY(v.getY() + n);
				} else {
					v.setZ(v.getZ() + n);
				}
				getExpr().change(e, new Vector[]{v}, Changer.ChangeMode.SET);
				break;
			case SET:
				if (axis == 0) {
					v.setX(n);
				} else if (axis == 1) {
					v.setY(n);
				} else {
					v.setZ(n);
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
		return "the " + axes[axis] + "-coordinate";
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}
}
