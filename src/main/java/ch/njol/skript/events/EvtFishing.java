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
package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtFishing extends SkriptEvent {

	static {
		Skript.registerEvent("Fishing", EvtFishing.class, PlayerFishEvent.class, "[player] fish[ing] [[state[s]] %-fishingstates%]")
				.description("Called when a player triggers a fishing event; reeling in, catching a fish/entity, failing, etc.")
				.examples("on fish:",
						"\tmessage \"You did something relating to fishing!\"",
						"on fishing states failed and in ground:", // Fun grappling hook.
						"\tplayer is holding a fishing rod",
						"\tname of player's tool is \"&6Grappling hook\"",
						"\tpush player direction from player to fishing hook at speed 2.5")
				.since("1.0");
	}

	@Nullable
	private Literal<PlayerFishEvent.State> states;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		states = (Literal<PlayerFishEvent.State>) args[0];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	public boolean check(Event e) {
		if (states == null)
			return true;
		PlayerFishEvent event = (PlayerFishEvent) e;
		for (PlayerFishEvent.State state : states.getArray(event)) {
			if (state == event.getState())
				return true;
		}
		return false;
	}

	@SuppressWarnings("null")
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (states == null)
			return "fishing";
		return "fishing with states " + states.toString(e, debug);
	}

}
