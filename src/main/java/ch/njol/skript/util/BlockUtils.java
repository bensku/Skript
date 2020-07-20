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
package ch.njol.skript.util;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockSetter;
import ch.njol.skript.bukkitutil.block.BlockValues;

/**
 * TODO !Update with every version [blocks] - also update aliases-*.sk
 * 
 * @author Peter Güttinger
 */
public abstract class BlockUtils {
	
	private final static BlockFace[] bed = new BlockFace[] {
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
	};
	
	// not the actual facing, but a direction where fence posts should exist
	private final static BlockFace[] gate = new BlockFace[] {
			BlockFace.WEST, BlockFace.NORTH
	};
	
	/**
	 * Sets the given block.
	 * @param block Block to set.
	 * @param type New type of the block.
	 * @param blockValues Block values to apply after setting the type.
	 * @param applyPhysics Whether physics should be applied or not.
	 * @return Whether setting block succeeded or not (currently always true).
	 */
	public static boolean set(Block block, Material type, @Nullable BlockValues blockValues, boolean applyPhysics) {
		int flags = BlockSetter.ROTATE | BlockSetter.ROTATE_FIX_TYPE | BlockSetter.MULTIPART;
		if (applyPhysics)
			flags |= BlockSetter.APPLY_PHYSICS;
		BlockCompat.SETTER.setBlock(block, type, blockValues, flags);
		
		
		return true;
	}
	
	public static boolean set(Block block, ItemData type, boolean applyPhysics) {
		return set(block, type.getType(), type.getBlockValues(), applyPhysics);
	}
	
	@SuppressWarnings("null")
	public static Iterable<Block> getBlocksAround(final Block b) {
		return Arrays.asList(b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST));
	}
	
	@SuppressWarnings("null")
	public static Iterable<BlockFace> getFaces() {
		return Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
	}
	
	/**
	 * @param b A block
	 * @return Location of the block, including its direction
	 */
	@Nullable
	public static Location getLocation(final @Nullable Block b) {
		if (b == null)
			return null;
		final Location l = b.getLocation().add(0.5, 0.5, 0.5);
//		final Material m = b.getType();
//		if (Directional.class.isAssignableFrom(m.getData())) {
//			final BlockFace f = ((Directional) m.getNewData(b.getData())).getFacing();
//			l.setPitch(Direction.getPitch(Math.sin(f.getModY())));
//			l.setYaw(Direction.getYaw(Math.atan2(f.getModZ(), f.getModX())));
//		}
		// TODO figure out what this code means
		return l;
	}
	
}
