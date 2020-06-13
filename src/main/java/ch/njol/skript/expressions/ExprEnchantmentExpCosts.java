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

import java.util.Arrays;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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

@Name("Enchantment Experience Level Costs")
@Description({"The experience cost of an enchantment in an enchant prepare event.",
			"If the cost is changed, it will always be at least 1."})
@Examples({"on enchant prepare:",
		"\tset the cost of enchantment 1 to 50"})
@Since("INSERT VERSION")
@Events("enchant prepare")
@RequiredPlugins("1.9 or 1.10")
@SuppressWarnings("deprecation")
public class ExprEnchantmentExpCosts extends SimpleExpression<Number>{

	static {
		if (!Skript.isRunningMinecraft(1, 11)) { // This expression should only be usable on 1.9 and 1.10.
			Skript.registerExpression(ExprEnchantmentExpCosts.class, Number.class, ExpressionType.SIMPLE,
					"[the] cost of (enchant[ment]s|enchant[ment] offers)",
					"[the] cost of enchant[ment] [offer] %number%",
					"enchant[ment] [offer] %number%'[s] cost",
					"[the] cost of [the] %number%(st|nd|rd|th) enchant[ment] [offer]");
		}
	}

	@SuppressWarnings("null")
	private Expression<Number> exprOfferNumber;

	private boolean multiple;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("The enchantment exp level cost is only usable in an enchant prepare event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (matchedPattern == 0) {
			multiple = true;
		} else {
			exprOfferNumber = (Expression<Number>) exprs[0];
			multiple = false;
		}
		return true;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Number[] get(Event e) {
		if (multiple) {
			return Arrays.stream(((PrepareItemEnchantEvent) e).getExpLevelCostsOffered())
					.boxed()
					.toArray(Number[]::new);
		}

		int offerNumber = exprOfferNumber.getSingle(e).intValue();
		if (offerNumber < 1 || offerNumber > 3)
			return new Number[]{};

		return new Number[]{((PrepareItemEnchantEvent) e).getExpLevelCostsOffered()[offerNumber - 1]};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null)
			return;
		int cost = ((Number) delta[0]).intValue();
		if (cost < 1) 
			return;

		// Index Form
		int offerNumber = 0;
		if (exprOfferNumber != null) {
			offerNumber = exprOfferNumber.getSingle(event).intValue() - 1;
		}

		int change;
		PrepareItemEnchantEvent e = (PrepareItemEnchantEvent) event;
		switch (mode) {
			case SET:
				if (multiple) {
					for (int i = 0; i <= 2; i++)
						e.getExpLevelCostsOffered()[i] = cost;
				} else {
					e.getExpLevelCostsOffered()[offerNumber] = cost;
				}
				break;
			case ADD:
				if (multiple) {
					for (int i = 0; i <= 2; i++) {
						change = cost + e.getExpLevelCostsOffered()[i];
						if (change < 1) 
							continue;
						e.getExpLevelCostsOffered()[i] = change;
					}
				} else {
					change = cost + e.getExpLevelCostsOffered()[offerNumber];
					if (change < 1) 
						return;
					e.getExpLevelCostsOffered()[offerNumber] = change;
				}
				break;
			case REMOVE:
				if (multiple) {
					for (int i = 0; i <= 2; i++) {
						change = cost - e.getExpLevelCostsOffered()[i];
						if (change < 1) 
							continue;
						e.getExpLevelCostsOffered()[i] = change;
					}
				} else {
					change = cost - e.getExpLevelCostsOffered()[offerNumber];
					if (change < 1) 
						return;
					e.getExpLevelCostsOffered()[offerNumber] = change;
				}
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return !multiple;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (multiple)
			return "cost of enchantment offers";
		return "cost of enchantment offer " + exprOfferNumber.toString(e, debug);
	}

}
