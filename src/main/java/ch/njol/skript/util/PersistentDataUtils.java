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
package ch.njol.skript.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.CondHasRelationalVariable;
import ch.njol.skript.expressions.ExprRelationalVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable.Value;

/**
 * This class allows Persistent Data to work properly with Skript.
 * In Skript, Persistent Data is formatted like variables.
 * This looks like: <b>set persistent data {isAdmin} of player to true</b>
 * @author APickledWalrus
 * @see SingleVariablePersistentDataType
 * @see ListVariablePersistentDataType
 * @see ExprRelationalVariable
 * @see CondHasRelationalVariable
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Value> SINGLE_VARIABLE_TYPE = new SingleVariablePersistentDataType();
	private final static PersistentDataType<byte[], Map<String, Value>> LIST_VARIABLE_TYPE = new ListVariablePersistentDataType();

	/*
	 * General Utility Methods
	 */

	/**
	 * For a {@link Block} or an {@link ItemType}, only parts/some of them are actually a {@link PersistentDataHolder}.
	 * This gets the part that is a  {@link PersistentDataHolder} from those types (e.g. ItemMeta or TileState).
	 * @param holders A {@link PersistentDataHolder}, a {@link Block}, or an {@link ItemType}.
	 * @return A map keyed by the unconverted holder with the converted holder as its value.
	 */
	private static Map<Object, PersistentDataHolder> getConvertedHolders(Object[] holders) {
		Map<Object, PersistentDataHolder> actualHolders = new HashMap<>();
		for (Object holder : holders) {
			if (holder instanceof PersistentDataHolder) {
				actualHolders.put(holder, (PersistentDataHolder) holder);
			} else if (holder instanceof ItemType) {
				actualHolders.put(holder, ((ItemType) holder).getItemMeta());
			} else if (holder instanceof Block && ((Block) holder).getState() instanceof TileState) {
				actualHolders.put(holder, ((TileState) ((Block) holder).getState()));
			}
		}
		return actualHolders;
	}
	
	/**
	 * This returns a {@link NamespacedKey} from the provided name with Skript as the namespace being used.
	 * The name will be encoded in Base64 to make sure the key name is valid.
	 * @param name The name to convert
	 * @return The created {@link NamespacedKey}
	 */
	@SuppressWarnings("null")
	public static NamespacedKey getNamespacedKey(String name) {
		// Encode the name in Base64 to make sure the key name is valid
		name = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)).replace('=', '_').replace('+', '.');
		return new NamespacedKey(Skript.getInstance(), name);
	}

	/*
	 * Single Variable Modification Methods
	 */

	/**
	 * Gets the Persistent Data Tag's value of the given single variable name from the given holder.
	 * If the value set was not serializable, it was set under Metadata and is retrieved from Metadata here.
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * @return The Persistent Data Tag's value from each holder, or an empty list if no values could be retrieved.
	 * @see PersistentDataUtils#setSingle(String, Object, Object...)
	 * @see PersistentDataUtils#removeSingle(String, Object...)
	 */
	public static Object[] getSingle(String name, Object... holders) {
		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return new Object[0];

		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return new Object[0];

		name = "!!SINGLE!!" + name;
		NamespacedKey key = getNamespacedKey(name);

		List<Object> returnValues = new ArrayList<>();
		for (Entry<Object, PersistentDataHolder> entry : actualHolders.entrySet()) {
			Object holder = entry.getKey();
			PersistentDataHolder actualHolder = entry.getValue();
			if (actualHolder.getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE)) {
				Value value = actualHolder.getPersistentDataContainer().get(key, SINGLE_VARIABLE_TYPE);
				if (value != null)
					 returnValues.add(Classes.deserialize(value.type, value.data));
			}
			// Try to get as Metadata instead
			if (holder instanceof Metadatable) {
				List<MetadataValue> values = ((Metadatable) holder).getMetadata(name);
				for (MetadataValue mv : values) {
					if (mv.getOwningPlugin() == Skript.getInstance()) // Get the latest value set by Skript
						returnValues.add(mv.value());
				}
			}
		}

		return returnValues.toArray();
	}

	/**
	 * Sets the Persistent Data Tag from the given name and value for the given holder.
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @param value The value for the Persistent Data Tag to be set to.
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * If this value is not serializable (see {@link Classes#serialize(Object)}), this value will be set under Metadata.
	 * @see PersistentDataUtils#getSingle(String, Object...)
	 * @see PersistentDataUtils#removeSingle(String, Object...)
	 */
	public static void setSingle(String name, Object value, Object... holders) {
		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return;

		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return;

		name = "!!SINGLE!!" + name;
		Value serialized = Classes.serialize(value);

		if (serialized != null) { // Can be serialized, set as Persistent Data
			NamespacedKey key = getNamespacedKey(name);
			for (Entry<Object, PersistentDataHolder> entry : actualHolders.entrySet()) {
				PersistentDataHolder actualHolder = entry.getValue();

				actualHolder.getPersistentDataContainer().set(key, SINGLE_VARIABLE_TYPE, serialized);

				// This is to store the data on the ItemType or TileState
				Object holder = entry.getKey();
				if (holder instanceof ItemType) {
					((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
				} else if (actualHolder instanceof TileState) {
					((TileState) actualHolder).update();
				}
			}
		} else { // Set as Metadata instead
			for (Object holder : actualHolders.keySet()) {
				if (holder instanceof Metadatable)
					((Metadatable) holder).setMetadata(name, new FixedMetadataValue(Skript.getInstance(), value));
			}
		}
	}

	/**
	 * Removes the Persistent Data Tag's value for the given holder(s) from the given name and value.
	 * This method will check the holder's {@link org.bukkit.persistence.PersistentDataContainer} and Metadata.
	 * @param name The name of the single variable (e.g. <b>"myVariable" from {myVariable}</b>)
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * @see PersistentDataUtils#getSingle(String, Object...)
	 * @see PersistentDataUtils#setSingle(String, Object, Object...)
	 */
	public static void removeSingle(String name, Object... holders) {
		if (name.contains(Variable.SEPARATOR)) // This is a list variable..
			return;

		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return;

		name = "!!SINGLE!!" + name;
		NamespacedKey key = getNamespacedKey(name);

		for (Entry<Object, PersistentDataHolder> entry : actualHolders.entrySet()) {
			Object holder = entry.getKey();
			PersistentDataHolder actualHolder = entry.getValue();

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
	}

	/*
	 * List Variable Modification Methods
	 */

	/**
	 * Gets the Persistent Data Tag's value of the given list variable name from the given holder(s).
	 * This method may return a single value, or multiple, depending on the given name.
	 * If the value set was not serializable, it was set under Metadata and is retrieved from Metadata here.
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * @return The Persistent Data Tag's value(s) from the holder, or an empty array if: 
	 * the holder was invalid, the name was invalid, the key was invalid, or if no value(s) could be found.
	 * @see PersistentDataUtils#setList(String, Object, Object...)
	 * @see PersistentDataUtils#removeList(String, Object...)
	 * @see PersistentDataUtils#getListMap(String, Object)
	 */
	@SuppressWarnings("null")
	public static Object[] getList(String name, Object... holders) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return new Object[0];

		// Format the variable for getListMap (e.g. {varName::*})
		// We don't need to worry about the name being invalid, as getListMap will handle that
		String listName = name;
		if (!name.endsWith("*"))
			listName = name.substring(0, name.lastIndexOf(Variable.SEPARATOR) + 2) + "*";

		List<Object> returnValues = new ArrayList<>();
		for (Object holder : holders) {
			Map<String, Object> listVar = getListMap(listName, holder);
			if (listVar == null) // One of our values was invalid
				continue;

			String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());
			if (index.equals("*")) { // Return all values
				returnValues.addAll(listVar.values());
			} else if (listVar.containsKey(index)){ // Return the value under the given index (if it exists)
				returnValues.add(listVar.get(index));
			}
		}
		return returnValues.toArray();
	}

	/**
	 * Sets the Persistent Data Tag's value for the given holder(s) from the given list variable name and value.
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If the index of the name is "*", then the index set in the list will be "1".
	 * To set a different index, format the list variable like normal (e.g. <b>"myList::index" from {myList::index}</b>)
	 * @param value The value for the Persistent Data Tag to be set to.
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * If this value is not serializable (see {@link Classes#serialize(Object)}), this value will be set under Metadata.
	 * @see PersistentDataUtils#getList(String, Object...)
	 * @see PersistentDataUtils#removeSingle(String, Object...)
	 * @see PersistentDataUtils#setListMap(String, Map, Object)
	 */
	@SuppressWarnings("unchecked")
	public static void 	setList(String name, Object value, Object... holders) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return;

		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return;

		Value serialized = Classes.serialize(value);

		String keyName = "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR));

		if (serialized != null) {  // Can be serialized, set as Persistent Data
			NamespacedKey key = getNamespacedKey(keyName);
			for (Entry<Object, PersistentDataHolder> entry : actualHolders.entrySet()) {
				PersistentDataHolder actualHolder = entry.getValue();

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
				Object holder = entry.getKey();
				if (holder instanceof ItemType) {
					((ItemType) holder).setItemMeta((ItemMeta) actualHolder);
				} else if (actualHolder instanceof TileState) {
					((TileState) actualHolder).update();
				}
			}
		} else { // Try to set as Metadata instead
			for (Object holder : actualHolders.keySet()) {
				if (holder instanceof Metadatable) {
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
		}
	}

	/**
	 * Removes the value of the Persistent Data Tag of the given name for the given holder(s).
	 * This method will check the holder's {@link org.bukkit.persistence.PersistentDataContainer} and Metadata.
	 * @param name The name of the list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * If the index of the name is "*", then the entire list will be cleared.
	 * To remove a specific index, format the list variable like normal (e.g. <b>"myList::index" from {myList::index}</b>)
	 * @see PersistentDataUtils#getList(String, Object...)
	 * @see PersistentDataUtils#setList(String, Object, Object...)
	 */
	@SuppressWarnings({"unchecked"})
	public static void removeList(String name, Object... holders) {
		if (!name.contains(Variable.SEPARATOR)) // This is a single variable..
			return;

		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return;

		String keyName = "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);

		String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());
		
		for (Entry<Object, PersistentDataHolder> entry : actualHolders.entrySet()) {
			Object holder = entry.getKey();
			PersistentDataHolder actualHolder = entry.getValue();

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
	}

	/**
	 * Returns the map of a list variable. Keyed by variable index.
	 * This method will check the holder's {@link org.bukkit.persistence.PersistentDataContainer} and Metadata.
	 * @param name The full list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * @param holder The holder of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * If it is not provided in this format, a null value will be returned.
	 * @return The map of a list variable, or null if:
	 * If name was provided in an incorrect format, the holder is invalid, or if no value is set under that name for the holder.
	 * @see PersistentDataUtils#getList(String, Object...)
	 * @see PersistentDataUtils#setListMap(String, Map, Object)
	 */
	@Nullable
	@SuppressWarnings({"null", "unchecked"})
	public static Map<String, Object> getListMap(String name, Object holder) {
		if (!name.endsWith("*")) // Make sure we're getting a whole list
			return null;

		PersistentDataHolder actualHolder = getConvertedHolders(new Object[]{holder}).get(holder);
		if (actualHolder == null)
			return null;

		String keyName = "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);

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
	 * This map <i>should</i> be gotten from {@link PersistentDataUtils#getListMap(String, Object)}
	 * This method will check the holder's {@link org.bukkit.persistence.PersistentDataContainer} and Metadata.
	 * @param name The full list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If it is not provided in this format, nothing will be set.
	 * @param varMap The new map for Persistent Data Tag of the given holder.
	 * If a variable map doesn't already exist in the holder's {@link org.bukkit.persistence.PersistentDataContainer},
	 * this map will be set in their Metadata.
	 * @param holder The holder of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * @see PersistentDataUtils#setList(String, Object, Object...)
	 * @see PersistentDataUtils#getListMap(String, Object)
	 */
	public static void setListMap(String name, Map<String, Object> varMap, Object holder) {
		if (!name.endsWith("*")) // Make sure we're setting a whole list
			return;

		if (varMap.isEmpty()) { // If the map is empty, remove the whole value instead.
			removeList(name, holder);
			return;
		}

		PersistentDataHolder actualHolder = getConvertedHolders(new Object[]{holder}).get(holder);
		if (actualHolder == null)
			return;

		String keyName = "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);

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

	/**
	 * This returns the indexes of a stored list.
	 * Mainly used for the ADD changer in {@link ExprRelationalVariable}
	 * @param name The full list variable (e.g. <b>"myList::*" from {myList::*}</b>)
	 * If it is not provided in this format, nothing will be set.
	 * @param holder The holder of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * @return The set of indexes, or an empty String set.
	 */
	@Nullable
	@SuppressWarnings({"null", "unchecked"})
	public static Set<String> getListIndexes(String name, Object holder) {
		if (!name.endsWith("*")) // Make sure we're getting a whole list
			return null;

		PersistentDataHolder actualHolder = getConvertedHolders(new Object[]{holder}).get(holder);
		if (actualHolder == null)
			return null;

		String keyName = "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR));
		NamespacedKey key = getNamespacedKey(keyName);

		if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) { // It exists under Persistent Data
			return actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE).keySet();
		} else if (holder instanceof Metadatable) { // Check Metadata
			for (MetadataValue mv : ((Metadatable) holder).getMetadata(keyName)) { // Get the latest value set by Skript
				if (mv.getOwningPlugin() == Skript.getInstance())
					return ((Map<String, Object>) mv.value()).keySet();
			}
		}

		return null;
	}

	/*
	 * Other Utility Methods
	 */

	/**
	 * Whether the given holders have a value under the given name.
	 * This method will check the holder's {@link org.bukkit.persistence.PersistentDataContainer} and Metadata.
	 * @param name The name of the variable
	 * @param holders The holder(s) of the Persistent Data Tag. See {@link PersistentDataUtils#getConvertedHolders(Object[])}
	 * (e.g. <b>"myVariable" from {myVariable}</b> OR <b>"myList::index" from {myList::index}</b> OR <b>"myList::*" from {myList::*}</b>)
	 * @return True if the user has something under the Persistent Data Tag from the given name.
	 * This method will return false if: the holder is invalid, the name is invalid, or if no value could be found.
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static boolean has(String name, Object... holders) {
		Map<Object, PersistentDataHolder> actualHolders = getConvertedHolders(holders);
		if (actualHolders.isEmpty())
			return false;
		
		boolean isList = name.contains(Variable.SEPARATOR);
		String keyName = isList ? "!!LIST!!" + name.substring(0, name.lastIndexOf(Variable.SEPARATOR)) : "!!SINGLE!!" + name;
		NamespacedKey key = getNamespacedKey(keyName);

		if (isList) {
			for (Entry<Object, PersistentDataHolder> entry: actualHolders.entrySet()) {
				Object holder = entry.getKey();
				PersistentDataHolder actualHolder = entry.getValue();

				String index = name.substring(name.lastIndexOf(Variable.SEPARATOR) + Variable.SEPARATOR.length());

				if (actualHolder.getPersistentDataContainer().has(key, LIST_VARIABLE_TYPE)) {
					if (index.equals("*")) // There will NEVER be an empty map stored.
						continue;
					if (actualHolder.getPersistentDataContainer().get(key, LIST_VARIABLE_TYPE).containsKey(index))
						continue;
					return false;
				}
				if (holder instanceof Metadatable) {
					Metadatable mHolder = (Metadatable) holder;
					if (!mHolder.hasMetadata(keyName)) {
						return false;
					} else { // They have something under that key name, check the values.
						for (MetadataValue mv : mHolder.getMetadata(keyName)) { // Get the latest value set by Skript
							if (mv.getOwningPlugin() == Skript.getInstance() && !((Map<String, Object>) mv.value()).containsKey(index))
								return false;
						}
					}
				}
			}
		} else {
			for (Entry<Object, PersistentDataHolder> entry: actualHolders.entrySet()) {
				if (entry.getValue().getPersistentDataContainer().has(key, SINGLE_VARIABLE_TYPE))
					continue;
				if (((Metadatable) entry.getKey()).hasMetadata(keyName))
					continue;
				return false;
			}
		}
		return true;
	}

}
