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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable.Value;

/**
 * @author APickledWalrus
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Value> SINGLE_VARIABLE_TYPE = new SingleVariableType();
	private final static PersistentDataType<byte[], Map<String, Value>> LIST_VARIABLE_TYPE = new ListVariableType();

	private final static String SEPARATOR = Variable.SEPARATOR;

	/*
	 * General Utility Methods
	 */

	/**
	 * For a {@linkplain Block} or an {@linkplain ItemType}, only parts of them are actually a {@linkplain PersistentDataHolder}.
	 * This gets the 'actual' holder from those types (e.g. ItemMeta or TileState).
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @return The actual {@linkplain PersistentDataHolder}, or null if the object's actual holder can't be found.
	 */
	@Nullable
	private static PersistentDataHolder getActualHolder(Object holder) {
		if (holder instanceof PersistentDataHolder) {
			return (PersistentDataHolder) holder;
		} else if (holder instanceof ItemType) {
			return ((ItemType) holder).getItemMeta();
		} else if (holder instanceof Block) {
			if (((Block) holder).getState() instanceof TileState)
				return ((TileState) ((Block) holder).getState());
		}
		return null;
	}

	/**
	 * Tries to convert the input String to a valid key name.
	 * This method <b>will</b> print a warning to the console if the name couldn't be fully converted.
	 * @param name The name to convert
	 * @return The converted name or null if the name couldn't be fully converted.
	 * @see NamespacedKey
	 */
	@SuppressWarnings("null")
	@Nullable
	public static NamespacedKey getNamespacedKey(String name) {
		if (name.contains(" "))
			name = name.replace(" ", "");
		if (name.contains("::"))
			name = name.replace("::", "//");
		try {
			return new NamespacedKey(Skript.getInstance(), name);
		} catch (IllegalArgumentException e) {
			Skript.warning("Invalid characters were used in a Persistent Data variable, or the name is longer than 256 characters."
				+ " If you are trying to get it, it will not be returned."
				+ " If you are trying to change it, it will not be returned."
				+ " Valid characters are letters, numbers, periods, underscores, hyphens, and forward slashes."
				+ " If you believe this is a Skript issue, please create an issue with the appropriate details on GitHub."
			);
			return null;
		}
	}

	/*
	 * Get, Set, Remove, Has Methods
	 */

	/**
	 * Gets a value from a key in the holder's PersistentDataContainer.
	 * <b>Note:</b> If the object can't be found under PersistentData, the holder's metadata for
	 * the same value will be checked if possible. If a value is found, that value will be returned instead.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @return The value, or null if it was not found.
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static Object[] get(Object holder, String name) {
		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return new Object[0];

		if (name.contains("::")) { // Check if it's a list variable.
			String keyName = name.substring(0, name.lastIndexOf(SEPARATOR));
			NamespacedKey key = getNamespacedKey(keyName);
			if (key == null)
				return new Object[0];

			Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
			String index = name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length());

			if (values != null) {
				if (index.equals("*")) { // Return all values
					List<Object> returnObjects = new ArrayList<>();
					for (Value value : values.values())
						returnObjects.add(Classes.deserialize(value.type, value.data));
					return returnObjects.toArray();
				} else { // Return just one value
					Value value = values.get(index);
					if (value != null)
						return new Object[]{Classes.deserialize(value.type, value.data)};
				}
			}

			// Try to get as Metadata instead.
			if (holder instanceof Metadatable) {
				Metadatable mHolder = (Metadatable) holder;
				List<MetadataValue> mValues = mHolder.getMetadata(keyName);

				if (!mValues.isEmpty()) {
					Map<String, Object> mMap = null;
					for (MetadataValue mv : mValues) { // Get the latest value set by Skript
						if (mv.getOwningPlugin() == Skript.getInstance()) {
							mMap = (Map<String, Object>) mv.value();
							break;
						}
					}
					if (mMap == null)
						return new Object[0];

					if (index.equals("*")) { // Return all values
						List<Object> returnObjects = new ArrayList<>();
						for (Object object : mMap.values())
							returnObjects.add(object);
						return returnObjects.toArray();
					} else { // Return just one
						return new Object[]{mMap.get(index)};
					}

				}
			}
		} else {
			NamespacedKey key = getNamespacedKey(name);
			if (key == null)
				return new Object[0];

			Value value = actualHolder.getPersistentDataContainer().get(key, SINGLE_VARIABLE_TYPE);
			if (value != null)
				return new Object[]{Classes.deserialize(value.type, value.data)};

			// Try to get as Metadata instead
			if (holder instanceof Metadatable) {
				Metadatable mHolder = (Metadatable) holder;
				List<MetadataValue> values = mHolder.getMetadata(name);
				for (MetadataValue mv : values) {
					if (mv.getOwningPlugin() == Skript.getInstance()) // Get the latest value set by Skript
						return new Object[]{mv.value()};
				}
				return new Object[0];
			}
		}

		return new Object[0];
	}

	/**
	 * Sets a key to a value in the holder's PersistentDataContainer.
	 * <b>Note:</b> If the given value can't be serialized by Skript, the value will be set in Metadata instead if possible.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @param value The value for the key to be set to.
	 * @return Whether the key was set to the value.
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static boolean set(Object holder, String name, Object value) {
		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		Value serialized = Classes.serialize(value);

		if (name.contains("::")) { // Check if it's a list variable.
			if (serialized != null) {  // Can be serialized, set as PersistentData
				String keyName = name.substring(0, name.lastIndexOf(SEPARATOR));
				NamespacedKey key = getNamespacedKey(keyName);
				if (key == null)
					return false;

				Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
				if (values == null)
					values = new HashMap<>();

				String index = name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length());
				if (index.equals("*")) { // Clear map and set value
					values.clear();
					values.put("1", serialized);
				} else {
					values.put(index, serialized);
				}

				actualHolder.getPersistentDataContainer().set(key, LIST_VARIABLE_TYPE, values);

				// This is to store the data on the ItemType or TileState
				if (holder instanceof ItemType) {
					((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
				} else if (actualHolder instanceof TileState) {
					((TileState) actualHolder).update();
				}

				return true;
			}

			// Try to set as Metadata instead
			if (holder instanceof Metadatable) {
				Skript.warning("The variable '{" + name + "}' is not able to be set under Persistent Data."
						+ " However, the value will instead be set under Metadata and will clear on a restart."
						+ " It will still be accessible through the Persistent Data expression too."
				);
				Metadatable mHolder = (Metadatable) holder;

				String keyName = name.substring(0, name.lastIndexOf(SEPARATOR));
				List<MetadataValue> mValues = mHolder.getMetadata(keyName);

				Map<String, Object> mMap = null;
				for (MetadataValue mv : mValues) { // Get the latest value set by Skript
					if (mv.getOwningPlugin() == Skript.getInstance()) {
						mMap = (Map<String, Object>) mv.value();
						break;
					}
				}
				if (mMap == null)
					mMap = new HashMap<>();

				String index = name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length());
				if (index.equals("*")) { // Clear map and set value
					mMap.clear();
					mMap.put("1", value);
				} else {
					mMap.put(index, value);
				}

				mHolder.setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), mMap));

				return true;
			}

		} else {
			if (serialized != null) { // Can be serialized, set as PersistentData
				NamespacedKey key = getNamespacedKey(name);
				if (key == null)
					return false;

				// Attempt to set the value based on the possible PersistentDataTypes.
				actualHolder.getPersistentDataContainer().set(key, SINGLE_VARIABLE_TYPE, serialized);

				// This is to store the data on the ItemType or TileState
				if (holder instanceof ItemType) {
					((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
				} else if (actualHolder instanceof TileState) {
					((TileState) actualHolder).update();
				}

				return true;
			}

			// Try to set as Metadata instead
			if (holder instanceof Metadatable) {
				Skript.warning("The variable '{" + name + "}' is not able to be set under Persistent Data."
						+ " However, the value will instead be set under Metadata and will clear on a restart."
						+ " It will still be accessible through the Persistent Data expression too."
				);
				Metadatable mHolder = (Metadatable) holder;
				mHolder.setMetadata(name, new FixedMetadataValue(Skript.getInstance(), value));
				return true;
			}

		}

		return false;
	}

	/**
	 * Removes a key/value from the holder's PersistentDataContainer.
	 * <b>Note:</b> If the given key can't be found in the holder's PersistentDataContainer, this method will try to
	 * find it under the holder's Metadata if possible. If a value is found, then it will be removed.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @return Whether the value was removed. False returns mean that the holder does not have the value.
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static boolean remove(Object holder, String name) {
		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		if (name.contains("::")) { // Check if it's a list variable.
			String keyName = name.substring(0, name.lastIndexOf(SEPARATOR));
			NamespacedKey key = getNamespacedKey(keyName);
			if (key == null)
				return false;

			Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
			if (values == null)
				values = new HashMap<>();

			String index = name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length());

			if (!values.isEmpty()) { // Has values may be able to remove
				if (index.equals("*")) { // Remove ALL values
					actualHolder.getPersistentDataContainer().remove(key);
					return true;
				} else { // Remove value and set again
					if (!values.containsKey(index))
						return false;

					values.remove(index);
					if (values.isEmpty()) { // No point in storing an empty map. The last value was removed.
						actualHolder.getPersistentDataContainer().remove(key);
					} else {
						actualHolder.getPersistentDataContainer().set(key, LIST_VARIABLE_TYPE, values);
					}

					// This is to store the data on the ItemType or TileState
					if (holder instanceof ItemType) {
						((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
					} else if (actualHolder instanceof TileState) {
						((TileState) actualHolder).update();
					}

					return true;
				}
			} else { // The map is empty, so we should check Metadata
				Metadatable mHolder = (Metadatable) holder;

				List<MetadataValue> mValues = mHolder.getMetadata(keyName);

				if (!mValues.isEmpty()) {

					Map<String, Object> mMap = null;
					for (MetadataValue mv : mValues) { // Get the latest value set by Skript
						if (mv.getOwningPlugin() == Skript.getInstance()) {
							mMap = (Map<String, Object>) mv.value();
							break;
						}
					}
					if (mMap == null)
						mMap = new HashMap<>();

					if (index.equals("*")) { // Remove ALL values
						if (!mHolder.hasMetadata(keyName))
							return false;
						mHolder.removeMetadata(keyName, Skript.getInstance());
						return true;
					} else { // Remove value and set again
						if (!mMap.containsKey(index))
							return false;
						mMap.remove(index);
						if (mMap.isEmpty()) { // No point in storing an empty map. The last value was removed.
							mHolder.removeMetadata(keyName, Skript.getInstance());
						} else {
							mHolder.setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), mMap));
						}

						return true;
					}

				}
			}
		} else {
			NamespacedKey key = getNamespacedKey(name);
			if (key == null)
				return false;

			if (actualHolder.getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE)) {
				actualHolder.getPersistentDataContainer().remove(key);

				// This is to update the data on the ItemType or TileState
				if (holder instanceof ItemType) {
					((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
				} else if (actualHolder instanceof TileState) {
					((TileState) actualHolder).update();
				}

				return true;
			}

			// Try to remove Metadata instead
			if (holder instanceof Metadatable) {
				Metadatable mHolder = (Metadatable) holder;
				if (mHolder.hasMetadata(name)) {
					mHolder.removeMetadata(name, Skript.getInstance());
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Whether or not the holder has a value under the key.
	 * <b>Note:</b> If the holder doesn't have this value in its PersistentDataContainer, then the holder's metadata will
	 * instead be checked. If a value is found under metadata, then this method will return true.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @return True if the user has the key and false if they do not (or if a problem occurred e.g invalid holder, name).
	 */
	public static boolean has(Object holder, String name) {
		// Run the get method because of how much needs to be checked/done (avoid code repetition)
		for (Object object : get(holder, name)) {
			if (object != null)
				return true;
		}
		return false;
	}

	/*
	 * Allow PersistentData to work with all Skript types that can be serialized.
	 */

	/**
	 * This {@link PersistentDataType} is used for single variables.
	 * The {@link NamespacedKey}'s key should be the variable's name.
	 * {hello} -> "hello" and the {@link Value} is the variable's serialized value.
	 * @see PersistentDataUtils#getNamespacedKey(String) for conversion details.
	 */
	private final static class SingleVariableType implements PersistentDataType<byte[], Value> {

		// This is how many bytes an int is.
		private final int INT_LENGTH = 4;

		// Charset used for converting bytes and Strings
		@SuppressWarnings("null")
		private final Charset STRING_CHARSET = StandardCharsets.UTF_8;

		public SingleVariableType() {}

		@Override
		public Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@Override
		public Class<Value> getComplexType() {
			return Value.class;
		}

		@SuppressWarnings("null")
		@Override
		public byte[] toPrimitive(Value complex, PersistentDataAdapterContext context) {
			byte[] type = complex.type.getBytes(STRING_CHARSET);

			ByteBuffer bb = ByteBuffer.allocate(INT_LENGTH + type.length + complex.data.length);
			bb.putInt(type.length);
			bb.put(type);
			bb.put(complex.data);

			return bb.array();
		}

		@Override
		public Value fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
			ByteBuffer bb = ByteBuffer.wrap(primitive);

			int typeLength = bb.getInt();
			byte[] typeBytes = new byte[typeLength];
			bb.get(typeBytes, 0, typeLength);
			String type = new String(typeBytes, STRING_CHARSET);

			byte[] data = new byte[bb.remaining()];
			bb.get(data);

			return new Value(type, data);
		}

	}

	/**
	 * This {@link PersistentDataType} is used for list variables.
	 * In this case, a list variable is any variable containing "::" (the separator)
	 * The map's key is the variable's index and the map's value is the index's value.
	 * With this {@link PersistentDataType}, the NamespacedKey's key is the rest of the list variable.
	 * e.g. {one::two::three} where "one//two" would be the {@link NamespacedKey}'s key and "three" the key for the map.
	 * @see PersistentDataUtils#getNamespacedKey(String) for conversion details.
	 */
	private final static class ListVariableType implements PersistentDataType<byte[], Map<String, Value>> {

		// This is how many bytes an int is.
		private final int INT_LENGTH = 4;

		// Charset used for converting bytes and Strings
		@SuppressWarnings("null")
		private final Charset STRING_CHARSET = StandardCharsets.UTF_8;

		public ListVariableType() {}

		@Override
		public Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Map<String, Value>> getComplexType() {
			return (Class<Map<String, Value>>) (Class<?>) Map.class;
		}

		@SuppressWarnings("null")
		@Override
		public byte[] toPrimitive(Map<String, Value> complex, PersistentDataAdapterContext context) {
			int allocate = 0;

			for (String index : complex.keySet()) {
				Value value = complex.get(index);
				// Store it: index -> type -> data
				allocate += INT_LENGTH + index.getBytes(STRING_CHARSET).length
							+ INT_LENGTH + value.type.getBytes(STRING_CHARSET).length 
							+ INT_LENGTH + value.data.length;
			}

			ByteBuffer bb = ByteBuffer.allocate(allocate);

			for (String index : complex.keySet()) {
				Value value = complex.get(index);
				byte[] typeBytes = value.type.getBytes(STRING_CHARSET);
				byte[] indexBytes = index.getBytes(STRING_CHARSET);

				bb.putInt(indexBytes.length);
				bb.put(indexBytes);

				bb.putInt(typeBytes.length);
				bb.put(typeBytes);

				bb.putInt(value.data.length);
				bb.put(value.data);
			}

			return bb.array();
		}

		@Override
		public Map<String, Value> fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
			ByteBuffer bb = ByteBuffer.wrap(primitive);

			HashMap<String, Value> values = new HashMap<>();

			while (bb.hasRemaining()) {
				int indexLength = bb.getInt();
				byte[] indexBytes = new byte[indexLength];
				bb.get(indexBytes, 0, indexLength);
				String index = new String(indexBytes, STRING_CHARSET);

				int typeLength = bb.getInt();
				byte[] typeBytes = new byte[typeLength];
				bb.get(typeBytes, 0, typeLength);
				String type = new String(typeBytes, STRING_CHARSET);

				int dataLength = bb.getInt();
				byte[] dataBytes = new byte[dataLength];
				bb.get(dataBytes, 0, dataLength);

				values.put(index, new Value(type, dataBytes));
			}

			return values;
		}

	}

}
