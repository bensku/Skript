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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.NonNullByDefault;

import ch.njol.skript.Skript;

/**
 * A block that gets all data from a BlockState, and either reflects changes on the BlockState or delays them to the real block by 1 tick depending on which constructor is used.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@NonNullByDefault(false)
public class BlockStateBlock implements Block {
	
	final BlockState state;
	private final boolean delayChanges;
	
	public BlockStateBlock(final BlockState state) {
		assert state != null;
		this.state = state;
		delayChanges = false;
	}
	
	public BlockStateBlock(final BlockState state, final boolean delayChanges) {
		assert state != null;
		this.state = state;
		this.delayChanges = delayChanges;
	}
	
	@Override
	public void setMetadata(final String metadataKey, final MetadataValue newMetadataValue) {
		state.setMetadata(metadataKey, newMetadataValue);
	}
	
	@Override
	public List<MetadataValue> getMetadata(final String metadataKey) {
		return state.getMetadata(metadataKey);
	}
	
	@Override
	public boolean hasMetadata(final String metadataKey) {
		return state.hasMetadata(metadataKey);
	}
	
	@Override
	public void removeMetadata(final String metadataKey, final Plugin owningPlugin) {
		state.removeMetadata(metadataKey, owningPlugin);
	}
	
	@Override
	public byte getData() {
		return state.getRawData();
	}
	
	@Override
	public Block getRelative(final int modX, final int modY, final int modZ) {
		return state.getBlock().getRelative(modX, modY, modZ);
	}
	
	@Override
	public Block getRelative(final BlockFace face) {
		return state.getBlock().getRelative(face);
	}
	
	@Override
	public Block getRelative(final BlockFace face, final int distance) {
		return state.getBlock().getRelative(face, distance);
	}
	
	@Override
	public Material getType() {
		return state.getType();
	}
	
	@Override
	public int getTypeId() {
		return state.getTypeId();
	}
	
	@Override
	public byte getLightLevel() {
		return state.getLightLevel();
	}
	
	@Override
	public byte getLightFromSky() {
		return state.getBlock().getLightFromSky();
	}
	
	@Override
	public byte getLightFromBlocks() {
		return state.getBlock().getLightFromBlocks();
	}
	
	@Override
	public World getWorld() {
		return state.getWorld();
	}
	
	@Override
	public int getX() {
		return state.getX();
	}
	
	@Override
	public int getY() {
		return state.getY();
	}
	
	@Override
	public int getZ() {
		return state.getZ();
	}
	
	@Override
	public Location getLocation() {
		return state.getLocation();
	}
	
	@Override
	public Chunk getChunk() {
		return state.getChunk();
	}
	
	@Override
	public void setData(final byte data) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setData(data);
				}
			});
		} else {
			state.setRawData(data);
		}
	}
	
	@Override
	public void setData(final byte data, final boolean applyPhysics) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setData(data, applyPhysics);
				}
			});
		} else {
			state.setRawData(data);
		}
	}
	
	@Override
	public void setType(final Material type) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setType(type);
				}
			});
		} else {
			state.setType(type);
		}
	}
	
	@Override
	public boolean setTypeId(final int type) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setTypeId(type);
				}
			});
			return true;
		} else {
			final int id = getTypeId();
			state.setTypeId(type);
			return id != type;
		}
	}
	
	@Override
	public boolean setTypeId(final int type, final boolean applyPhysics) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setTypeId(type, applyPhysics);
				}
			});
			return true;
		} else {
			final int id = getTypeId();
			state.setTypeId(type);
			return id != type;
		}
	}
	
	@Override
	public boolean setTypeIdAndData(final int type, final byte data, final boolean applyPhysics) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setTypeIdAndData(type, data, applyPhysics);
				}
			});
			return true;
		} else {
			final int id = getTypeId();
			final byte d = getData();
			state.setTypeId(type);
			state.setRawData(data);
			return id != type || d != data;
		}
	}
	
	@Override
	public BlockFace getFace(final Block block) {
		return state.getBlock().getFace(block);
	}
	
	@Override
	public BlockState getState() {
		return state;
	}

	@Override
	public BlockState getState(boolean useSnapshot) {
		return state;
	}

	@Override
	public Biome getBiome() {
		return state.getBlock().getBiome();
	}
	
	@Override
	public void setBiome(final Biome bio) {
		state.getBlock().setBiome(bio);
	}
	
	@Override
	public boolean isBlockPowered() {
		return state.getBlock().isBlockPowered();
	}
	
	@Override
	public boolean isBlockIndirectlyPowered() {
		return state.getBlock().isBlockIndirectlyPowered();
	}
	
	@Override
	public boolean isBlockFacePowered(final BlockFace face) {
		return state.getBlock().isBlockFacePowered(face);
	}
	
	@Override
	public boolean isBlockFaceIndirectlyPowered(final BlockFace face) {
		return state.getBlock().isBlockFaceIndirectlyPowered(face);
	}
	
	@Override
	public int getBlockPower(final BlockFace face) {
		return state.getBlock().getBlockPower(face);
	}
	
	@Override
	public int getBlockPower() {
		return state.getBlock().getBlockPower();
	}
	
	@Override
	public boolean isEmpty() {
		return getTypeId() == 0;
	}
	
	@Override
	public boolean isLiquid() {
		return getType() == Material.WATER || getType() == Material.STATIONARY_WATER || getType() == Material.LAVA || getType() == Material.STATIONARY_LAVA;
	}
	
	@Override
	public double getTemperature() {
		return state.getBlock().getTemperature();
	}
	
	@Override
	public double getHumidity() {
		return state.getBlock().getHumidity();
	}
	
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean breakNaturally() {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().breakNaturally();
				}
			});
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean breakNaturally(final ItemStack tool) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().breakNaturally(tool);
				}
			});
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Collection<ItemStack> getDrops() {
		assert false;
		return Collections.emptySet();
	}
	
	@Override
	public Collection<ItemStack> getDrops(final ItemStack tool) {
		assert false;
		return Collections.emptySet();
	}
	
	@Override
	public Location getLocation(final Location loc) {
		if (loc != null) {
			loc.setWorld(getWorld());
			loc.setX(getX());
			loc.setY(getY());
			loc.setZ(getZ());
			loc.setPitch(0);
			loc.setYaw(0);
		}
		return loc;
	}

	@Override
	public void setType(Material arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
