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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * This class allows persistent data to work easily with Skript.
 * In Skript, Persistent Data is formatted like variables.
 * This looks like: <b>set persistent data {isAdmin} of player to true</b>
 * @author APickledWalrus
 * @see SingleVariablePersistentDataType
 * @see ListVariablePersistentDataType
 * @see ch.njol.skript.expressions.ExprPersistentData
 * @see ch.njol.skript.conditions.CondHasPersistentData
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Value> SINGLE_VARIABLE_TYPE = new SingleVariablePersistentDataType();
	private final static PersistentDataType<byte[], Map<String, Value>> LIST_VARIABLE_TYPE = new ListVariablePersistentDataType();

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
	 * This returns a {@link NamespacedKey} from the provided name with Skript as the namespace being used.
	 * The name will be encoded in Base64 to make sure the key name is valid.
	 * @param name The name to convert
	 * @return The created {@link NamespacedKey}
	 */
	@SuppressWarnings("null")
	@Nullable
	public static NamespacedKey getNamespacedKey(String name) {
		// Encode the name in Base64 to make sure the key name is valid
		name = Base64.getEncoder().encodeToString(name.replace(" ", "").getBytes(StandardCharsets.UTF_8)).replace('=', '_').replace('+', '.');
		return new NamespacedKey(Skript.getInstance(), name);
	}

	/*
	 * Single Variable Modification Methods
	 */

	/**
	 * Gets the Persistent Data Tag's value of the given single variable name from the given holder.
	 * If the value set was not serializable, it was set under Metadata and is retrieved from Metadata here.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @return The Persistent Data Tag's value from the holder, or null if: 
	 * the holder was invalid, the key was invalid, or if a value could not be found.
	 * @see PersistentDataUtils#setSingle(Object, String, Object)
	 * @see PersistentDataUtils#removeSingle(Object, String)
	 */
	@Nullable
	public static Object getSingle(Object holder, String name) {
		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return null;

		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return null;

		NamespacedKey key = getNamespacedKey(name);
		if (key == null)
			return null;

		// We need to check to avoid an IllegalArgumentException
		if (actualHolder.getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE)) {
			Value value = actualHolder.getPersistentDataContainer().get(key, SINGLE_VARIABLE_TYPE);
			if (value != null)
				return Classes.deserialize(value.type, value.data);
		}

		// Try to get as Metadata instead
		if (holder instanceof Metadatable) {
			List<MetadataValue> values = ((Metadatable) holder).getMetadata(name);
			for (MetadataValue mv : values) {
				if (mv.getOwningPlugin() == Skript.getInstance()) // Get the latest value set by Skript
					return mv.value();
			}
		}

		return null;
	}

	/**
	 * Sets the Persistent Data Tag from the given name and value for the given holder.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @param value The value for the Persistent Data Tag to be set to.
	 * If this value is not serializable (see {@linkplain Classes#serialize(Object)}), this value will be set under Metadata.
	 * @see PersistentDataUtils#getSingle(Object, String)
	 * @see PersistentDataUtils#removeSingle(Object, String)
	 */
	public static void setSingle(Object holder, String name, Object value) {
		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return;

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return;

		Value serialized = Classes.serialize(value);

		if (serialized != null) { // Can be serialized, set as Persistent Data
			NamespacedKey key = getNamespacedKey(name);
			if (key == null)
				return;

			actualHolder.getPersistentDataContainer().set(key, SINGLE_VARIABLE_TYPE, serialized);

			// This is to store the data on the ItemType or TileState
			if (holder instanceof ItemType) {
				((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
			} else if (actualHolder instanceof TileState) {
				((TileState) actualHolder).update();
			}
		} else if (holder instanceof Metadatable) { // Set as Metadata instead
			((Metadatable) holder).setMetadata(name, new FixedMetadataValue(Skript.getInstance(), value));
		}
	}

	/**
	 * Sets the Persistent Data Tag's value for the given holder from the given name and value.
	 * This method will check the holder's {@linkplain PersistentDataContainer} and Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @see PersistentDataUtils#getSingle(Object, String)
	 * @see PersistentDataUtils#setSingle(Object, String, Object)
	 */
	public static void removeSingle(Object holder, String name) {
		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return;

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return;

		NamespacedKey key = getNamespacedKey(name);
		if (key == null)
			return;

		if (actualHolder.getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE)) { // Can be serialized, try to remove Persistent Data
			actualHolder.getPersistentDataContainer().remove(key);

			// This is to store the data on the ItemType or TileState
			if (holder instanceof ItemType) {
				((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
			} else if (actualHolder instanceof TileState) {
				((TileState) actualHolder).update();
			}
		} else if (holder instanceof Metadatable) { // Try to remove Metadata instead
			((Metadatable) holder).removeMetadata(name, Skript.getInstance());
		}
	}

	/*
	 * List Variable Modification Methods
	 */

	/**
	 * Gets the Persistent Data Tag's value of the given list variable name from the given holder.
	 * This method may return a single value, or multiple, depending on the given name.
	 * If the value set was not serializable, it was set under Metadata and is retrieved from Metadata here.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * @return The Persistent Data Tag's value(s) from the holder, or an empty array if: 
	 * the holder was invalid, the name was invalid, the key was invalid, or if no value(s) could be found.
	 * @see PersistentDataUtils#setList(Object, String, Object)
	 * @see PersistentDataUtils#removeList(Object, String)
	 * @see PersistentDataUtils#getListMap(Object, String)
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static Object[] getList(Object holder, String name) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return new Object[0];

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return new Object[0];

		String keyName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);
		if (key == null)
			return new Object[0];

		String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());

		// We need to check to avoid an IllegalArgumentException
		if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) {
			Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
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
			List<MetadataValue> mValues = ((Metadatable) holder).getMetadata(keyName);
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
		return new Object[0];
	}

	/**
	 * Sets the Persistent Data Tag's value for the given holder from the given list variable name and value.
	 * This method may return a single value, or multiple, depending on the given name.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If the index of the name is "*", then the index set in the list will be "1".
	 * To set a different index, format the list variable like normal (e.g. <b>"myList::index" from {myList::index}</b>)
	 * @param value The value for the Persistent Data Tag to be set to.
	 * If this value is not serializable (see {@linkplain Classes#serialize(Object)}), this value will be set under Metadata.
	 * @see PersistentDataUtils#getList(Object, String)
	 * @see PersistentDataUtils#removeList(Object, String)
	 * @see PersistentDataUtils#setListMap(Object, String, Map)
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static void setList(Object holder, String name, Object value) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return;

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return;

		Value serialized = Classes.serialize(value);

		String keyName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR));

		if (serialized != null) {  // Can be serialized, set as Persistent Data
			NamespacedKey key = getNamespacedKey(keyName);
			if (key == null)
				return;

			Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
			if (values == null)
				values = new HashMap<>();

			String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());
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
		} else if (holder instanceof Metadatable) { // Try to set as Metadata instead
			Metadatable mHolder = (Metadatable) holder;

			Map<String, Object> mMap = null;
			for (MetadataValue mv : mHolder.getMetadata(keyName)) { // Get the latest value set by Skript
				if (mv.getOwningPlugin() == Skript.getInstance()) {
					mMap = (Map<String, Object>) mv.value();
					break;
				}
			}
			if (mMap == null)
				mMap = new HashMap<>();

			String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());
			if (index.equals("*")) { // Clear map and set value
				mMap.clear();
				mMap.put("1", value);
			} else {
				mMap.put(index, value);
			}

			mHolder.setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), mMap));
		}
	}

	/**
	 * Removes the value of the Persistent Data Tag of the given name for the given holder.
	 * This method will check the holder's {@linkplain PersistentDataContainer} and Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If the index of the name is "*", then the entire list will be cleared.
	 * To remove a specific index, format the list variable like normal (e.g. <b>"myList::index" from {myList::index}</b>)
	 * @see PersistentDataUtils#getList(Object, String)
	 * @see PersistentDataUtils#setList(Object, String, Object)
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static void removeList(Object holder, String name) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return;

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return;

		String keyName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);
		if (key == null)
			return;

		String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());

		if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) {
			if (index.equals("*")) { // Remove the whole thing
				actualHolder.getPersistentDataContainer().remove(key);
			} else { // Remove just some
				Map<String, Value> values = actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE);
				if (values != null) {
					values.remove(index);
					if (values.isEmpty()) { // No point in storing an empty map. The last value was removed.
						actualHolder.getPersistentDataContainer().remove(key);
					} else {
						actualHolder.getPersistentDataContainer().set(key, LIST_VARIABLE_TYPE, values);
					}
				}
			}
			
			// This is to store the data on the ItemType or TileState
			if (holder instanceof ItemType) {
				((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
			} else if (actualHolder instanceof TileState) {
				((TileState) actualHolder).update();
			}
		} else if (holder instanceof Metadatable) { // Try metadata
			Metadatable mHolder = (Metadatable) holder;

			if (index.equals("*")) { // Remove ALL values
				mHolder.removeMetadata(keyName, Skript.getInstance());
			} else { // Remove just one
				List<MetadataValue> mValues = mHolder.getMetadata(keyName);

				if (!mValues.isEmpty()) {
					Map<String, Object> mMap = null;
					for (MetadataValue mv : mValues) { // Get the latest value set by Skript
						if (mv.getOwningPlugin() == Skript.getInstance()) {
							mMap = (Map<String, Object>) mv.value();
							break;
						}
					}

					if (mMap != null) {
						mMap.remove(index);
						if (mMap.isEmpty()) { // No point in storing an empty map. The last value was removed.
							mHolder.removeMetadata(keyName, Skript.getInstance());
						} else {
							mHolder.setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), mMap));
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the map of a list variable. Keyed by variable index.
	 * This method will check the holder's {@linkplain PersistentDataContainer} and Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The full list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If it is not provided in this format, a null value will be returned.
	 * @return The map of a list variable, or null if:
	 * If name was provided in an incorrect format, the holder is invalid, or if no value is set under that name for the holder.
	 * @see PersistentDataUtils#getList(Object, String)
	 * @see PersistentDataUtils#setListMap(Object, String, Map)
	 */
	@SuppressWarnings({"unchecked", "null"})
	@Nullable
	public static Map<String, Object> getListMap(Object holder, String name) {
		if (!name.endsWith("*")) // Make sure we're getting a whole list
			return null;

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return null;

		String keyName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);
		if (key == null)
			return null;

		if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) { // It exists under Persistent Data
			Map<String, Object> returnMap = new HashMap<>();
			for (Entry<String, Value> entry : actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE).entrySet()) {
				returnMap.put(entry.getKey(), Classes.deserialize(entry.getValue().type, entry.getValue().data));
			}
			return returnMap;
		} else if (holder instanceof Metadatable) { // Check Metadata
			Map<String, Object> mMap = null;
			for (MetadataValue mv : ((Metadatable) holder).getMetadata(keyName)) { // Get the latest value set by Skript
				if (mv.getOwningPlugin() == Skript.getInstance()) {
					mMap = (Map<String, Object>) mv.value();
					break;
				}
			}
			return mMap;
		}

		return null;
	}

	/**
	 * Sets the list map of the given holder.
	 * This map <i>should</i> be gotten from {@linkplain PersistentDataUtils#getListMap(Object, String)}
	 * This method will check the holder's {@linkplain PersistentDataContainer} and Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The full list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If it is not provided in this format, nothing will be set.
	 * @param varMap The new map for Persistent Data Tag of the given holder.
	 * If a variable map doesn't already exist in the holder's {@linkplain PersistentDataContainer},
	 * this map will be set in their Metadata.
	 * @see PersistentDataUtils#setList(Object, String, Object)
	 * @see PersistentDataUtils#getListMap(Object, String)
	 */
	@SuppressWarnings("null")
	public static void setListMap(Object holder, String name, Map<String, Object> varMap) {
		if (!name.endsWith("*")) // Make sure we're setting a whole list
			return;

		if (varMap.isEmpty()) { // If the map is empty, remove the whole value instead.
			removeList(holder, name);
			return;
		}

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return;

		String keyName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);
		if (key == null)
			return;

		if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) { // It exists under Persistent Data
			Map<String, Value> serializedMap = new HashMap<>();
			for (Entry<String, Object> entry : varMap.entrySet())
				serializedMap.put(entry.getKey(), Classes.serialize(entry.getValue()));
			actualHolder.getPersistentDataContainer().set(key, LIST_VARIABLE_TYPE, serializedMap);
		} else if (holder instanceof Metadatable) { // Check Metadata
			((Metadatable) holder).setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), varMap));
		}

		// We need to update the data on an ItemType or TileState
		if (holder instanceof ItemType) {
			((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
		} else if (actualHolder instanceof TileState) {
			((TileState) actualHolder).update();
		}
	}

	/*
	 * Other Utility Methods
	 */

	/**
	 * Whether the given holder has a value under the given name.
	 * This method will check the holder's {@linkplain PersistentDataContainer} and Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@linkplain PersistentDataUtils#getActualHolder(Object)}
	 * @param name The name of the variable 
	 * (e.g. <b>"myVariable" from {myVariable}</b> OR <b>"myList::index" from {myList::index}</b> OR <b>"myList::*" from {myList::*}</b>)
	 * @return True if the user has something under the Persistent Data Tag from the given name.
	 * This method will return false if: the holder is invalid, the name is invalid, or if no value could be found.
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static boolean has(Object holder, String name) {
		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		boolean isList = name.contains(Variable.SEPARATOR);
		String keyName = isList ? name.substring(0, name.lastIndexOf(Variable.SEPARATOR)) : name;
		NamespacedKey key = getNamespacedKey(keyName);
		if (key == null)
			return false;

		if (isList) {
			String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());
			if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) {
				if (actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE).containsKey(index))
					return true;
			}
			if (holder instanceof Metadatable) {
				Metadatable mHolder = (Metadatable) holder;
				if (mHolder.hasMetadata(keyName)) {
					for (MetadataValue mv : mHolder.getMetadata(keyName)) { // Get the latest value set by Skript
						if (mv.getOwningPlugin() == Skript.getInstance()) {
							if (((Map<String, Object>) mv.value()).containsKey(index))
								return true;
							break;
						}
					}
				}
			}
		} else {
			if (actualHolder.getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE))
				return true;
			if (holder instanceof Metadatable)
				return ((Metadatable) holder).hasMetadata(name);
		}

		return false;
	}

}
