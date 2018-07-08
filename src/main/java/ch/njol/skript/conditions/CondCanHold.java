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
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Can Hold")
@Description("Tests whether a player or a chest can hold the given item.")
@Examples({"block can hold 200 cobblestone",
		"player has enough space for 64 feathers"})
@Since("1.0")
public class CondCanHold extends Condition {
	static {
		Skript.registerCondition(CondCanHold.class,
				"%inventories% (can hold|ha(s|ve) [enough] space (for|to hold)) %itemtypes%",
				"%inventories% (can(no|')t hold|(ha(s|ve) not|ha(s|ve)n't|do[es]n't have) [enough] space (for|to hold)) %itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<Inventory> invis;
	@SuppressWarnings("null")
	Expression<ItemType> items;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		invis = (Expression<Inventory>) vars[0];
		items = (Expression<ItemType>) vars[1];
		if (items instanceof Literal) {
			for (ItemType t : ((Literal<ItemType>) items).getAll()) {
				t = t.getItem();
				if (!(t.isAll() || (t.getTypes().size() == 1 && !t.getTypes().get(0).hasDataRange() && t.getTypes().get(0).getId() != -1))) {
					Skript.error("The condition 'can hold' can currently only be used with aliases that start with 'every' or 'all', or only represent one item and one data value.", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
			}
		}
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return invis.check(e, new Checker<Inventory>() {
			@Override
			public boolean check(final Inventory invi) {
				if (!items.getAnd()) {
					return items.check(e, new Checker<ItemType>() {
						@Override
						public boolean check(final ItemType t) {
							return t.getItem().hasSpace(invi);
						}
					}, isNegated());
				}
				final ItemStack[] buf = ItemType.getStorageContents(invi);
				return items.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						return t.getItem().addTo(buf);
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return invis.toString(e, debug) + " can" + (isNegated() ? "'t" : "") + " hold " + items.toString(e, debug);
	}
	
}
