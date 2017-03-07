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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * A block that gets all data from the world, but either delays any changes by 1 tick of reflects them on a given BlockState depending on which constructor is used.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@NonNullByDefault(false)
public class DelayedChangeBlock implements Block {
	
	final Block b;
	@Nullable
	private final BlockState newState;
	
	public DelayedChangeBlock(final Block b) {
		assert b != null;
		this.b = b;
		newState = null;
	}
	
	public DelayedChangeBlock(final Block b, final BlockState newState) {
		assert b != null;
		this.b = b;
		this.newState = newState;
	}
	
	@Override
	public void setMetadata(final String metadataKey, final MetadataValue newMetadataValue) {
		b.setMetadata(metadataKey, newMetadataValue);
	}
	
	@Override
	public List<MetadataValue> getMetadata(final String metadataKey) {
		return b.getMetadata(metadataKey);
	}
	
	@Override
	public boolean hasMetadata(final String metadataKey) {
		return b.hasMetadata(metadataKey);
	}
	
	@Override
	public void removeMetadata(final String metadataKey, final Plugin owningPlugin) {
		b.removeMetadata(metadataKey, owningPlugin);
	}
	
	@Override
	public byte getData() {
		return b.getData();
	}
	
	@Override
	public Block getRelative(final int modX, final int modY, final int modZ) {
		return b.getRelative(modX, modY, modZ);
	}
	
	@Override
	public Block getRelative(final BlockFace face) {
		return b.getRelative(face);
	}
	
	@Override
	public Block getRelative(final BlockFace face, final int distance) {
		return b.getRelative(face, distance);
	}
	
	@Override
	public Material getType() {
		return b.getType();
	}
	
	@Override
	public int getTypeId() {
		return b.getTypeId();
	}
	
	@Override
	public byte getLightLevel() {
		return b.getLightLevel();
	}
	
	@Override
	public byte getLightFromSky() {
		return b.getLightFromSky();
	}
	
	@Override
	public byte getLightFromBlocks() {
		return b.getLightFromBlocks();
	}
	
	@Override
	public World getWorld() {
		return b.getWorld();
	}
	
	@Override
	public int getX() {
		return b.getX();
	}
	
	@Override
	public int getY() {
		return b.getY();
	}
	
	@Override
	public int getZ() {
		return b.getZ();
	}
	
	@Override
	public Location getLocation() {
		return b.getLocation();
	}
	
	@Override
	public Chunk getChunk() {
		return b.getChunk();
	}
	
	@Override
	public void setData(final byte data) {
		if (newState != null) {
			newState.setRawData(data);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setData(data);
				}
			});
		}
	}
	
	@Override
	public void setData(final byte data, final boolean applyPhysics) {
		if (newState != null) {
			newState.setRawData(data);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setData(data, applyPhysics);
				}
			});
		}
	}
	
	@Override
	public void setType(final Material type) {
		if (newState != null) {
			newState.setType(type);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setType(type);
				}
			});
		}
	}
	
	@Override
	public boolean setTypeId(final int type) {
		final BlockState newState = this.newState;
		if (newState != null) {
			newState.setTypeId(type);
			return newState.getTypeId() != getTypeId();
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setTypeId(type);
				}
			});
			return true;
		}
	}
	
	@Override
	public boolean setTypeId(final int type, final boolean applyPhysics) {
		final BlockState newState = this.newState;
		if (newState != null) {
			newState.setTypeId(type);
			return newState.getTypeId() != getTypeId();
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setTypeId(type, applyPhysics);
				}
			});
			return true;
		}
	}
	
	@Override
	public boolean setTypeIdAndData(final int type, final byte data, final boolean applyPhysics) {
		final BlockState newState = this.newState;
		if (newState != null) {
			newState.setTypeId(type);
			newState.setRawData(data);
			return newState.getTypeId() != getTypeId() || newState.getRawData() != getData();
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setTypeIdAndData(type, data, applyPhysics);
				}
			});
			return true;
		}
	}
	
	@Override
	public BlockFace getFace(final Block block) {
		return b.getFace(block);
	}
	
	@Override
	public BlockState getState() {
		return b.getState();
	}
	
	@Override
	public Biome getBiome() {
		return b.getBiome();
	}
	
	@Override
	public void setBiome(final Biome bio) {
		b.setBiome(bio);
	}
	
	@Override
	public boolean isBlockPowered() {
		return b.isBlockPowered();
	}
	
	@Override
	public boolean isBlockIndirectlyPowered() {
		return b.isBlockIndirectlyPowered();
	}
	
	@Override
	public boolean isBlockFacePowered(final BlockFace face) {
		return b.isBlockFacePowered(face);
	}
	
	@Override
	public boolean isBlockFaceIndirectlyPowered(final BlockFace face) {
		return b.isBlockFaceIndirectlyPowered(face);
	}
	
	@Override
	public int getBlockPower(final BlockFace face) {
		return b.getBlockPower(face);
	}
	
	@Override
	public int getBlockPower() {
		return b.getBlockPower();
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
		return b.getTemperature();
	}
	
	@Override
	public double getHumidity() {
		return b.getHumidity();
	}
	
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return b.getPistonMoveReaction();
	}
	
	@Override
	public boolean breakNaturally() {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally();
				}
			});
			return true;
		}
	}
	
	@Override
	public boolean breakNaturally(final ItemStack tool) {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally(tool);
				}
			});
			return true;
		}
	}
	
	@Override
	public Collection<ItemStack> getDrops() {
		return b.getDrops();
	}
	
	@Override
	public Collection<ItemStack> getDrops(final ItemStack tool) {
		return b.getDrops(tool);
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
