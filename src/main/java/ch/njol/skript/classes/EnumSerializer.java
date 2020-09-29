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
package ch.njol.skript.classes;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.yggdrasil.ClassResolver;
import ch.njol.yggdrasil.Fields;

/**
 * Mainly kept for backwards compatibility, but also serves as {@link ClassResolver} for enums.
 * 
 * @author Peter Güttinger
 */
public class EnumSerializer<T extends Enum<T>> extends Serializer<T> {
	
	private final Class<T> c;
	
	public EnumSerializer(final Class<T> c) {
		this.c = c;
	}
	
	@Override
	@Deprecated
	@Nullable
	public T deserialize(final String s) {
		try {
			return Enum.valueOf(c, s);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		assert false;
		return false;
	}
	
	@Override
	public Fields serialize(final T t) {
		throw new IllegalStateException(); // not used
	}
	
	@Override
	public void deserialize(final T o, final Fields f) {
		assert false;
	}
	
}
