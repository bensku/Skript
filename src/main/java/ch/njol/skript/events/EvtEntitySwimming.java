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
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtEntitySwimming extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.event.entity.EntityToggleSwimEvent"))
			Skript.registerEvent("Entity Swim", EvtEntitySwimming.class, EntityToggleSwimEvent.class, "[entity] [(1¦start|2¦stop)] swim[ming]", "[entity] swim[ming] (1¦start|2¦stop)")
					.description("Called when a living entity toggles their swimming state.")
					.examples("on entity stop swimming:",
							"\tbroadcast \"A %entity% has stopped swimming\"")
					.since("INSERT VERSION");
	}
	
	private boolean both, start;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		start = parser.mark == 1;
		both = parser.mark == 0;
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		if (both)
			return true;
		return ((EntityToggleSwimEvent) e).isSwimming() ? start : !start;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "entity " + ((both) ? "" : (start ? "started" : "stopped")) + " swimming";
	}
	
}
