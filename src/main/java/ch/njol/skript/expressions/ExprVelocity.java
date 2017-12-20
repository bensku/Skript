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
@Description("Gets, sets, adds or removes the velocity/vector of entities.")
@Examples("set player's velocity to {_vector}")
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Entity, Vector> {
	
	static {
		register(ExprVelocity.class, Vector.class, "(vector[s]|velocit(y|ies))", "entities");
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
		if (!(mode == Changer.ChangeMode.REMOVE_ALL))
			return new Class[] {Vector.class};
		return null;
	}
	
	@Override
	@Nullable
	public Vector convert(Entity entity) {
		return entity.getVelocity();
	}
	
	@Override
	@SuppressWarnings("null")
	public void change(final Event event, final @Nullable Object[] delta, final Changer.ChangeMode mode) throws UnsupportedOperationException {
		if (delta == null) return;
		for (final Entity entity : getExpr().getArray(event)) {
			if (entity == null) return;
			switch (mode) {
				case ADD:
					entity.setVelocity(entity.getVelocity().add((Vector) delta[0]));
					break;
				case REMOVE:
					entity.setVelocity(entity.getVelocity().subtract((Vector) delta[0]));
					break;
				case REMOVE_ALL:
					break;
				case RESET:
				case DELETE:
					entity.setVelocity(new Vector());
					break;	
				case SET:
					entity.setVelocity((Vector) delta[0]);
					break;
			}
		}
	}
}
