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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Contains")
@Description("Checks whether an inventory contains an item, a text contains another piece of text, " +
		"or a list (e.g. {list variable::*} or 'drops') contains another object.")
@Examples({"block contains 20 cobblestone",
		"player has 4 flint and 2 iron ingots",
		"{list::*} contains 5"})
@Since("1.0")
public class CondContains extends Condition {

	static {
		Skript.registerCondition(CondContains.class,
				"%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings/objects% contain[s] %itemtypes/strings/objects%",
				"%inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%",
				"[the] list [of] %objects% (doesn't|does not|do not|don't) contain %objects%", // comes before 'contains' because of a parser bug
				"[the] list [of] %objects% contain[s] %objects%",
				"(all|1¦any|2¦none) of %strings% contain[s] %strings%");
	}

	@SuppressWarnings("null")
	private Expression<?> containers;
	@SuppressWarnings("null")
	private Expression<?> items;
	private boolean isListCheck, isStringCheck, isAnyNoneOf;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		containers = exprs[0];
		items = exprs[1];
		isAnyNoneOf = parseResult.mark == 1 || parseResult.mark == 2;
		isListCheck = matchedPattern == 4 || matchedPattern == 5;
		isStringCheck = matchedPattern == 6;

		if (!isListCheck && !(containers instanceof Variable) && containers.getReturnType() != Inventory.class &&
				containers.getReturnType() != String.class && containers.getReturnType() != Object.class) {
			Expression<?> converted = containers.getConvertedExpression(Inventory.class);
			if (converted == null)
				converted = containers.getConvertedExpression(String.class);
			if (converted != null)
				containers = converted;
		}

		setNegated(matchedPattern == 1 || matchedPattern == 3 || matchedPattern == 4 || parseResult.mark == 2);
		return true;
	}

	@Override
	public boolean check(Event e) {
		// List contains checks
		if (!isListCheck && !isStringCheck) {
			if (containers instanceof ExpressionList) {
				// It should be list check if the expression list contains
				// an expression that doesn't return an inventory or a string
				for (Expression<?> expr : ((ExpressionList<?>) containers).getExpressions()) {
					Class<?> returnType = expr.getReturnType();
					if (returnType != Inventory.class && returnType != String.class && returnType != Object.class) {
						isListCheck = true;
						break;
					}
				}
			} else if (containers.getReturnType() != Inventory.class && containers.getReturnType() != String.class &&
					!containers.isSingle()) {
				isListCheck = true;
			}
		}
		if (isListCheck) {
			Object[] containersAll = containers.getAll(e);
			if (containersAll.length == 0) // not actually needed, but avoids further loops
				return isNegated();
			return items.check(e, (Checker<Object>) item -> {
				for (Object container : containers.getAll(e)) {
					if (Relation.EQUAL.is(Comparators.compare(container, item)))
						return true;
				}
				return false;
			}, isNegated());
		}

		boolean caseSensitive = SkriptConfig.caseSensitive.value();

		// 'any/none of texts contains' checks
		if (isAnyNoneOf) {
			for (Object container : containers.getAll(e)) {
				String str = (String) container;
				assert str != null;
				if (items.check(e, (Checker<Object>) type ->
						type instanceof String && StringUtils.contains(str, (String) type, caseSensitive)))
					return !isNegated();
			}
			return isNegated();
		}

		// Inventory/string contains checks
		return containers.check(e, (Checker<Object>) container -> {
			if (container instanceof Inventory) {
				Inventory inv = (Inventory) container;
				return items.check(e, (Checker<Object>) type -> {
					if (type instanceof ItemType)
						return ((ItemType) type).isContainedIn(inv);
					if (type instanceof ItemStack)
						return inv.contains((ItemStack) type);
					return false;
				});
			} else if (container instanceof String) {
				String str = (String) container;
				return items.check(e, (Checker<Object>) type ->
					type instanceof String && StringUtils.contains(str, (String) type, caseSensitive));
			}
			return false;
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return containers.toString(e, debug) + (isNegated() ? " doesn't contain " : " contains ") + items.toString(e, debug);
	}

}
