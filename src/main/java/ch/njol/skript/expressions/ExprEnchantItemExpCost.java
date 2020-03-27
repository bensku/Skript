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
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
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

@Name("Enchant Event Experience Cost")
@Description({"The cost to enchant in an enchant event.", 
				"This is the number displayed in the enchantment table, not the actual number of levels removed."})
@Examples({"on enchant:",
			"\tsend \"Cost: %cost of enchanting%\" to player"})
@Events("enchant prepare")
@Since("INSERT VERSION")
public class ExprEnchantItemExpCost extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprEnchantItemExpCost.class, Number.class, ExpressionType.SIMPLE, "[the] [exp[erience]] cost of enchant[(ing|ment)]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EnchantItemEvent.class)) {
			Skript.error("The experience cost of enchanting is only usable in an enchant event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		return new Number[]{((EnchantItemEvent) e).getExpLevelCost()};
	}

	/*
	 * As of now, modifying the cost does not seem to serve a purpose.
	 * However, in the case that this eventually gains functionality,
	 * it can be added easily.
	 * 
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int cost = delta != null ? ((Number) delta[0]).intValue() : 1;
		if (cost < 1) cost = 1;
		EnchantItemEvent e = (EnchantItemEvent) event;
		switch (mode) {
			case SET:
				e.setExpLevelCost(cost);
				break;
			case ADD:
				int add = cost + e.getExpLevelCost();
				if (add < 1) add = 1;
				e.setExpLevelCost(add);
				break;
			case REMOVE:
				int subtract = cost + e.getExpLevelCost();
				if (subtract < 1) subtract = 1;
				e.setExpLevelCost(subtract);
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}
	*/

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "experience cost of enchantment";
	}

}
