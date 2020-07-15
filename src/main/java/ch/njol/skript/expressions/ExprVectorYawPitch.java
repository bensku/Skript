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

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.VectorMath;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author bi0qaw
 */
@Name("Vectors - Yaw and Pitch")
@Description("Gets or sets the yaw or pitch value of a vector.")
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
		register(ExprVectorYawPitch.class, Number.class, "[vector] (0¦yaw|1¦pitch)", "vectors");
	}

	private boolean usesYaw;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		usesYaw = parseResult.mark == 0;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public Number convert(Vector vector) {
		if (vector != null) {
			if (usesYaw)
				return VectorMath.skriptYaw(VectorMath.getYaw(vector));
			else
				return VectorMath.skriptPitch(VectorMath.getPitch(vector));
		}
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
				&& getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class))
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		Vector v = getExpr().getSingle(e);
		if (v == null)
			return;
		float n = ((Number) delta[0]).floatValue();
		float yaw = VectorMath.getYaw(v);
		float pitch = VectorMath.getPitch(v);
		switch (mode) {
			case ADD:
				if (usesYaw)
					yaw += n;
				else
					pitch -= n; // Negative because of Minecraft's / Skript's upside down pitch
				v = VectorMath.fromYawAndPitch(yaw, pitch);
				getExpr().change(e, new Vector[]{v}, ChangeMode.SET);
				break;
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case SET:
				if (usesYaw)
					yaw = VectorMath.fromSkriptYaw(n);
				else
					pitch = VectorMath.fromSkriptPitch(n);
				v = VectorMath.fromYawAndPitch(yaw, pitch);
				getExpr().change(e, new Vector[]{v}, ChangeMode.SET);
		}
	}

	@Override
	protected String getPropertyName() {
		return usesYaw ? "yaw" : "pitch";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
