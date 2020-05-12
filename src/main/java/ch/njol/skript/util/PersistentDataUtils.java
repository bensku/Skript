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
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.meta.BlockStateMeta;
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
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable.Value;

/**
 * @author APickledWalrus
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Value> SKRIPT_TYPE = new SkriptDataType();

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
	private static PersistentDataHolder getActualHolder(@Nullable Object holder) {
		if (holder == null) {
			return null;
		} else if (holder instanceof PersistentDataHolder) {
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
	 * From:
	 * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/NamespacedKey.java#33
	 */
	@SuppressWarnings("null")
	private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

	private static boolean isValidKey(String key) {
		return VALID_KEY.matcher(key).matches();
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
	public static String getKeyName(String name) {
		if (name.contains(" "))
			name = name.replace(" ", "");
		// TODO list support
		if (!isValidKey(name)) {
			Skript.warning("Invalid characters were used in a Persistent Data variable."
					+ " If you are trying to get it, it will not be returned."
					+ " If you are trying to change it, it will not be returned."
					+ " Valid characters are letters, numbers, periods, underscores, hyphens, and forward slashes."
					+ " If you believe this is a Skript issue, please create an issue with the appropriate details on GitHub."
			);
			return null;
		}
		return name;
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
	@Nullable
	public static Object get(Object holder, String name) {

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return null;

		String keyName = getKeyName(name);
		if (keyName == null)
			return null;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), keyName);

		Value value = actualHolder.getPersistentDataContainer().get(key, SKRIPT_TYPE);
		if (value != null)
			return Classes.deserialize(value.type, value.data);

		// Try to get as Metadata instead
		if (holder instanceof Metadatable) {
			Metadatable mHolder = (Metadatable) holder;
			List<MetadataValue> values = mHolder.getMetadata(keyName);
			// Get the most recent value
			return values.isEmpty() ? null : values.get(values.size() - 1).value();
		}

		return null;

	}

	/**
	 * Sets a key to a value in the holder's PersistentDataContainer.
	 * <b>Note:</b> If the given value can't be serialized by Skript, the value will be set in Metadata instead if possible.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @param value The value for the key to be set to.
	 * @return Whether the key was set to the value.
	 */
	public static boolean set(Object holder, String name, Object value) {

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		String keyName = getKeyName(name);
		if (keyName == null)
			return false;

		Value serialized = Classes.serialize(value);

		if (serialized != null) { // Can be serialized, set as PersistentData
			NamespacedKey key = new NamespacedKey(Skript.getInstance(), keyName);

			// Attempt to set the value based on the possible PersistentDataTypes.
			actualHolder.getPersistentDataContainer().set(key, SKRIPT_TYPE, serialized);

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
			mHolder.setMetadata(keyName, new FixedMetadataValue(Skript.getInstance(), value));
		}

		return true;

	}

	/**
	 * Removes a key/value from the holder's PersistentDataContainer.
	 * <b>Note:</b> If the given key can't be found in the holder's PersistentDataContainer, this method will try to
	 * find it under the holder's Metadata if possible. If a value is found, then it will be removed.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The key name in the PersistentDataContainer. It will go through conversion to replace some characters.
	 * @return Whether the value was removed. False returns mean that the holder does not have the value.
	 */
	public static boolean remove(Object holder, String name) {

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		String keyName = getKeyName(name);
		if (keyName == null)
			return false;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), keyName);

		if (actualHolder.getPersistentDataContainer().has(key, SKRIPT_TYPE)) {
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
			if (mHolder.hasMetadata(keyName)) {
				mHolder.removeMetadata(keyName, Skript.getInstance());
				return true;
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

		PersistentDataHolder actualHolder = getActualHolder(holder);
		if (actualHolder == null)
			return false;

		String keyName = getKeyName(name);
		if (keyName == null)
			return false;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), keyName);

		boolean hasPersistent = actualHolder.getPersistentDataContainer().has(key, SKRIPT_TYPE);
		if (hasPersistent)
			return true;

		if (holder instanceof Metadatable) {
			Metadatable mHolder = (Metadatable) holder;
			return mHolder.hasMetadata(keyName);
		}

		return false;

	}

	/*
	 * Allow PersistentData to work with all Skript types that can be serialized.
	 */

	private final static class SkriptDataType implements PersistentDataType<byte[], Value> {

		// This is how many bytes an int is.
		private final int INT_LENGTH = 4;

		// Charset used for converting bytes and Strings
		@SuppressWarnings("null")
		private final Charset STRING_CHARSET = StandardCharsets.UTF_8;

		public SkriptDataType() {}

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

}
