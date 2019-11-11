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


import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;

@Name("Item with Enchantments")
@Description("Returns the given item type with the specified enchantments added to it.")
@Examples({"give player a diamond sword of sharpness 2", "give player 1 of {_item} with sharpness and mending"})
@Since("INSERT VERSION")
public class ExprItemWithEnchant extends PropertyExpression<ItemType, ItemType> {
	
	static {
		Skript.registerExpression(ExprItemWithEnchant.class, ItemType.class, ExpressionType.PROPERTY,
			"%itemtype% (of|with) %enchantmenttypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<EnchantmentType> enchants;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		enchants = (Expression<EnchantmentType>) exprs[1];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected ItemType[] get(Event e, ItemType[] source) {
		EnchantmentType[] enchants = this.enchants.getArray(e);
		return get(source, item -> {
			ItemMeta meta = item.getItemMeta();
			for (EnchantmentType enchant : enchants) {
				meta.addEnchant(enchant.getType(), enchant.getLevel(), true);
			}
			item.setItemMeta(meta);
			return item;
		});
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return getExpr().toString(e, d) + " with enchantment " + enchants.toString(e, d);
	}
	
}
