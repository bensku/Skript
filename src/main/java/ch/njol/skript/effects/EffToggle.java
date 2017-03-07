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
package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@Name("Toggle")
@Description("Toggle the state of a block.")
@Examples({"# use arrows to toggle switches, doors, etc.",
		"on projectile hit:",
		"    projectile is arrow",
		"    toggle the block at the arrow"})
@Since("1.4")
public class EffToggle extends Effect {
	static {
		Skript.registerEffect(EffToggle.class, "(close|turn off|de[-]activate) %blocks%", "(toggle|switch) [[the] state of] %blocks%", "(open|turn on|activate) %blocks%");
	}
	
	@SuppressWarnings("null")
	private Expression<Block> blocks;
	private int toggle;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		blocks = (Expression<Block>) vars[0];
		toggle = matchedPattern - 1;
		return true;
	}
	
	// TODO !Update with every version [blocks]
	private final static byte[] bitFlags = new byte[Skript.MAXBLOCKID + 1];
	private final static boolean[] doors = new boolean[Skript.MAXBLOCKID + 1];
	static {
		bitFlags[Material.DETECTOR_RAIL.getId()] = 0x8;
		// Doors
		bitFlags[Material.WOODEN_DOOR.getId()] = 0x4;
		bitFlags[Material.SPRUCE_DOOR.getId()] = 0x4;
		bitFlags[Material.BIRCH_DOOR.getId()] = 0x4;
		bitFlags[Material.JUNGLE_DOOR.getId()] = 0x4;
		bitFlags[Material.ACACIA_DOOR.getId()] = 0x4;
		bitFlags[Material.DARK_OAK_DOOR.getId()] = 0x4;
		bitFlags[Material.IRON_DOOR_BLOCK.getId()] = 0x4;
		// Redstone stuff
		bitFlags[Material.LEVER.getId()] = 0x8;
		bitFlags[Material.STONE_PLATE.getId()] = 0x1;
		bitFlags[Material.WOOD_PLATE.getId()] = 0x1;
		bitFlags[Material.STONE_BUTTON.getId()] = 0x8;
		// Trapdoors
		bitFlags[Material.TRAP_DOOR.getId()] = 0x4;
		bitFlags[Material.IRON_TRAPDOOR.getId()] = 0x4;
		// Fence gates
		bitFlags[Material.FENCE_GATE.getId()] = 0x4;
		bitFlags[Material.SPRUCE_FENCE_GATE.getId()] = 0x4;
		bitFlags[Material.BIRCH_FENCE_GATE.getId()] = 0x4;
		bitFlags[Material.JUNGLE_FENCE_GATE.getId()] = 0x4;
		bitFlags[Material.DARK_OAK_FENCE_GATE.getId()] = 0x4;
		bitFlags[Material.ACACIA_FENCE_GATE.getId()] = 0x4;
		
		doors[Material.WOODEN_DOOR.getId()] = true;
		doors[Material.SPRUCE_DOOR.getId()] = true;
		doors[Material.BIRCH_DOOR.getId()] = true;
		doors[Material.JUNGLE_DOOR.getId()] = true;
		doors[Material.ACACIA_DOOR.getId()] = true;
		doors[Material.DARK_OAK_DOOR.getId()] = true;
		doors[Material.IRON_DOOR_BLOCK.getId()] = true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (Block b : blocks.getArray(e)) {
			int type = b.getTypeId();
			
			byte data = b.getData();
			if (doors[type] == true && (data & 0x8) == 0x8) {
				b = b.getRelative(BlockFace.DOWN);
				type = b.getTypeId();
				if (doors[type] != true)
					continue;
				data = b.getData();
			}
			if (toggle == -1)
				b.setData((byte) (data & ~bitFlags[type]));
			else if (toggle == 0)
				b.setData((byte) (data ^ bitFlags[type]));
			else
				b.setData((byte) (data | bitFlags[type]));
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "toggle " + blocks.toString(e, debug);
	}
	
}
