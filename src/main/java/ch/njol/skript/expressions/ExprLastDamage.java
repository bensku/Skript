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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Last Damage")
@Description("The last damage that was done to an entity. Note that changing it doesn't deal more/less damage.")
@Examples({"set last damage of event-entity to 2"})
@Since("INSERT VERSION")
public class ExprLastDamage extends SimplePropertyExpression<LivingEntity, Number> {
	
	static {
		register(ExprLastDamage.class, Number.class, "last damage", "livingentities");
	}
	
	@Nullable
	@Override
	public Number convert(LivingEntity livingEntity) {
		return livingEntity.getLastDamage();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case REMOVE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int mod = 1;
		if (delta != null) {
			switch (mode) {
				case SET:
					for (LivingEntity entity : getExpr().getArray(e))
						entity.setLastDamage((Long) delta[0]);
					break;
				case REMOVE:
					mod = -1;
				case ADD:
					for (LivingEntity entity : getExpr().getArray(e))
						entity.setLastDamage((Long) delta[0] * mod + entity.getLastDamage());
					break;
			}
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last damage";
	}
	
}
