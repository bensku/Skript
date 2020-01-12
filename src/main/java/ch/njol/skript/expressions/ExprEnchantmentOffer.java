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
import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
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

@Name("Enchantment Offer")
@Description("The enchantment offer in enchant prepare events.")
@Examples("enchantment offer 1")
@Since("INSERT VERSION")
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentOffer> {

	static {
		Skript.registerExpression(ExprEnchantmentOffer.class, EnchantmentOffer.class, ExpressionType.SIMPLE, 
				"enchant[ment] offer 1",
				"enchant[ment] offer 2",
				"enchant[ment] offer 3",
				"enchant[ment] offers");
	}

	private int offerNumber;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("Enchantment offers are only usable in enchant prepare events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		offerNumber = matchedPattern;
		return true;
	}

	@Override
	@Nullable
	protected EnchantmentOffer[] get(Event e) {
		if (offerNumber == 4)
			return ((PrepareItemEnchantEvent) e).getOffers();
		return new EnchantmentOffer[]{((PrepareItemEnchantEvent) e).getOffers()[offerNumber]};
	}

	@Override
	public Class<? extends EnchantmentOffer> getReturnType() {
		return EnchantmentOffer.class;
	}

	@Override
	public boolean isSingle() {
		return offerNumber != 4;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "enchantment offer " + offerNumber;
	}

}