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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Has Client Weather")
@Description("Checks whether the given players have a custom client weather")
@Examples({"if the player has custom weather:",
		"\tmessage \"Your custom weather is %player's weather%\""})
@Since("INSERT VERSION")
public class CondHasClientWeather extends Condition {

	static {
		Skript.registerCondition(CondHasClientWeather.class,
				"%players% (has|have) [a] (client|custom) weather [set]",
				"%players% do[es](n't| not) have [a] (client|custom) weather [set]");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		this.players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event e) {
		return players.check(e, player -> player.getPlayerWeather() != null, isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return players.toString(e, debug) + (isNegated() ? " have " : " don't have ") + " custom weather";
	}
}