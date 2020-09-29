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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import ch.njol.skript.variables.SerializedVariable.Value;

/**
 * This {@link PersistentDataType} is used for list variables.
 * In this case, a list variable is any variable containing "::" (the separator)
 * The map's key is the variable's index and the map's value is the index's value.
 * With this {@link PersistentDataType}, the NamespacedKey's key is the rest of the list variable.
 * e.g. {one::two::three} where "one//two" would be the {@link org.bukkit.NamespacedKey}'s key and "three" the key for the map.
 * @see PersistentDataUtils#getNamespacedKey(String)
 * @see PersistentDataUtils
 * @author APickledWalrus
 */
public final class ListVariablePersistentDataType implements PersistentDataType<byte[], Map<String, Value>> {

	// This is how many bytes an int is.
	private final int INT_LENGTH = 4;

	// Charset used for converting bytes and Strings
	@SuppressWarnings("null")
	private final Charset SERIALIZED_CHARSET = StandardCharsets.UTF_8;

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
		int bufferLength = 0;

		for (Entry<String, Value> entry : complex.entrySet()) {
			// Store it: index -> type -> data
			bufferLength += INT_LENGTH + entry.getKey().getBytes(SERIALIZED_CHARSET).length
						+ INT_LENGTH + entry.getValue().type.getBytes(SERIALIZED_CHARSET).length 
						+ INT_LENGTH + entry.getValue().data.length;
		}

		ByteBuffer bb = ByteBuffer.allocate(bufferLength);

		for (Entry<String, Value> entry : complex.entrySet()) {
			byte[] indexBytes = entry.getKey().getBytes(SERIALIZED_CHARSET);
			byte[] typeBytes = entry.getValue().type.getBytes(SERIALIZED_CHARSET);

			bb.putInt(indexBytes.length);
			bb.put(indexBytes);

			bb.putInt(typeBytes.length);
			bb.put(typeBytes);

			bb.putInt(entry.getValue().data.length);
			bb.put(entry.getValue().data);
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
			String index = new String(indexBytes, SERIALIZED_CHARSET);

			int typeLength = bb.getInt();
			byte[] typeBytes = new byte[typeLength];
			bb.get(typeBytes, 0, typeLength);
			String type = new String(typeBytes, SERIALIZED_CHARSET);

			int dataLength = bb.getInt();
			byte[] dataBytes = new byte[dataLength];
			bb.get(dataBytes, 0, dataLength);

			values.put(index, new Value(type, dataBytes));
		}

		return values;
	}

}
