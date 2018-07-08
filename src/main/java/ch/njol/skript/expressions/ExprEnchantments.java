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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Item Enchantments")
@Description("All the enchantments an <a href='classes.html#itemtype>item type</a> has.")
@Examples("clear enchantments of event-item")
@Since("2.2-dev36")
public class ExprEnchantments extends SimpleExpression<EnchantmentType> {

	static {
		PropertyExpression.register(ExprEnchantments.class, EnchantmentType.class, "enchantments", "itemtypes");
	}

	@SuppressWarnings("null")
	private Expression<ItemType> items;

	@SuppressWarnings({"null","unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		return true;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	@Nullable
	protected EnchantmentType[] get(Event e) {
		List<EnchantmentType> enchantments = new ArrayList<>();
		for (ItemType item : items.getArray(e)) {
			Map<Enchantment, Integer> enchants = item.getEnchantments();
			if (enchants == null)
				continue;
			for (Entry<Enchantment, Integer> enchant : enchants.entrySet())
					enchantments.add(new EnchantmentType(enchant.getKey(), enchant.getValue()));

		}
		return enchantments.toArray(new EnchantmentType[enchantments.size()]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(EnchantmentType[].class, Enchantment[].class);
	}


	// TODO: improve changer once aliases rework is done
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		ItemType[] source = items.getArray(e);
		Map<Enchantment, Integer> enchantments = new HashMap<>();
		if (mode != Changer.ChangeMode.DELETE || mode != Changer.ChangeMode.RESET) {
			assert delta != null;
			for (Object enchant : delta) {
				if (enchant instanceof EnchantmentType) {
					EnchantmentType enchantment = (EnchantmentType) enchant;
					if (enchantment.getType() != null)
						enchantments.put(enchantment.getType(), enchantment.getLevel());
				} else {
					enchantments.put((Enchantment) enchant, -1);
				}
			}
			if (mode == Changer.ChangeMode.SET ||mode == Changer.ChangeMode.ADD)
				enchantments.replaceAll((enchant, level) -> level == -1 ? 1 : level);
		}
		switch (mode) {
			case ADD:
				changeEnchantments(item -> {
					item.addEnchantments(enchantments);
				}, source);
				break;
			case REMOVE:
			case REMOVE_ALL:
				changeEnchantments(item -> {
					Map<Enchantment, Integer> enchants = item.getEnchantments();
					assert enchants != null;
					enchantments.forEach((enchant, level) -> {
						if (level == -1)
							enchants.remove(enchant);
						else
							enchants.remove(enchant, level);
					});
					item.clearEnchantments();
					item.addEnchantments(enchants);
				}, source);
				break;
			case SET:
				changeEnchantments(item -> {
					item.clearEnchantments();
					item.addEnchantments(enchantments);
				}, source);
				break;
			case DELETE:
			case RESET:
				changeEnchantments(ItemType::clearEnchantments, source);
		}
	}

	@Override
	public Class<? extends EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "" + String.format("the enchantments of %s", items.toString(e, debug));
	}

	private static void changeEnchantments(Consumer<ItemType> consumer, ItemType... items) {
		for (ItemType item : items) {
			consumer.accept(item);
		}
	}

}
