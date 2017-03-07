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

import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Enchanted")
@Description("Checks whether an item is enchanted.")
@Examples({"tool of the player is enchanted with efficiency 2",
		"helm, chestplate, leggings or boots are enchanted"})
@Since("1.4.6")
public class CondIsEnchanted extends Condition {
	
	static {
		Skript.registerCondition(CondIsEnchanted.class,
				"%itemtypes% (is|are) enchanted [with %-enchantmenttype%]",
				"%itemtypes% (isn't|is not|aren't|are not) enchanted [with %-enchantmenttype%]");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@Nullable
	Expression<EnchantmentType> enchs;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		enchs = (Expression<EnchantmentType>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return items.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType item) {
				final Expression<EnchantmentType> es = enchs;
				if (es == null) {
					final Map<Enchantment, Integer> enchs = item.getEnchantments();
					return isNegated() ^ (enchs != null && !enchs.isEmpty());
				}
				return es.check(e, new Checker<EnchantmentType>() {
					@Override
					public boolean check(final EnchantmentType ench) {
						return ench.has(item);
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<EnchantmentType> es = enchs;
		return items.toString(e, debug) + " " + (items.isSingle() ? "is" : "are") + (isNegated() ? " not" : "") + " enchanted" + (es == null ? "" : " with " + es.toString(e, debug));
	}
	
}
