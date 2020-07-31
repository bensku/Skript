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

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Arrow Damage")
@Description("An arrow's base damage.")
@Examples({"on shoot:",
	"\tevent-projectile is an arrow",
	"\tset arrow damage of event-projectile to 0"})
@Since("INSERT VERSION")
public class ExprArrowDamage extends SimplePropertyExpression<Projectile, Number> {
	
	private final static boolean abstractArrowExists = Skript.classExists("org.bukkit.entity.AbstractArrow");
	
	static {
		if (abstractArrowExists || Skript.methodExists(Arrow.class, "getDamage"))
			register(ExprArrowDamage.class, Number.class, "[the] arrow damage", "projectiles");
	}
	
	@Nullable
	@Override
	public Number convert(Projectile arrow) {
		if (abstractArrowExists)
			return arrow instanceof AbstractArrow ? ((AbstractArrow) arrow).getDamage() : null;
		return arrow instanceof Arrow ? ((Arrow) arrow).getDamage() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case RESET:
			case SET:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		double strength = delta != null ? Math.max(((Number) delta[0]).doubleValue(), 0) : 0;
		int mod = 1;
		switch (mode) {
			case REMOVE:
				mod = -1;
			case ADD:
				if (abstractArrowExists) {
					for (Projectile entity : getExpr().getArray(e)) {
						if (entity instanceof AbstractArrow) {
							AbstractArrow abstractArrow = (AbstractArrow) entity;
							double dmg = abstractArrow.getDamage() + strength * mod;
							if (dmg < 0)
								dmg = 0;
							abstractArrow.setDamage(dmg);
						}
					}
				} else {
					for (Projectile entity : getExpr().getArray(e)) {
						if (entity instanceof Arrow) {
							Arrow arrow = (Arrow) entity;
							double dmg = arrow.getDamage() + strength * mod;
							if (dmg < 0)
								dmg = 0;
							arrow.setDamage(dmg);
						}
					}
				}
				break;
			case RESET:
			case SET:
				for (Projectile entity : getExpr().getArray(e)) {
					if (abstractArrowExists) {
						if (entity instanceof AbstractArrow) ((AbstractArrow) entity).setDamage(strength);
					} else if (entity instanceof Arrow) {
						((Arrow) entity).setDamage(strength);
					}
				}
				break;
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "arrow damage";
	}
	
}
