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

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;

@Name("Damaged Item")
@Description("Directly damages an item. In MC versions 1.12.2 and lower, this can be used to apply data values to items/blocks")
@Examples({"give player diamond sword with damage value 100", "set player's tool to diamond hoe with damage 250",
		"give player diamond sword with damage 700 named \"BROKEN SWORD\"",
		"set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"",
		"set target block of player to dirt with data value 1", "set target block of player to potato plant with data value 7"})
@Since("INSERT VERSION")
public class ExprDamagedItem extends PropertyExpression<ItemType, ItemType> {
	
	static {
		Skript.registerExpression(ExprDamagedItem.class, ItemType.class, ExpressionType.COMBINED,
				"%itemtype% with (damage|data) [value] %number%",
				"%itemtype% damaged by %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> damage;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		damage = (Expression<Number>) exprs[1];
		return true;
	}
	
	@SuppressWarnings({"deprecation"})
	@Override
	protected ItemType[] get(Event e, ItemType[] source) {
		Number value = damage.getSingle(e) != null ? damage.getSingle(e) : 0;
		return get(source, new Getter<ItemType, ItemType>() {
			@Override
			public ItemType get(ItemType item) {
				item = item.clone();
				if (Skript.isRunningMinecraft(1, 13)) {
					ItemMeta meta = item.getItemMeta();
					((Damageable) meta).setDamage(value != null ? value.intValue() : 0);
					item.setItemMeta(meta);
				} else {
					ItemStack stack = new ItemStack(item.getRandom());
					stack.setDurability(value != null ? value.shortValue() : 0);
					item = new ItemType(stack);
				}
				return item;
			}
		});
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, boolean debug) {
		return getExpr().toString(e, debug) + " with damage value " + damage;
	}
	
}
