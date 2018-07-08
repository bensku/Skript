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
import ch.njol.util.VectorMath;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Yaw and pitch")
@Description("Gets or sets the yaw or pitch value of a vector")
@Examples({"set {_v} to vector -1, 1, 1",
		"send \"%vector yaw of {_v}%, %vector pitch of {_v}%\"",
		"add 45 to vector yaw of {_v}",
		"subtract 45 from vector pitch of {_v}",
		"send \"%vector yaw of {_v}%, %vector pitch of {_v}%\"",
		"set vector yaw of {_v} to -45",
		"set vector pitch of {_v} to 45",
		"send \"%vector yaw of {_v}%, %vector pitch of {_v}%\"",})
@Since("2.2-dev28")
public class ExprVectorYawPitch extends SimplePropertyExpression<Vector, Number> {
	static {
		Skript.registerExpression(ExprVectorYawPitch.class, Number.class, ExpressionType.PROPERTY,"vector (0¦yaw|1¦pitch) of %vector%");
	}

	private int mark;
	private final static String[] type = new String[] {"yyaw", "ppitch"};

	@Override
	@SuppressWarnings("null")
	public Number convert(Vector vector) {
		if (vector != null) {
			switch (mark) {
				case 0:
					return VectorMath.skriptYaw(VectorMath.getYaw(vector));
				case 1:
					return VectorMath.skriptPitch(VectorMath.getPitch(vector));
				default:
					break;
			}
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return type[mark] + " of vector";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		mark = parseResult.mark;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if ((mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) && getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), Changer.ChangeMode.SET, Vector.class))
			return new Class[] { Number.class };
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, final @Nullable Object[] delta, Changer.ChangeMode mode) {
		Vector v = getExpr().getSingle(e);
		if (v == null){
			return;
		}
		float n = ((Number) delta[0]).floatValue();
		float yaw = VectorMath.getYaw(v);
		float pitch = VectorMath.getPitch(v);
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (mark == 0){
					yaw += n;
				} else if (mark == 1){
					pitch -= n; // Negative because of minecraft's / skript's upside down pitch
				}
				v = VectorMath.fromYawAndPitch(yaw, pitch);
				getExpr().change(e, new Vector[]{v}, Changer.ChangeMode.SET);
				break;
			case SET:
				if (mark == 0){
					yaw = VectorMath.fromSkriptYaw(n);
				} else if (mark == 1){
					pitch = VectorMath.fromSkriptPitch(n);
				}
				v = VectorMath.fromYawAndPitch(yaw, pitch);
				getExpr().change(e, new Vector[]{v}, Changer.ChangeMode.SET);
				break;
			case REMOVE_ALL:
			case DELETE:
			case RESET:
				assert false;
		}
	}
}
