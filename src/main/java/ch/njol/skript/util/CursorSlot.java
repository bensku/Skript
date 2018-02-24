/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.util;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Item that is in player's cursor.
 */
public class CursorSlot extends Slot {
	private final Player player;

	public CursorSlot(Player p) {
		this.player = p;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		return player.getItemOnCursor();
	}

	@Override
	public void setItem(final @Nullable ItemStack item) {
		player.setItemOnCursor(item);
		PlayerUtils.updateInventory(player);
	}

	@Override
	protected String toString_i() {
		return "cursor slot of inventory of " + Classes.toString(player);
	}

	@Override
	public boolean isSameSlot(final Slot o) {
		return o instanceof CursorSlot && ((CursorSlot) o).getPlayer().equals(this.player);
	}
}
