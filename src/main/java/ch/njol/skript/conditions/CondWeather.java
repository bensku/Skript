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

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Weather")
@Description({"Checks whether the weather in a world is of a specific type.",
		"<i>I welcome any ideas how to write this condition differently.</i>"})
@Examples({"is thundering",
		"is raining in \"world\" or \"world2\""})
@Since("1.0")
public class CondWeather extends Condition {
	
	static {
		// TODO a better alternative syntax, without the 'is' at the beginning
		Skript.registerCondition(CondWeather.class, "is %weathertypes% [in %worlds%]");
	}
	
	@SuppressWarnings("null")
	private Expression<WeatherType> weathers;
	@SuppressWarnings("null")
	private Expression<World> worlds;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		weathers = (Expression<WeatherType>) vars[0];
		worlds = (Expression<World>) vars[1];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return worlds.check(e, w -> {
			final WeatherType weatherType;
			if (e instanceof WeatherEvent && w.equals(((WeatherEvent) e).getWorld()) && !Delay.isDelayed(e)) {
				weatherType = WeatherType.fromEvent((WeatherEvent) e);
			} else {
				weatherType = WeatherType.fromWorld(w);
			}
			return weathers.check(e,
					expectedType -> expectedType == weatherType);
		}, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is " + weathers.toString(e, debug) + " in " + worlds.toString(e, debug);
	}
	
}
