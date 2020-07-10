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

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Offer Enchantment Level")
@Description({"The enchantment level of an enchantment offer.",
			"If the level is changed, it will always be at least 1.",
			"This change is cosmetic, and does not change the number of levels applied.", 
			"To change the number of levels applied, use the applied enchantment expression.",})
@Examples("set enchantment level of enchantment offer 1 to 3")
@Since("INSERT VERSION")
@RequiredPlugins("1.11 or newer")
public class ExprEnchantmentOfferLevel extends SimplePropertyExpression<EnchantmentOffer, Number>{

	static {
		if (Skript.classExists("org.bukkit.enchantments.EnchantmentOffer")) {
			register(ExprEnchantmentOfferLevel.class, Number.class, "[enchant[ment]] level", "enchantmentoffers");
		}
	}

	@Override
	public Number convert(final EnchantmentOffer offer) {
		return offer.getEnchantmentLevel();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		EnchantmentOffer[] offers = getExpr().getArray(event);
		if (offers.length == 0 || delta == null)
			return;
		int level = ((Number) delta[0]).intValue();
		if (level < 1) 
			return;
		switch (mode) {
			case SET:
				for (EnchantmentOffer offer : offers)
					offer.setEnchantmentLevel(level);
				break;
			case ADD:
				for (EnchantmentOffer offer : offers) {
					int add = offer.getEnchantmentLevel() + level;
					if (add < 1)
						return;
					offer.setEnchantmentLevel(add);
				}
				break;
			case REMOVE:
				for (EnchantmentOffer offer : offers) {
					int subtract = offer.getEnchantmentLevel() - level;
					if (subtract < 1)
						return;
					offer.setEnchantmentLevel(subtract);
				}
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "enchantment level";
	}

}
