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

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

public class SkriptChunk implements YggdrasilExtendedSerializable {
	
	private String worldName = "";
	private int x;
	private int z;
	
	// Empty constructor for deserializer
	public SkriptChunk() {}
	
	public SkriptChunk(World world, int x, int z) {
		this.worldName = world.getName();
		this.x = x;
		this.z = z;
	}
	
	public SkriptChunk(Location location) {
		this(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}
	
	public SkriptChunk(Chunk chunk) {
		this(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	@Nullable
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}
	
	@Nullable
	public Chunk getChunk() {
		World world = getWorld();
		if (world != null) {
			return world.getChunkAt(x, z);
		}
		return null;
	}
	
	public boolean isLoaded() {
		World world = getWorld();
		return world != null && world.isChunkLoaded(x, z);
	}
	
	public Entity[] getEntities() {
		Chunk chunk = getChunk();
		if (chunk != null)
			return chunk.getEntities();
		return new Entity[]{};
	}
	
	@Override
	public String toString() {
		return "chunk (" + x + "," + z + ") of " + worldName;
	}
	
	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this);
	}
	
	@Override
	public void deserialize(@NonNull Fields fields) throws StreamCorruptedException, NotSerializableException {
		fields.setFields(this);
	}
	
}
