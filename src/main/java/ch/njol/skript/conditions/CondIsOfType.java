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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is of Type")
@Description("Checks whether an item of entity is of the given type. This is mostly useful for variables, as you can use the general 'is' condition otherwise (e.g. 'victim is a creeper').")
@Examples({"tool is of type {*selected type}",
		"victim is of type {villager type}"})
@Since("1.4")
public class CondIsOfType extends Condition {
	static {
		Skript.registerCondition(CondIsOfType.class,
				"%itemstacks/entities% (is|are) of type[s] %itemtypes/entitydatas%", "%itemstacks/entities% (isn't|is not|aren't|are not) of type[s] %itemtypes/entitydatas%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> what;
	@SuppressWarnings("null")
	Expression<?> types;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		what = exprs[0];
		types = exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return what.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o1) {
				return types.check(e, new Checker<Object>() {
					@Override
					public boolean check(final Object o2) {
						if (o2 instanceof ItemType && o1 instanceof ItemType) {
							return ((ItemType) o2).isSupertypeOf((ItemType) o1);
						} else if (o2 instanceof EntityData && o1 instanceof Entity) {
							return ((EntityData<?>) o2).isInstance((Entity) o1);
						} else if (o2 instanceof ItemType && o1 instanceof Entity) {
							return Relation.EQUAL.is(DefaultComparators.entityItemComparator.compare(EntityData.fromEntity((Entity) o1), (ItemType) o2));
						} else {
							return false;
						}
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return what.toString(e, debug) + (what.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + "of type " + types.toString(e, debug);
	}
	
}
