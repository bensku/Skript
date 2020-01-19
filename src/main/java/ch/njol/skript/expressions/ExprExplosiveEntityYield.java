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
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.util.coll.CollectionUtils;

@Name("Explosive Entity Explosion Yield")
@Description("The yield/radius of an explosive (primed tnt, fireball, etc.). This is how big of an explosion is caused by the entity. For creepers, use the creeper explosion radius expression (1.12+).")
@Examples("set the explosive radius of the event-entity to 10")
@Since("INSERT VERSION")
public class ExprExplosiveEntityYield extends SimplePropertyExpression<Entity, Number> {

	static {
		register(ExprExplosiveEntityYield.class, Number.class, "explosi(ve|on) (radius|size|yield)", "entities");
	}

	@Override
	public Number convert(Entity e) {
		return e instanceof Explosive ? ((Explosive) e).getYield() : 0;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		Float f = delta == null ? 0 : ((Number) delta[0]).floatValue();
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Explosive) {
				Explosive e = (Explosive) entity;
				switch (mode) {
					case SET:
						e.setYield(f);
						break;
					case ADD:
						float add = e.getYield() + f;
						if (add < 0) add = 0;
						e.setYield(add);
						break;
					case REMOVE:
						float subtract = e.getYield() - f;
						if (subtract < 0) subtract = 0;
						e.setYield(subtract);
						break;	
					case DELETE:
						e.setYield(0);
						break;
					case REMOVE_ALL:
						assert false;
					case RESET:
						assert false;
				}
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "explosive entity explosion yield";
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

}