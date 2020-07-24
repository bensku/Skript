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

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Arrow Knockback Strength")
@Description("An arrow's knockback strength.")
@Examples({"on shoot:",
	"\tset arrow knockback strength of event-projectile to 10"})
@Since("INSERT VERSION")
public class ExprArrowKnockbackStrength extends SimplePropertyExpression<Projectile, Number> {
	
	static {
		register(ExprArrowKnockbackStrength.class, Number.class, "[the] arrow knockback (strength|force)", "projectiles");
	}
	
	@Nullable
	@Override
	public Number convert(Projectile arrow) {
		return arrow instanceof Arrow ? ((Arrow) arrow).getKnockbackStrength() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? CollectionUtils.array(Number.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int state = delta != null ? ((Long) delta[0]).intValue() : 1;
		for (Projectile entity : getExpr().getAll(e)) {
			if (entity instanceof Arrow)
				((Arrow) entity).setKnockbackStrength(state);
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "arrow knockback strength";
	}
	
}
