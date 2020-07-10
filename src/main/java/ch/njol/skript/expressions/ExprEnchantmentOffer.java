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
import java.util.Arrays;
import java.util.List;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Offer")
@Description("The enchantment offer in enchant prepare events.")
@Examples({"on enchant prepare:",
			"\tsend \"Your enchantment offers are: %the enchantment offers%\" to player"})
@Since("INSERT VERSION")
@Events("enchant prepare")
@RequiredPlugins("1.11 or newer")
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentOffer> {

	static {
		if (Skript.classExists("org.bukkit.enchantments.EnchantmentOffer")) {
			Skript.registerExpression(ExprEnchantmentOffer.class, EnchantmentOffer.class, ExpressionType.SIMPLE, 
					"[all [of]] [the] enchant[ment] offers",
					"enchant[ment] offer[s] %numbers%",
					"[the] %number%(st|nd|rd|th) enchant[ment] offer");
		}
	}

	@SuppressWarnings("null")
	private Expression<Number> exprOfferNumber;

	private boolean all;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("Enchantment offers are only usable in enchant prepare events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (matchedPattern == 0) {
			all = true;
		} else {
			exprOfferNumber = (Expression<Number>) exprs[0];
			all = false;
		}
		return true;
	}

	@SuppressWarnings({"null", "unused"})
	@Override
	@Nullable
	protected EnchantmentOffer[] get(Event e) {
		if (all)
			return ((PrepareItemEnchantEvent) e).getOffers();
		if (exprOfferNumber == null)
			return new EnchantmentOffer[0];
		if (exprOfferNumber.isSingle()) {
			Number offerNumber = exprOfferNumber.getSingle(e);
			if (offerNumber == null)
				return new EnchantmentOffer[0];
			int offer = offerNumber.intValue();
			if (offer < 1 || offer > ((PrepareItemEnchantEvent) e).getOffers().length)
				return new EnchantmentOffer[0];
			return new EnchantmentOffer[]{((PrepareItemEnchantEvent) e).getOffers()[offer - 1]};
		}
		List<EnchantmentOffer> offers = new ArrayList<>();
		int i;
		for (Number n : exprOfferNumber.getArray(e)) {
			i = n.intValue();
			if (i >= 1 || i <= ((PrepareItemEnchantEvent) e).getOffers().length)
				offers.add(((PrepareItemEnchantEvent) e).getOffers()[i - 1]);
		}
		return offers.toArray(new EnchantmentOffer[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.DELETE)
			return CollectionUtils.array(EnchantmentOffer.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		List<Number> offerNumbers = new ArrayList<>();
		if (exprOfferNumber != null) {
			if (exprOfferNumber.isSingle())
				offerNumbers.add(exprOfferNumber.getSingle(e));
			else
				offerNumbers.addAll(Arrays.asList(exprOfferNumber.getArray(e)));
		}
		if (e instanceof PrepareItemEnchantEvent) {
			switch (mode) {
				case DELETE:
					if (all) {
						Arrays.fill(((PrepareItemEnchantEvent) e).getOffers(), null);
					} else {
						int i;
						for (Number n : offerNumbers) {
							i = n.intValue();
							((PrepareItemEnchantEvent) e).getOffers()[i - 1] = null;
						}
					}
					break;
				case SET:
				case ADD:
				case REMOVE:
				case RESET:
				case REMOVE_ALL:
					assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return !all && exprOfferNumber.isSingle();
	}

	@Override
	public Class<? extends EnchantmentOffer> getReturnType() {
		return EnchantmentOffer.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return all ? "the enchantment offers" : "enchantment offer(s) " + exprOfferNumber.toString(e, debug);
	}

}
