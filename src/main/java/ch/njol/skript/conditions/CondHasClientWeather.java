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

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Has Client Weather")
@Description("Checks whether the given players have a custom client weather")
@Examples({"if the player has custom weather:",
		"\tmessage \"Your custom weather is %player's weather%\""})
@Since("2.3")

public class CondHasClientWeather extends PropertyCondition<Player> {
	
	static {
		register(CondHasClientWeather.class, PropertyType.HAVE, "[a] (client|custom) weather [set]", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.getPlayerWeather() != null;
	}
	
	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}
	
	@Override
	protected String getPropertyName() {
		return "custom weather set";
	}
	
}
