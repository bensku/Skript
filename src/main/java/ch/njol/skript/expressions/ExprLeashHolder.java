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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Leash Holder")
@Description("Returns the leash holder of the living entity.")
@Examples("set {_example} to the leashed holder of the target mob")
@Since("INSERT VERSION")
public class ExprLeashHolder extends SimplePropertyExpression<LivingEntity, Entity> {

	static {
		register(ExprLeashHolder.class, Entity.class, "leash[ed] holder", "livingentity");
	}

	@SuppressWarnings("null")
	@Override
	public Entity convert(final LivingEntity entity) {
		return entity.getLeashHolder();
	}
	
	@Override
	protected String getPropertyName() {
		return "leash holder";
	}
	
	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

}
