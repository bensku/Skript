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
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Offer Enchantment")
@Description({"The enchantment of an enchantment offer.", 
			"The level and cost of the enchantment should be set with their corresponding expressions.", 
			"This change is cosmetic, and does not affect what the item will be enchanted with.", 
			"To change the enchantment that is applied, use the applied enchantment expression."})
@Examples("set enchantment of enchantment offer 1 to sharpness")
@Since("INSERT VERSION")
@RequiredPlugins("1.11 or newer")
public class ExprEnchantmentOfferEnchantment extends SimplePropertyExpression<EnchantmentOffer, EnchantmentType> {

	static {
		if (Skript.classExists("org.bukkit.enchantments.EnchantmentOffer")) {
			register(ExprEnchantmentOfferEnchantment.class, EnchantmentType.class, "enchant[ment]", "enchantmentoffers");
		}
	}

	@SuppressWarnings("null")
	@Override
	public EnchantmentType convert(final EnchantmentOffer offer) {
		return Converters.convert(offer.getEnchantment(), EnchantmentType.class);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EnchantmentType.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		EnchantmentOffer[] offers = getExpr().getArray(event);
		if (offers.length == 0 || delta == null || delta.length == 0)
			return;
		EnchantmentType enchant = (EnchantmentType) delta[0];
		switch (mode) {
			case SET:
				for (EnchantmentOffer offer : offers) {
					offer.setEnchantment(enchant.getType());
					offer.setEnchantmentLevel(enchant.getLevel());
				}
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public Class<EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	protected String getPropertyName() {
		return "enchant[ment]";
	}

}
