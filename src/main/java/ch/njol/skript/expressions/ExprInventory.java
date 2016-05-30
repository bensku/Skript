/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Inventory")
@Description("The inventory of a block or player. You can usually omit this expression and can directly add or remove items to/from blocks or players.")
@Examples({"add a plank to the player's inventory",
		"clear the player's inventory",
		"remove 5 wool from the inventory of the clicked block"})
@Since("1.0")
public class ExprInventory extends SimplePropertyExpression<InventoryHolder, Inventory> {
	static {
		register(ExprInventory.class, Inventory.class, "inventor(y|ies)", "inventoryholders");
	}
	
	@Override
	@Nullable
	public Inventory convert(final InventoryHolder h) {
		return h.getInventory();
	}
	
	@Override
	public Class<Inventory> getReturnType() {
		return Inventory.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "inventor" + (getExpr().isSingle() ? "y" : "ies");
	}
	
}
