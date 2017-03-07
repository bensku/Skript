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
package ch.njol.skript.aliases;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class ItemData implements Cloneable, YggdrasilSerializable {
	static {
		Variables.yggdrasil.registerSingleClass(ItemData.class, "ItemData");
	}
	
	/**
	 * Only ItemType may set this directly.
	 */
	int typeid = -1;
	public short dataMin = -1;
	public short dataMax = -1;
	
	public ItemData(final int typeid) {
		this.typeid = typeid;
	}
	
	public ItemData(final int typeid, final short data) {
		if (data < -1)
			throw new IllegalArgumentException("data (" + data + ") must be >= -1");
		this.typeid = typeid;
		dataMin = dataMax = data;
	}
	
	public ItemData(final int typeid, final short dMin, final short dMax) {
		if (dMin < -1 || dMax < -1)
			throw new IllegalArgumentException("datas (" + dMin + "," + dMax + ") must be >= -1");
		if (dMin == -1 && dMax != -1 || dMin != -1 && dMax == -1)
			throw new IllegalArgumentException("dataMin (" + dMin + ") and dataMax (" + dMax + ") must either both be -1 or positive");
		if (dMin > dMax)
			throw new IllegalArgumentException("dataMin (" + dMin + ") must not be grater than dataMax (" + dMax + ")");
		this.typeid = typeid;
		dataMin = dMin;
		dataMax = dMax;
	}
	
	public ItemData() {
		typeid = -1;
		dataMin = -1;
		dataMax = -1;
	}
	
	public ItemData(final ItemStack i) {
		typeid = i.getTypeId();
		dataMin = dataMax = i.getDurability();// <- getData() returns a new data object based on the durability (see Bukkit source)
	}
	
	public ItemData(final ItemData other) {
		typeid = other.typeid;
		dataMax = other.dataMax;
		dataMin = other.dataMin;
	}
	
	public int getId() {
		return typeid;
	}
	
	/**
	 * Tests whether the given item is of this type.
	 * 
	 * @param item
	 * @return Whether the given item is of this type. If <tt>item</tt> is <tt>null</tt> this returns <tt>getId() == 0</tt>.
	 */
	public boolean isOfType(final @Nullable ItemStack item) {
		if (item == null)
			return typeid == 0;
		return isOfType(item.getTypeId(), item.getDurability());
	}
	
	public boolean isOfType(final int id, final short data) {
		return (typeid == -1 || typeid == id) && (dataMin == -1 || dataMin <= data) && (dataMax == -1 || data <= dataMax);
	}
	
	public boolean isSupertypeOf(final ItemData other) {
		return (typeid == -1 || other.typeid == typeid) && (dataMin == -1 || dataMin <= other.dataMin) && (dataMax == -1 || dataMax >= other.dataMax);
	}
	
	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(int, short, short, boolean) getMaterialName}(typeid, dataMin, dataMax, false)</code>
	 */
	@Override
	public String toString() {
		return Aliases.getMaterialName(typeid, dataMin, dataMax, false);
	}
	
	public String toString(final boolean debug, final boolean plural) {
		return debug ? Aliases.getDebugMaterialName(typeid, dataMin, dataMax, plural) : Aliases.getMaterialName(typeid, dataMin, dataMax, plural);
	}
	
	/**
	 * @return The item's gender or -1 if no name is found
	 */
	public int getGender() {
		return Aliases.getGender(typeid, dataMin, dataMax);
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ItemData))
			return false;
		final ItemData other = (ItemData) obj;
		return other.typeid == typeid && other.dataMin == dataMin && other.dataMax == dataMax;
	}
	
	@Override
	public int hashCode() {
		return (typeid * 31 + dataMin) * 31 + dataMax;
	}
	
	/**
	 * Computes the intersection of two ItemDatas. The data range of the returned item data will be the real intersection of the two data ranges, and the type id will be the one
	 * set if any.
	 * 
	 * @param other
	 * @return A new ItemData which is the intersection of the given types, or null if the intersection of the data ranges is empty or both datas have an id != -1 which are not the
	 *         same.
	 */
	@Nullable
	public ItemData intersection(final ItemData other) {
		if (other.dataMin != -1 && dataMin != -1 && (other.dataMax < dataMin || dataMax < other.dataMin) || other.typeid != -1 && typeid != -1 && other.typeid != typeid)
			return null;
		
		return new ItemData(typeid == -1 ? other.typeid : typeid,
				(short) Math.max(dataMin, other.dataMin),
				dataMax == -1 ? other.dataMax : (other.dataMax == -1 ? dataMax : (short) Math.min(dataMax, other.dataMax)));
	}
	
	public ItemStack getRandom() {
		int type = typeid;
		if (type == -1) {
			final Material m = CollectionUtils.getRandom(Material.values(), 1);
			assert m != null;
			type = m.getId();
		}
		if (dataMin == -1 && dataMax == -1) {
			return new ItemStack(type, 1);
		} else {
			return new ItemStack(type, 1, (short) (Utils.random(dataMin, dataMax + 1)));
		}
	}
	
	public Iterator<ItemStack> getAll() {
		if (typeid == -1) {
			return new Iterator<ItemStack>() {
				
				@SuppressWarnings("null")
				private final Iterator<Material> iter = Arrays.asList(Material.values()).listIterator(1); // ignore air
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public ItemStack next() {
					return new ItemStack(iter.next(), 1);
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		if (dataMin == dataMax)
			return new SingleItemIterator<ItemStack>(new ItemStack(typeid, 1, dataMin == -1 ? 0 : dataMin));
		return new Iterator<ItemStack>() {
			
			private short data = dataMin;
			
			@Override
			public boolean hasNext() {
				return data <= dataMax;
			}
			
			@Override
			public ItemStack next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return new ItemStack(typeid, 1, data++);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public ItemData clone() {
		return new ItemData(this);
	}
	
	public boolean hasDataRange() {
		return dataMin != dataMax;
	}
	
	public int numItems() {
		return dataMax - dataMin + 1;
	}
	
}
