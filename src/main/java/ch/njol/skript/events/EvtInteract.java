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

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;

public class EvtInteract extends SkriptEvent {
	
	static {
		Skript.registerEvent("Entity Physical Interact", EvtInteract.class, new Class[]{EntityInteractEvent.class, PlayerInteractEvent.class},
			"(interact[ing]|trampl(e|ing)|trip[ping]|trigger[ing]) [[(with|on|of)] %itemtypes%]")
			.description("Called when an entity physically interacts with a block, such as a player trampling farmland, a villager opening a door " +
				"or a zombie breaking turtle eggs.")
			.examples("on trampling farmland:",
				"\tif event-entity is not a player:",
				"\t\tcancel event",
				"on interacting with any door:",
				"\tif event-entity is a villager:",
				"\t\tcancel event",
				"on triggering an oak pressure plate:",
				"\tif event-entity is a player:",
				"\t\tteleport player to spawn")
			.since("INSERT VERSION");
		
		EventValues.registerEventValue(EntityInteractEvent.class, Block.class, new Getter<Block, EntityInteractEvent>() {
			@Nullable
			@Override
			public Block get(EntityInteractEvent e) {
				return e.getBlock();
			}
		}, 0);
	}
	
	@Nullable
	private Literal<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = (Literal<ItemType>) args[0];
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		boolean player = e instanceof PlayerInteractEvent && ((PlayerInteractEvent) e).getAction() == Action.PHYSICAL;
		if (!player && !(e instanceof EntityInteractEvent)) return false;
		if (types != null) {
			for (ItemType type : types.getAll()) {
				if (player)
					return type.isOfType(((PlayerInteractEvent) e).getClickedBlock());
				else
					return type.isOfType(((EntityInteractEvent) e).getBlock());
			}
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "Interact" + (types != null ? " on " + types.toString(e, d) : "");
	}
	
}
