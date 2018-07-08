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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Wearing")
@Description("Checks whether a player is wearing some armour.")
@Examples({"player is wearing an iron chestplate and iron leggings",
		"player is wearing all diamond armour"})
@Since("1.0")
public class CondIsWearing extends Condition {
	
	static {
		Skript.registerCondition(CondIsWearing.class, "%livingentities% (is|are) wearing %itemtypes%", "%livingentities% (isn't|is not|aren't|are not) wearing %itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<LivingEntity>) vars[0];
		types = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return entities.check(e, new Checker<LivingEntity>() {
			@Override
			public boolean check(final LivingEntity en) {
				return types.check(e, new Checker<ItemType>() {
					@Override
					public boolean check(final ItemType t) {
						for (final ItemStack is : en.getEquipment().getArmorContents()) {
							if (t.isOfType(is) ^ t.isAll())
								return !t.isAll();
						}
						return t.isAll();
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return entities.toString(e, debug) + (entities.isSingle() ? " is" : " are") + (isNegated() ? "not " : "") + " wearing " + types;
	}
	
}
