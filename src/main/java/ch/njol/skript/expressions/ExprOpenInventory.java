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
package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Open Inventory")
@Description({"Return the open inventory of a player.",
	"If no inventory is open, it returns the own player's crafting inventory."})
@Examples({"set slot 1 of open inventory of player to diamond sword"})
@Since("2.2-dev24")
public class ExprOpenInventory extends SimplePropertyExpression<Player, Inventory>{
	static {
		register(ExprOpenInventory.class, Inventory.class, "(current|open|top) inventory", "player");
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@Override
	protected String getPropertyName() {
		return "open inventory";
	}

	@Override
	@Nullable
	public Inventory convert(Player p) {
		return p.getOpenInventory() != null ? p.getOpenInventory().getTopInventory() : null;
	}
	
}
