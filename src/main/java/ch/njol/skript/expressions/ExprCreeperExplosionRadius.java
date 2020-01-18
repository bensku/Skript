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

import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
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

@Name("Creeper Explosion Radius")
@Description("The explosion radius that a creeper has.")
@Examples("set target entity's explosion radius to 10")
@Since("INSERT VERSION")
public class ExprCreeperExplosionRadius extends SimplePropertyExpression<LivingEntity, Number> {
	
	static {
		if(Skript.methodExists(LivingEntity.class, "getExplosionRadius"))
			register(ExprCreeperExplosionRadius.class, Number.class, "[creeper] explosion radius", "livingentities");
	}

	@Override
	public Number convert(LivingEntity e) {
		return e instanceof Creeper ? ((Creeper) e).getExplosionRadius() : 0;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		int d = delta == null ? 0 : ((Number) delta[0]).intValue();
		for (LivingEntity le : getExpr().getArray(e)) {
			if (le instanceof Creeper) {
				Creeper c = (Creeper) le;
				switch (mode) {
					case ADD:
						int r1 = c.getExplosionRadius() + d;
						if (r1 < 0) r1 = 0;
						c.setExplosionRadius(r1);
						break;
					case SET:
						c.setExplosionRadius(d);
						break;
					case DELETE:
					case RESET:
						c.setExplosionRadius(0);
						break;
					case REMOVE:
						int r2 = c.getExplosionRadius() - d;
						if (r2 < 0) r2 = 0;
						c.setExplosionRadius(r2);
						break;
					case REMOVE_ALL:
						assert false;		
				}
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "creeper explosion radius";
	}
	
	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}
	
}
