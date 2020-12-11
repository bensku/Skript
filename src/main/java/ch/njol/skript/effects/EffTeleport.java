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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.papermc.lib.PaperLib;

@Name("Teleport")
@Description("Teleport an entity to a specific location.")
@Examples({"teleport the player to {homes.%player%}",
		"teleport the attacker to the victim"})
@Since("1.0")
public class EffTeleport extends Effect {

	static {
		Skript.registerEffect(EffTeleport.class, "teleport %entities% (to|%direction%) %location%");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;
	@SuppressWarnings("null")
	private Expression<Location> location;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		location = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}
	
	@Nullable
	@Override
	protected TriggerItem walk(Event e) {
		debug(e, true);
		TriggerItem next = getNext();
		boolean delayed = Delay.isDelayed(e);
		Delay.addDelayedEvent(e);
		
		final Location loc = location.getSingle(e);
		final Entity[] entityArray = entities.getArray(e); // We have to fetch this before possible async execution to avoid async local variable access.
		final boolean respawnEvent = !delayed && e instanceof PlayerRespawnEvent && entityArray.length == 1 && entityArray[0].equals(((PlayerRespawnEvent) e).getPlayer());
		
		if (respawnEvent && loc != null) {
			((PlayerRespawnEvent) e).setRespawnLocation(getSafeLocation(loc));
		}
		
		if (respawnEvent || loc == null) {
			continueWalk(next, e);
			return null;
		}
		
		Object localVars = Variables.removeLocals(e);
		
		// This will either fetch the chunk instantly if on spigot or already loaded or fetch it async if on paper.
		PaperLib.getChunkAtAsync(loc).thenAccept(chunk -> {
			// The following is now on the main thread
			for (final Entity entity : entityArray) {
				entity.teleport(getSafeLocation(loc));
			}

			// Re-set local variables
			if (localVars != null)
				Variables.setLocalVariables(e, localVars);
			
			// Continue the rest of the trigger if there is one
			continueWalk(next, e);
		});
		return null;
	}
	
	private void continueWalk(@Nullable TriggerItem next, Event e) {
		Object timing = null;
		if (next != null) {
			if (SkriptTimings.enabled()) {
				Trigger trigger = getTrigger();
				if (trigger != null) {
					timing = SkriptTimings.start(trigger.getDebugLabel());
				}
			}
			
			TriggerItem.walk(next, e);
		}
		Variables.removeLocals(e); // Clean up local vars, we may be exiting now
		SkriptTimings.stop(timing);
	}
	
	private Location getSafeLocation(Location loc) {
		Location toLoc = loc;
		if (Math.abs(toLoc.getX() - toLoc.getBlockX() - 0.5) < Skript.EPSILON && Math.abs(toLoc.getZ() - toLoc.getBlockZ() - 0.5) < Skript.EPSILON) {
			final Block on = toLoc.getBlock().getRelative(BlockFace.DOWN);
			if (on.getType() != Material.AIR) {
				toLoc = toLoc.clone();
				// TODO 1.13 block height stuff
				//to.setY(on.getY() + Utils.getBlockHeight(on.getTypeId(), on.getData()));
			}
		}
		return toLoc;
	}
	
	@Override
	protected void execute(final Event e) {
		// Nothing needs to happen here, we're executing in walk
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "teleport " + entities.toString(e, debug) + " to " + location.toString(e, debug);
	}

}
