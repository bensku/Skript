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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * @author Peter Güttinger
 */
public class InventorySlot extends Slot {
	
	private final Inventory invi;
	private final int index;
	
	public InventorySlot(final Inventory invi, final int index) {
		this.invi = invi;
		this.index = index;
	}
	
	public Inventory getInventory() {
		return invi;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	@Nullable
	public ItemStack getItem() {
		return invi.getItem(index) == null ? new ItemStack(Material.AIR, 1) : invi.getItem(index).clone();
	}
	
	@Override
	public void setItem(final @Nullable ItemStack item) {
		invi.setItem(index, item != null && item.getType() != Material.AIR ? item : null);
		if (invi instanceof PlayerInventory)
			PlayerUtils.updateInventory((Player) invi.getHolder());
	}
	
	@Override
	public String toString_i() {
		if (invi.getHolder() != null)
			return "slot " + index + " of inventory of " + Classes.toString(invi.getHolder());
		return "slot " + index + " of " + Classes.toString(invi);
	}

	@Override
	public boolean isSameSlot(Slot o) {
		if (o instanceof EquipmentSlot)
			return o.isSameSlot(this); // Reverse call to avoid code copy-paste
		return this.index == ((InventorySlot) o).getIndex();
	}
	
}
