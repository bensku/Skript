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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

@Name("Has Leash Holder")
@Description("Checks whether entities have leash holders.")
@Examples("target entity doesn't have a leash holder")
@Since("INSERT VERSION")
public class CondLeashHolder extends Condition {
	
	static {
		Skript.registerCondition(CondLeashHolder.class,
				"%livingentities% (is|are) leashed [(from|by) %-entity%]",
				"%livingentities% [do[es]] ha(s|ve) [a] leash [holder] [(from|by) %-entity%]",
				"%livingentities% (is|are)(n't| not) leashed [(from|by) %-entity%]",
				"%livingentities% do[es](n't| not) have [a] leash [holder] [(from|by) %-entity%]");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	Expression<Entity> holder;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		holder = (Expression<Entity>) exprs[1];
		setNegated(matchedPattern <= 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return entities.check(e, new Checker<LivingEntity>() {
			@Override
			public boolean check(final LivingEntity target) {
				if (holder.getSingle(e) == null)
					return target.isLeashed() ? isNegated() : !isNegated();
				return holder.check(e, new Checker<Entity>() {
					@Override
					public boolean check(final Entity holder) {
						return target.getLeashHolder().equals(holder);
					}
				}, isNegated());
			}
		});
	}
	
	@SuppressWarnings("null")
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return entities.toString(e, debug) + " " + (entities.isSingle() ? "is" : "are") + (isNegated() ? " not" : "") + " leashed" + (holder == null ? "" : " by " + holder.toString(e, debug));
	}

}
