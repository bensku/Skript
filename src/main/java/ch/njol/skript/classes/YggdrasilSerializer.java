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

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

/**
 * @author Peter Güttinger
 */
public class YggdrasilSerializer<T extends YggdrasilSerializable> extends Serializer<T> {
	
	@Override
	public Fields serialize(final T o) throws NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			return ((YggdrasilExtendedSerializable) o).serialize();
		return new Fields(o);
	}
	
	@Override
	public void deserialize(final T o, final Fields f) throws StreamCorruptedException, NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			((YggdrasilExtendedSerializable) o).deserialize(f);
		else
			f.setFields(o);
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		return true;
	}
	
}
