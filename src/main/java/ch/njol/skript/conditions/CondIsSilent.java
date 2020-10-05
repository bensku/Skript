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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Silent")
@Description("Checks whether an entity is silent i.e. its sounds are disabled.")
@Examples("target entity is silent")
@Since("2.5")
public class CondIsSilent extends PropertyCondition<Entity> {
	
	static {
		register(CondIsSilent.class, PropertyType.BE, "silent", "entities");
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isSilent();
	}
	
	@Override
	protected String getPropertyName() {
		return "silent";
	}
}
