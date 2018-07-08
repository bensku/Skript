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
import org.bukkit.material.Directional;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.util.coll.CollectionUtils;

/**
 * TODO !Update with every version [blocks] - also update aliases-*.sk
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class BlockUtils {
	
	private final static BlockFace[] torch = new BlockFace[] {
			null, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN
	};
	
	private final static BlockFace[] button = new BlockFace[] {
			null, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, null, null, null,
			null, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
	};
	
	private final static BlockFace[] ladder = new BlockFace[] {
			null, null, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST
	}, wallSign = ladder;
	
	private final static BlockFace[] trapdoor = new BlockFace[] {
			BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST
	};
	
	private final static BlockFace[] lever = new BlockFace[] {
			BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.DOWN, BlockFace.UP,
			BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.DOWN, BlockFace.UP
	};
	
	private final static BlockFace[] cocoa = new BlockFace[] {
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
	};
	
	private final static BlockFace[] tripwireHook = new BlockFace[] {
			BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
			BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
	};
	
	private final static BlockFace[][] attached = new BlockFace[Skript.MAXBLOCKID + 1][];
	static {
		attached[Material.TORCH.getId()] = torch;
		attached[Material.STONE_BUTTON.getId()] = button;
		attached[Material.LADDER.getId()] = ladder;
		attached[Material.WALL_SIGN.getId()] = wallSign;
		attached[Material.TRAP_DOOR.getId()] = trapdoor;
		attached[Material.LEVER.getId()] = lever;
		if (Skript.fieldExists(Material.class, "COCOA")) {
			attached[Material.COCOA.getId()] = cocoa;
			attached[Material.TRIPWIRE_HOOK.getId()] = tripwireHook;
		}
		if (Skript.fieldExists(Material.class, "WOOD_BUTTON"))
			attached[Material.WOOD_BUTTON.getId()] = button;
	}
	
	private final static BlockFace[] bed = new BlockFace[] {
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
	};
	
	// not the actual facing, but a direction where fence posts should exist
	private final static BlockFace[] gate = new BlockFace[] {
			BlockFace.WEST, BlockFace.NORTH
	};
	
	/**
	 * @param b
	 * @param type
	 * @param dataMin The minimum data value from 0 to 15, can be -1
	 * @param dataMax The maximum data value from 0 to 15, can be -1
	 * @param applyPhysics TODO add effect that sets block without physics checks
	 * @return Whether the block could be set successfully
	 */
	public static boolean set(final Block b, final int type, byte dataMin, byte dataMax, final boolean applyPhysics) {
		final boolean any = dataMin == -1 && dataMax == -1;
		if (dataMin == -1)
			dataMin = 0;
		if (dataMax == -1)
			dataMax = 15;
		if (type < 0 || type > Skript.MAXBLOCKID)
			throw new IllegalArgumentException("Invalid block type id " + type);
		if (dataMin < 0 || dataMin > dataMax || dataMax > 15)
			throw new IllegalArgumentException("Invalid data range " + dataMin + " to " + dataMax);
		
		// ATTACHABLES
		final BlockFace[] attach = attached[type];
		if (attach != null) {
			if (dataMin >= attach.length) {
				b.setTypeIdAndData(type, (byte) Utils.random(dataMin, dataMax + 1), applyPhysics);
				return true;
			}
			dataMax = (byte) Math.min(dataMax, attach.length - 1);
			// solid blocks?
			if (CollectionUtils.indexOf(attach, BlockFace.DOWN, dataMin, dataMax) != -1) {
				if (isSolid(b.getRelative(BlockFace.DOWN).getTypeId())) {
					for (final byte data : CollectionUtils.permutation(dataMin, dataMax)) {
						if (attach[data] != BlockFace.DOWN)
							continue;
						b.setTypeIdAndData(type, data, applyPhysics);
						return true;
					}
					assert false;
				}
			}
			for (final int data : CollectionUtils.permutation(dataMin, dataMax)) {
				final BlockFace f = attach[data];
				if (f == null)
					continue;
				if (isSolid(b.getRelative(f).getTypeId())) {
					b.setTypeIdAndData(type, (byte) data, applyPhysics);
					return true;
				}
			}
			// no solid blocks - any blocks at all?
			if (CollectionUtils.indexOf(attach, BlockFace.DOWN, dataMin, dataMax) != -1) {
				if (b.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
					for (final byte data : CollectionUtils.permutation(dataMin, dataMax)) {
						if (attach[data] != BlockFace.DOWN)
							continue;
						b.setTypeIdAndData(type, data, applyPhysics);
						return true;
					}
					assert false;
				}
			}
			for (final int data : CollectionUtils.permutation(dataMin, dataMax)) {
				final BlockFace f = attach[data];
				if (f == null)
					continue;
				if (b.getRelative(f).getType() != Material.AIR) {
					b.setTypeIdAndData(type, (byte) data, applyPhysics);
					return true;
				}
			}
			// no blocks at all - just place it in the air, who cares ^^
			b.setTypeIdAndData(type, (byte) Utils.random(dataMin, dataMax + 1), applyPhysics);
			return true;
		}
		
		// DOORS
		if (type == Material.IRON_DOOR_BLOCK.getId() || type == Material.WOODEN_DOOR.getId()) {
			final int up = b.getRelative(BlockFace.UP).getTypeId();
			final int down = b.getRelative(BlockFace.DOWN).getTypeId();
			if (up == 0 || up == type && b.getRelative(BlockFace.UP).getData() >= 0x8) {
				if (dataMin >= 0x8) // top half
					return false;
				if (!isSolid(down))
					return false;
				dataMax = (byte) Math.min(dataMax, 0x8);
				final byte data = (byte) Utils.random(dataMin, dataMax + 1);
				if (up != type)
					b.getRelative(BlockFace.UP).setTypeIdAndData(type, (byte) 0x8, false);
				b.setTypeIdAndData(type, data, applyPhysics);
				return true;
			} else if (down == 0 || down == type && b.getRelative(BlockFace.DOWN).getData() < 0x8) {
				if (dataMax < 0x8) // bottom half
					return false;
				if (!isSolid(b.getRelative(BlockFace.DOWN, 2).getTypeId()))
					return false;
				dataMin = (byte) Math.max(dataMin, 0x8);
				final byte data = (byte) Utils.random(dataMin, dataMax + 1);
				if (down != type)
					b.getRelative(BlockFace.DOWN).setTypeIdAndData(type, (byte) 0x0, false);
				b.setTypeIdAndData(type, data, applyPhysics);
				return true;
			}
			return false;
		}
		
		// BED
		if (type == Material.BED_BLOCK.getId()) {
			for (final byte data : CollectionUtils.permutation(dataMin, dataMax)) {
				final boolean head = (data & 0x8) == 0x8;
				final BlockFace f = bed[data & 0x3];
				if (head) {
					if (b.getRelative(f, -1).getTypeId() != 0)
						continue;
					b.getRelative(f, -1).setTypeIdAndData(type, (byte) (data & ~0x8), false);
					b.setTypeIdAndData(type, data, applyPhysics);
					return true;
				} else {
					if (b.getRelative(f).getTypeId() != 0)
						continue;
					b.getRelative(f).setTypeIdAndData(type, (byte) (data | 0x8), false);
					b.setTypeIdAndData(type, data, applyPhysics);
					return true;
				}
			}
			return false;
		}
		
		// FENCE GATE
		if (type == Material.FENCE_GATE.getId()) {
			final boolean[] tried = new boolean[gate.length];
			for (final byte data : CollectionUtils.permutation(dataMin, dataMax)) {
				if (tried[data & 0x1])
					continue;
				final BlockFace f = gate[data & 0x1];
				final Block b1 = b.getRelative(f), b2 = b.getRelative(f, -1);
				final int m1 = b1.getTypeId(), m2 = b2.getTypeId();
				// 113 == nether fence
				if ((m1 == Material.FENCE.getId() || m1 == 113 || m1 == Material.FENCE_GATE.getId() && (b1.getData() & 0x1) == (data & 0x1))
						&& (m2 == Material.FENCE.getId() || m2 == 113 || m2 == Material.FENCE_GATE.getId() && (b2.getData() & 0x1) == (data & 0x1))) {
					b.setTypeIdAndData(type, data, applyPhysics);
					return true;
				} else {
					tried[data & 0x1] = true;
				}
			}
			b.setTypeIdAndData(type, (byte) Utils.random(dataMin, dataMax + 1), applyPhysics);
			return true;
		}
		
		// LARGE FLOWER
		if (type == 175) {
			if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
				final byte data = (byte) Utils.random(dataMin, dataMax + 1);
				b.getRelative(BlockFace.UP).setTypeIdAndData(type, (byte) (data | 0x4), false);
				b.setTypeIdAndData(type, (byte) (data & ~0x4), applyPhysics);
				return true;
			}
		}
		
		// REMIND rails?
		
		// DUMMY
		b.setTypeIdAndData(type, any ? 0 : (byte) Utils.random(dataMin, dataMax + 1), applyPhysics);
		return true;
	}
	
	// Material.isSolid() treats e.g. steps as solid...
	// TODO !Update with every version [blocks]
	private final static int[] solid = {
			1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 29, 33, 35, 41, 42, 43, 45, 46, 47, 48, 49,
			52, 54, 56, 57, 58, 60, 61, 62, 73, 74, 79, 80, 82, 84, 86, 87, 88, 89, 91, 95, 97, 98, 99,
			100, 103, 110, 112, 120, 121, 123, 124, 125, 129, 130, 133, 137, 138, 146,
			152, 153, 155, 158, 159, 161, 162, 170, 172, 173, 174
	};
	private final static boolean[] isSolid = new boolean[Skript.MAXBLOCKID + 1];
	static {
		for (final int i : solid)
			isSolid[i] = true;
	}
	
	public final static boolean isSolid(final int type) {
		if (type < 0 || type >= isSolid.length)
			throw new IllegalArgumentException(type + " is not a block id");
		return isSolid[type];
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
		final Material m = b.getType();
		if (Directional.class.isAssignableFrom(m.getData())) {
			final BlockFace f = ((Directional) m.getNewData(b.getData())).getFacing();
			l.setPitch(Direction.getPitch(Math.sin(f.getModY())));
			l.setYaw(Direction.getYaw(Math.atan2(f.getModZ(), f.getModX())));
		}
		return l;
	}
	
}
