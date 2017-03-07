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
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@Name("Contains")
@Description("Checks whether an inventory contains the given item, a text contains another piece of text, or a list of objects (e.g. a {list variable::*}) contains another object.")
@Examples({"block contains 20 cobblestone",
		"player has 4 flint and 2 iron ingots"})
@Since("1.0")
public class CondContains extends Condition {
	static {
		Skript.registerCondition(CondContains.class,
				"%inventories% ha(s|ve) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings/objects% contain[s] %itemtypes/strings/objects%",
				"%inventories% do[es](n't| not) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings/objects% do[es](n't| not) contain %itemtypes/strings/objects%");
	}
	
	@SuppressWarnings("null")
	Expression<?> containers;
	@SuppressWarnings("null")
	Expression<?> items;
	
	@SuppressWarnings({"unchecked", "null", "unused"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		containers = exprs[0].getConvertedExpression(Object.class);
		if (containers == null)
			return false;
		if (!(containers instanceof Variable) && !String.class.isAssignableFrom(containers.getReturnType()) && !Inventory.class.isAssignableFrom(containers.getReturnType())) {
			final ParseLogHandler h = SkriptLogger.startParseLogHandler();
			try {
				Expression<?> c = containers.getConvertedExpression(String.class);
				if (c == null)
					c = containers.getConvertedExpression(Inventory.class);
				if (c == null) {
					h.printError();
					return false;
				}
				containers = c;
				h.printLog();
			} finally {
				h.stop();
			}
		}
		items = exprs[1].getConvertedExpression(Object.class);
		if (items == null)
			return false;
		setNegated(matchedPattern >= 2);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return containers.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object container) {
				if (containers instanceof Variable && !containers.isSingle()) {
					return items.check(e, new Checker<Object>() {
						@Override
						public boolean check(final Object item) {
							return Relation.EQUAL.is(Comparators.compare(container, item));
						}
					}, isNegated());
				} else {
					if (container instanceof Inventory) {
						final Inventory invi = (Inventory) container;
						return items.check(e, new Checker<Object>() {
							@Override
							public boolean check(final Object type) {
								return type instanceof ItemType && ((ItemType) type).isContainedIn(invi);
							}
						}, isNegated());
					} else if (container instanceof String) {
						final String s = (String) container;
						return items.check(e, new Checker<Object>() {
							@Override
							public boolean check(final Object type) {
								return type instanceof String && StringUtils.contains(s, (String) type, SkriptConfig.caseSensitive.value());
							}
						}, isNegated());
					}
					return false;
				}
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return containers.toString(e, debug) + (isNegated() ? " doesn't contain " : " contains ") + items.toString(e, debug);
	}
	
}
