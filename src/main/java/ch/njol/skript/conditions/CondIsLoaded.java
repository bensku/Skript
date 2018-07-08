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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

@Name("Is Loaded")
@Description("Checks whether or not a chunk/world is loaded")
@Examples("if chunk at {home::%player's uuid%} is loaded:")
@Since("INSERT VERSION")
public class CondIsLoaded extends PropertyCondition<Object> {

	static {
		register(CondIsLoaded.class, "loaded", "worlds/chunks");
	}

	@Override
	public boolean check(Object o) {
		if (o instanceof Chunk)
			return ((Chunk) o).isLoaded();
		else if (o instanceof World)
			return Bukkit.getWorld(((World) o).getName()) != null;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "loaded";
	}

}
