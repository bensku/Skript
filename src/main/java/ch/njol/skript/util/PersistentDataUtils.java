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
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.log.SkriptLogger;

/**
 * @author APickledWalrus
 * 29th February, 2020
 */
public class PersistentDataUtils {

	private final static PersistentDataType<byte[], Boolean> BOOLEAN = new PersistentDataUtils().new BooleanDataType();

	/**
	 * @return All {@linkplain PersistentDataType}s usable within Skript.
	 */
	@SuppressWarnings("null")
	public static PersistentDataType<?,?>[] getTypes() {
		List<PersistentDataType<?,?>> list = new ArrayList<>();

		list.add(PersistentDataType.STRING);
		list.add(PersistentDataType.LONG);
		list.add(PersistentDataType.DOUBLE);
		list.add(BOOLEAN);

		return list.toArray(new PersistentDataType<?,?>[4]);
	}

	/**
	 * 
	 * @param holder The PersistentDataHolder {@linkplain PersistentDataHolder}.
	 * @param name The {@linkplain NamespacedKey} name.
	 * @return The value, or null if it was not found.
	 */
	@SuppressWarnings("null")
	@Nullable
	public static Object get(PersistentDataHolder holder, String name) {
		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);
		Object get = null;
		for (PersistentDataType<?,?> type : getTypes()) {
			try {
				get = holder.getPersistentDataContainer().get(key, type);
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
	public static boolean set(PersistentDataHolder holder, String name, Object value) {
		SkriptLogger.log(Level.INFO, value.getClass().getName());
		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);
		if (value instanceof String)
			holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, (String) value);
		else if (value instanceof Long)
			holder.getPersistentDataContainer().set(key, PersistentDataType.LONG, (Long) value);
		else if (value instanceof Double)
			holder.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, (Double) value);
		else if (value instanceof Boolean)
			holder.getPersistentDataContainer().set(key, BOOLEAN, (Boolean) value);
		else
			return false;
		return true;
	}

	/**
	 * Removes a value from the persistent data container of the given holder.
	 * @param holder The PersistentDataHolder {@linkplain PersistentDataHolder}.
	 * @param name The {@linkplain NamespacedKey} name.
	 * @return Whether the value was removed. False returns mean that the holder does not have the value.
	 */
	public static boolean remove(PersistentDataHolder holder, String name) {
		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);
		if (PersistentDataUtils.get(holder, name) == null)
			return false;
		holder.getPersistentDataContainer().remove(key);
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
