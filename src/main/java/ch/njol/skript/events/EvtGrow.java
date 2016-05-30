/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.world.StructureGrowEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.StructureType;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class EvtGrow extends SkriptEvent {
	static {
		Skript.registerEvent("Grow", EvtGrow.class, StructureGrowEvent.class, "grow [of %-structuretype%]")
				.description("Called when a tree or giant mushroom grows to full size.")
				.examples("on grow", "on grow of a tree", "on grow of a huge jungle tree")
				.since("1.0");
	}
	
	@Nullable
	private Literal<StructureType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<StructureType>) args[0];
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "grow" + (types != null ? " of " + types.toString(e, debug) : "");
	}
	
	@Override
	public boolean check(final Event e) {
		if (types != null) {
			return types.check(e, new Checker<StructureType>() {
			
				@Override
				public boolean check(final StructureType t) {
					return t.is(((StructureGrowEvent) e).getSpecies());
				}
			});
		}
		return true;
	}
	
}
