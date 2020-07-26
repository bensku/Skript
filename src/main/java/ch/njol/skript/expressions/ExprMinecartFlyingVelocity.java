/**
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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Minecart Flying Velocity")
@Description("The velocity of a minecart when it has begun flying.")
@Examples({"on right click on minecart:",
	"\tset flying velocity of event-entity to vector 2, 10, 2"})
@Since("INSERT VERSION")
public class ExprMinecartFlyingVelocity extends SimplePropertyExpression<Entity, Vector> {
	
	static {
		register(ExprMinecartFlyingVelocity.class, Vector.class, "[minecart] fl(y[ing]|ight) velocity", "entities");
	}
	
	@Nullable
	@Override
	public Vector convert(Entity entity) {
		return entity instanceof Minecart ? ((Minecart) entity).getFlyingVelocityMod() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) ? CollectionUtils.array(Vector.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			switch (mode) {
				case SET:
					getExpr().stream(e).forEach(entity -> {
						if (entity instanceof Minecart)
							((Minecart) entity).setFlyingVelocityMod((Vector) delta[0]);
					});
					break;
				case ADD:
					getExpr().stream(e).forEach(entity -> {
						if (entity instanceof Minecart) {
							Minecart minecart = (Minecart) entity;
							minecart.setFlyingVelocityMod(((Vector) delta[0]).add(minecart.getFlyingVelocityMod()));
						}
					});
					break;
				case REMOVE:
					getExpr().stream(e).forEach(entity -> {
						if (entity instanceof Minecart) {
							Minecart minecart = (Minecart) entity;
							minecart.setFlyingVelocityMod(((Vector) delta[0]).subtract(minecart.getFlyingVelocityMod()));
						}
					});
					break;
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "flying velocity";
	}
	
	
	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}
}
