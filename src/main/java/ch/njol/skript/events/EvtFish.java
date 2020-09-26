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
package ch.njol.skript.events;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

@Name("Fishing")
@Description("Called when a player triggers a fishing event (catching a fish, failing, etc.)")
@RequiredPlugins("1.14+ (reel in)")
@Since("1.0, INSERT VERSION (fishing states, entity, player and hook)")
@Examples({"on fishing state of fish caught:",
	"\tsend \"You caught a fish!\" to player",
	"on fishing state of entity caught:",
	"\tpush event-entity vector from entity to player"})
public class EvtFish extends SkriptEvent {
	
	static {
		Skript.registerEvent("Fishing", EvtFish.class, PlayerFishEvent.class, "[player] fish[ing] [state[s] [of] %-fishingstates%]");
	}
	
	@SuppressWarnings("null")
	Literal<PlayerFishEvent.State> states;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		states = (Literal<PlayerFishEvent.State>) args[0];
		if (states == null) {
			return true;
		} else {
			List<PlayerFishEvent.State> enumStates = Arrays.asList(PlayerFishEvent.State.values());
			if (enumStates.containsAll(Arrays.asList(states.getArray())))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean check(Event e) {
		if (e instanceof PlayerFishEvent) {
			return states == null || (states != null && Arrays.asList(states.getArray()).contains(((PlayerFishEvent) e).getState()));
		}
		return false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (states == null)
			return "fishing";
		return "fishing states of " + states.toString(e, debug);
	}
	
}
