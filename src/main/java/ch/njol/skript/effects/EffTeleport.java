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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

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

	@Override
	protected void execute(final Event e) {
		Location to = location.getSingle(e);
		if (to == null)
			return;
		if (Math.abs(to.getX() - to.getBlockX() - 0.5) < Skript.EPSILON && Math.abs(to.getZ() - to.getBlockZ() - 0.5) < Skript.EPSILON) {
			final Block on = to.getBlock().getRelative(BlockFace.DOWN);
			if (on.getType() != Material.AIR) {
				to = to.clone();
				// TODO 1.13 block height stuff
				//to.setY(on.getY() + Utils.getBlockHeight(on.getTypeId(), on.getData()));
			}
		}
		for (final Entity entity : entities.getArray(e)) {
			to.getChunk().load();
			if (e instanceof PlayerRespawnEvent && entity.equals(((PlayerRespawnEvent) e).getPlayer()) && !Delay.isDelayed(e)) {
				((PlayerRespawnEvent) e).setRespawnLocation(to);
			} else {
				entity.teleport(to);
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "teleport " + entities.toString(e, debug) + " to " + location.toString(e, debug);
	}

}
