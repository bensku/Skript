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
package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.ItemStack;

@Name("Is Transparent")
@Description("Checks whether an item is transparent.")
@Examples({"glass is transparent", "player's tool is transparent."})
@Since("2.2-dev36")
public class CondIsTransparent extends PropertyCondition<ItemStack> {

	static {
		PropertyCondition.register(CondIsTransparent.class, "transparent", "itemstacks");
	}

	@Override
	public boolean check(ItemStack i) {
		return i.getType().isTransparent();
	}

	@Override
	protected String getPropertyName() {
		return "transparent";
	}

}
