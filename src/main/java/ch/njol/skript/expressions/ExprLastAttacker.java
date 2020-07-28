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
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.bukkitutil.ProjectileUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Last Attacker")
@Description("The last block / entity that attacked an entity.")
@Examples({"send \"%last attacker of event-entity%\""})
@Since("INSERT VERSION")
public class ExprLastAttacker extends SimplePropertyExpression<Entity, Object> {
	
	static {
		register(ExprLastAttacker.class, Object.class, "last attacker", "entity");
	}
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	public Object convert(Entity entity) {
		if (entity.getLastDamageCause() != null) {
			if (entity.getLastDamageCause() instanceof EntityDamageByBlockEvent)
				return ((EntityDamageByBlockEvent) entity.getLastDamageCause()).getDamager();
			EntityDamageEvent event = entity.getLastDamageCause();
			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;
				if (evt.getDamager() instanceof Projectile) {
					@Nullable
					Object shooter = ProjectileUtils.getShooter((Projectile) evt.getDamager());
					if (shooter instanceof Entity)
						return shooter;
				}
				return evt.getDamager();
			}
		}
		return null;
	}
	
	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last attacker";
	}
	
}
