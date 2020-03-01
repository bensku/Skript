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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.log.SkriptLogger;

/**
 * @author APickledWalrus
 * 29th February, 2020
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Boolean> BOOLEAN = new PersistentDataUtils().new BooleanDataType();

	private final static PersistentDataType<?,?>[] types = new PersistentDataType<?,?>[]{
		PersistentDataType.STRING,
		PersistentDataType.LONG,
		PersistentDataType.DOUBLE,
		BOOLEAN
	};

	/**
	 * @return All {@linkplain PersistentDataType}s usable within Skript.
	 */
	public static PersistentDataType<?,?>[] getTypes() {
		return types;
	}

	/**
	 * To make PersistentData work well with Skript, the holder is not limited to just {@linkplain PersistentDataHolder}s.
	 * A holder can also be a {@linkplain Block} or an {@linkplain ItemType}.
	 * This gets the actual holder from those types.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @return The actual {@linkplain PersistentDataHolder}, or null if the object's actual holder can't be found.
	 */
	@Nullable
	public static PersistentDataHolder getActualHolder(Object holder) {
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
	 * This returns the value from the holder's persistent data container.
	 * @param holder A {@linkplain PersistentDataHolder}, a {@linkplain Block}, or an {@linkplain ItemType}.
	 * @param name The {@linkplain NamespacedKey} name.
	 * @return The value, or null if it was not found.
	 * @see PersistentDataUtils#getActualHolder(Object)
	 */
	@SuppressWarnings("null")
	@Nullable
	public static Object get(Object holder, String name) {

		PersistentDataHolder realHolder = getActualHolder(holder);
		if (realHolder == null)
			return null;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);
		Object get = null;

		// Try to guess the key type.
		for (PersistentDataType<?,?> type : types) {
			try {
				get = realHolder.getPersistentDataContainer().get(key, type);
				if (get != null)
					break;
			} catch (IllegalArgumentException e) {
				// This is thrown if a value exists under the key, but can't be accessed using the given type
			}
		}
		return get;

	}

	/**
	 * Used to set persistent data. This is essentially for cleanliness in element files.
	 * @param holder The PersistentDataHolder {@linkplain PersistentDataHolder}.
	 * @param name The {@linkplain NamespacedKey} name.
	 * @param value The value of the persistent data.
	 * @return Whether the persistent data was set.
	 */
	@SuppressWarnings("null")
	public static boolean set(Object holder, String name, Object value) {

		PersistentDataHolder realHolder = getActualHolder(holder);
		if (realHolder == null)
			return false;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);

		// Attempt to set the value based on the possible PersistentDataTypes.
		if (value instanceof Boolean) {
			realHolder.getPersistentDataContainer().set(key, BOOLEAN, (Boolean) value);
		} else if (value instanceof String) {
			realHolder.getPersistentDataContainer().set(key, PersistentDataType.STRING, (String) value);
		} else if (value instanceof Long) {
			realHolder.getPersistentDataContainer().set(key, PersistentDataType.LONG, (Long) value);
		} else if (value instanceof Double) {
			realHolder.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, (Double) value);
		} else {
			return false;
		}

		// This properly stores the data on ItemTypes and TileStates
		if (holder instanceof ItemType) {
			((ItemType) holder).setItemMeta((ItemMeta) realHolder);
		} else if (realHolder instanceof TileState) {
			((TileState) realHolder).update();
		}

		return true;

	}

	/**
	 * Removes a value from the persistent data container of the given holder.
	 * @param holder The PersistentDataHolder {@linkplain PersistentDataHolder}.
	 * @param name The {@linkplain NamespacedKey} name.
	 * @return Whether the value was removed. False returns mean that the holder does not have the value.
	 */
	public static boolean remove(Object holder, String name) {

		PersistentDataHolder realHolder = getActualHolder(holder);
		if (realHolder == null)
			return false;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);
		if (PersistentDataUtils.get(holder, name) == null)
			return false;
		realHolder.getPersistentDataContainer().remove(key);

		// This properly stores the data on ItemTypes and TileStates
		if (holder instanceof ItemType) {
			((ItemType) holder).setItemMeta((ItemMeta) realHolder);
		} else if (realHolder instanceof TileState) {
			((TileState) realHolder).update();
		}

		return true;

	}


	/*
	 * Custom PersistentDataTypes
	 */

	/**
	 * Boolean type for {@linkplain PersistentDataType}.
	 * 1 = true, 0 = false
	 */
	public class BooleanDataType implements PersistentDataType<byte[], Boolean> {

		@Override
		public Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@Override
		public Class<Boolean> getComplexType() {
			return Boolean.class;
		}

		@Override
		public byte[] toPrimitive(Boolean complex, PersistentDataAdapterContext context) {
			return new byte[]{(byte) (complex ? 1 : 0)};
				
		}

		@Override
		public Boolean fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
			return primitive[0] == 1;
		}

	}

}
