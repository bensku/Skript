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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

/**
 * @author Sashie
 */
@Name("Vectors - Velocity")
@Description("Gets, sets, adds or removes velocity to/from/of an entity")
@Examples({"set player's velocity to {_v}"})
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Entity, Vector> {
	static {
		register(ExprVelocity.class, Vector.class, "velocity", "entities");
	}
	
	@Override
	protected String getPropertyName() {
		return "velocity";
	}
	
	@Override
	public Class<Vector> getReturnType() {
		return Vector.class;
	}
	
	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if ((mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.DELETE))
			return new Class[] {Vector.class};
		return null;
	}
	
	@Override
	@Nullable
	public Vector convert(Entity e) {
		return e.getVelocity();
	}
	
	@Override
	@SuppressWarnings("null")
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) throws UnsupportedOperationException {
		for (final Entity ent : getExpr().getArray(e)) {
			if (ent == null)
				return;
			switch (mode) {
				case ADD:
					ent.setVelocity(ent.getVelocity().add((Vector) delta[0]));
					break;
				case REMOVE:
					ent.setVelocity(ent.getVelocity().subtract((Vector) delta[0]));
					break;
				case REMOVE_ALL:
					break;
				case RESET:
				case DELETE:
					ent.setVelocity(new Vector());
					break;	
				case SET:
					ent.setVelocity((Vector) delta[0]);
					break;
			}
		}
	}
}
