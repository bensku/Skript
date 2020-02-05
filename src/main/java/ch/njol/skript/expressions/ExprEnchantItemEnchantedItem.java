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

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchant Event Enchanted Item")
@Description("The enchanted item in an enchant event. It can be modified, but enchantments will still be applied.")
@Examples({"on enchant:",
			"\tset the enchanted item to a diamond chestplate"})
@Since("INSERT VERSION")
public class ExprEnchantItemEnchantedItem extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprEnchantItemEnchantedItem.class, ItemType.class, ExpressionType.SIMPLE, "[the] enchanted(-| )item");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EnchantItemEvent.class)) {
			Skript.error("The enchanted item is only usable in an enchant event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event e) {
		return new ItemType[]{new ItemType(((EnchantItemEvent) e).getItem())};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(ItemType.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		ItemType item = ((ItemType) delta[0]);
		EnchantItemEvent e = (EnchantItemEvent) event;
		switch (mode) {
			case SET:
				e.getItem().setType(item.getMaterial());
				e.getItem().setItemMeta(item.getItemMeta());
				e.getItem().setAmount(item.getAmount());
				break;
			case ADD:
			case REMOVE:
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "enchanted item";
	}

}