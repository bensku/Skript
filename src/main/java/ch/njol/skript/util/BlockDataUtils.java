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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

/**
 * Utility class for modifying {@link BlockData} of {@link Block Blocks}
 */
public class BlockDataUtils {
	
	private static final Map<String, StateType> stateTypes = new HashMap<>();
	
	static {
		// Load all variations of block state types
		if (Skript.isRunningMinecraft(1, 13)) {
			for (Material material : Material.values()) {
				// If this material is not a block, let's continue.
				if (!material.isBlock())
					continue;
				
				BlockData blockData = material.createBlockData();
				String blockDataString = blockData.getAsString();
				
				// If this block data does not have states, let's continue.
				if (!blockDataString.contains("["))
					continue;
				
				String stateString = blockDataString.replaceAll(".+\\[|]", "");
				String[] splits = stateString.split(",");
				for (String s : splits) {
					String[] split = s.split("=");
					String state = split[0];
					String name = state.replace("_", " ");
					
					if (!stateTypes.containsKey(name))
						stateTypes.put(name, new StateType(name, state));
					
				}
			}
		}
	}
	
	/**
	 * Get all state types
	 *
	 * @return Collection of all state types
	 */
	public static Collection<StateType> getStateTypes() {
		return stateTypes.values();
	}
	
	/**
	 * Get a string of all state type names
	 * Used when registering class info
	 *
	 * @return String of all state type names
	 */
	public static String getStateTypeNames() {
		return StringUtils.join(stateTypes.keySet(), ", ");
	}
	
	/**
	 * Get a state type by name
	 *
	 * @param name Name of state type
	 * @return State type by name
	 */
	public static StateType getByName(String name) {
		return stateTypes.get(name);
	}
	
	/**
	 * Change the state of a block
	 *
	 * @param block     Block to change
	 * @param stateType StateType to change
	 * @param value     New value
	 */
	public static void setBlockState(Block block, StateType stateType, Object value) {
		BlockData blockData = block.getBlockData();
		String key = block.getType().getKey().toString();
		
		try {
			BlockData newData = Bukkit.createBlockData(key + "[" + stateType.type + "=" + value.toString().toLowerCase() + "]");
			blockData = blockData.merge(newData);
			block.setBlockData(blockData);
		} catch (IllegalArgumentException ignore) {
		}
	}
	
	/**
	 * Get a block state from a block
	 *
	 * @param block     Block to grab state from
	 * @param stateType StateType to grab
	 * @return Value of state type from block
	 */
	@Nullable
	public static Object getBlockStateType(Block block, StateType stateType) {
		BlockData blockData = block.getBlockData();
		String blockDataString = blockData.getAsString();
		
		if (!blockDataString.contains("["))
			return null;
		
		String stateString = blockDataString.replaceAll(".+\\[|]", "");
		String[] splits = stateString.split(",");
		for (String s : splits) {
			String[] split = s.split("=");
			String state = split[0];
			String value = split[1];
			if (!state.equalsIgnoreCase(stateType.name))
				continue;
			
			if (NumberUtils.isNumber(value))
				return NumberUtils.createNumber(value);
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
				return value.equalsIgnoreCase("true");
			
			@Nullable
			StateValues stateValues = StateValues.getByName(value);
			if (stateValues != null)
				return stateValues;
			
			@Nullable
			Direction direction = getDirection(value);
			if (direction != null)
				return direction;
			return value;
		}
		return null;
	}
	
	@Nullable
	private static Direction getDirection(String dir) {
		switch (dir) {
			case "north":
				return new Direction(BlockFace.NORTH, 1);
			case "south":
				return new Direction(BlockFace.SOUTH, 1);
			case "east":
				return new Direction(BlockFace.EAST, 1);
			case "west":
				return new Direction(BlockFace.WEST, 1);
			case "up":
				return new Direction(BlockFace.UP, 1);
			case "down":
				return new Direction(BlockFace.DOWN, 1);
		}
		return null;
	}
	
	/**
	 * Represents state types a block's {@link BlockData} can have
	 */
	public static class StateType implements YggdrasilExtendedSerializable {
		
		private String name = "";
		private String type = "";
		
		public StateType() {}
		
		public StateType(String name, String type) {
			this.name = name;
			this.type = type;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return type;
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
	
	/**
	 * Represents special block state values which are currently not handled by Skript
	 */
	public enum StateValues {
		
		// AXIS
		X("x"),
		Y("y"),
		Z("z"),
		
		// ORIENTATION (used in jigsaws)
		DOWN_EAST("down east"),
		DOWN_NORTH("down north"),
		DOWN_SOUTH("down south"),
		DOWN_WEST("down west"),
		EAST_UP("east up"),
		NORTH_UP("north up"),
		SOUTH_UP("south up"),
		UP_EAST("up east"),
		UP_NORTH("up north"),
		UP_SOUTH("up south"),
		UP_WEST("up west"),
		WEST_UP("west up"),
		
		// ATTACHMENT / FACE / HINGE
		CEILING("ceiling"),
		DOUBLE_WALL("double wall"),
		FLOOR("floor"),
		SINGLE_WALL("single wall"),
		WALL("wall"),
		LEFT("left"),
		RIGHT("right"),
		
		// HALF / PART
		LOWER("lower"),
		UPPER("upper"),
		BOTTOM("bottom"),
		TOP("top"),
		FOOT("foot"),
		HEAD("head"),
		
		// BLOCK SIDES
		NONE("none"),
		SIDE("side"),
		
		// SIZE
		SMALL("small"),
		LARGE("large"),
		
		// REDSTONE MODE
		COMPARE("compare"),
		SUBTRACT("subtract"),
		CORNER("corner"),
		DATA("data"),
		LOAD("load"),
		SAVE("save"),
		
		// SHAPE (used for rails)
		ASCENDING_EAST("ascending east"),
		ASCENDING_NORTH("ascending north"),
		ASCENDING_SOUTH("ascending south"),
		ASCENDING_WEST("ascending west"),
		INNER_LEFT("inner left"),
		INNER_RIGHT("inner right"),
		OUTER_LEFT("outer left"),
		OUTER_RIGHT("outer right"),
		STRAIGHT("straight"),
		EAST_WEST("east west"),
		NORTH_SOUTH("north south"),
		
		// PISTON TYPE
		NORMAL("normal"),
		STICKY("sticky"),
		SINGLE("single"),
		DOUBLE("double");
		
		private final String name;
		
		StateValues(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		private static final Map<String, StateValues> names = new HashMap<>();
		
		static {
			for (StateValues value : StateValues.values()) {
				names.put(value.name, value);
			}
		}
		
		public static String getNames() {
			return StringUtils.join(names.keySet(), ", ");
		}
		
		@Nullable
		public static StateValues getByName(String name) {
			if (names.containsKey(name))
				return names.get(name);
			return null;
		}
	}
	
}
