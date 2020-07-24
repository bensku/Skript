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

@Name("Arrow Critical State")
@Description("A projectile's critical state. The only currently accepted projectiles are arrows and tridents.")
@Examples({"on shoot:",
	"\tevent-projectile is an arrow",
	"\tset critical mode of event-projectile to true"})
@Since("INSERT VERSION")
public class ExprArrowCriticalState extends SimplePropertyExpression<Projectile, Boolean> {
	
	static {
		register(ExprArrowCriticalState.class, Boolean.class, "[the] (projectile|arrow) critical (state|ability|mode)", "projectiles");
	}
	
	boolean abstractArrowExists = Skript.classExists("org.bukkit.entity.AbstractArrow");
	
	@Nullable
	@Override
	public Boolean convert(Projectile arrow) {
		
		if (abstractArrowExists)
			return arrow instanceof AbstractArrow ? ((AbstractArrow) arrow).isCritical() : null;
		return arrow instanceof Arrow ? ((Arrow) arrow).isCritical() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		boolean state = delta != null ? (Boolean) delta[0] : false;
		for (Projectile entity : getExpr().getAll(e)) {
			if (abstractArrowExists) {
				if (entity instanceof AbstractArrow) ((AbstractArrow) entity).setCritical(state);
			} else if (entity instanceof Arrow)
				((Arrow) entity).setCritical(state);
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "critical arrow state";
	}
	
}
