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

import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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

@Name("Enchantment Exp Level Cost")
@Description({"The cost of an enchantment in an enchant prepare event. This expression is only for Minecraft 1.9 and 1.10.",
			"If the cost is changed, it will always be at least 1."})
@Examples("set cost of enchantment 1 to 50")
@Since("INSERT VERSION")
@SuppressWarnings("deprecation")
public class ExprEnchantmentExpLevelCosts extends SimpleExpression<Integer>{

	static {
		if (!Skript.isRunningMinecraft(1, 11)) { // This should only be usable on 1.9 and 1.10.
			Skript.registerExpression(ExprEnchantmentExpLevelCosts.class, Integer.class, ExpressionType.PROPERTY,
					"cost of enchant[ment] [offer] 1",
					"cost of enchant[ment] [offer] 2",
					"cost of enchant[ment] [offer] 3",
					"cost of (enchant[ment]s|enchant[ment] offers)");
		}
	}

	private int offerNumber;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("The enchantment exp level cost is only usable in an enchant prepare event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		offerNumber = matchedPattern;
		return true;
	}

	@Override
	@Nullable
	protected Integer[] get(Event e) {
		if (offerNumber == 4) {
			return Arrays.stream(((PrepareItemEnchantEvent) e).getExpLevelCostsOffered())
					.boxed()
					.toArray(Integer[]::new);
		}
		return new Integer[]{((PrepareItemEnchantEvent) e).getExpLevelCostsOffered()[offerNumber]};
	}

	@Override
	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null)
			return;
		int cost = delta != null ? ((Integer) delta[0]).intValue() : 1;
		if (cost < 1) cost = 1;
		PrepareItemEnchantEvent e = (PrepareItemEnchantEvent) event;
		switch (mode) {
			case SET:
				if (offerNumber == 4) {
					for (int i = 0; i <= 2; i++)
						e.getExpLevelCostsOffered()[i] = cost;
				} else {
					e.getExpLevelCostsOffered()[offerNumber] = cost;
				}
				break;
			case ADD:
				int add;
				if (offerNumber == 4) {
					for (int i = 0; i <= 2; i++) {
						add = cost + e.getExpLevelCostsOffered()[i];
						if (add < 1) add = 1;
						e.getExpLevelCostsOffered()[i] = add;
					}
				} else {
					add = cost + e.getExpLevelCostsOffered()[offerNumber];
					if (add < 1) add = 1;
					e.getExpLevelCostsOffered()[offerNumber] = add;
				}
				break;
			case REMOVE:
				int subtract;
				if (offerNumber == 4) {
					for (int i = 0; i <= 2; i++) {
						subtract = cost - e.getExpLevelCostsOffered()[i];
						if (subtract < 1) subtract = 1;
						e.getExpLevelCostsOffered()[i] = subtract;
					}
				} else {
					subtract = cost - e.getExpLevelCostsOffered()[offerNumber];
					if (subtract < 1) subtract = 1;
					e.getExpLevelCostsOffered()[offerNumber] = subtract;
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
		return offerNumber != 4;
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (offerNumber == 4)
			return "cost of enchantment offers";
		return "cost of enchantment offer " + (offerNumber+1);
	}

}
