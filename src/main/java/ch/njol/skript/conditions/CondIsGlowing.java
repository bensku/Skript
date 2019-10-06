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
package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Glowing")
@Description("Checks whether an entity is glowing.")
@Examples({"player is glowing:",
		"	message \"You have been blessed!\""})
@Since("INSERT VERSION")
public class CondIsGlowing extends PropertyCondition<Entity> {
	
	static {
		register(CondIsPoisoned.class, "glowing", "entities");
	}
	
	@Override
	public boolean check(final Entity e) {
		return e.isGlowing();
	}
	
	@Override
	protected String getPropertyName() {
		return "glowing";
	}
	
}
