/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.PersistentDataUtils;
import ch.njol.util.Kleenean;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Persistent Data")
@Description("Checks whether a persistent data holder has the specified value.")
@Examples("if player has persistent data \"epic\":")
@Since("INSERT VERSION")
public class CondHasPersistentData extends Condition {

	static {
		Skript.registerCondition(CondHasPersistentData.class,
				"%persistentdataholders% (has|have) persistent data [(value|tag)[s]] %strings%",
				"%persistentdataholders% (doesn't|does not|do not|don't) have persistent data [(value|tag)[s]] %strings%"
		);
	}

	@SuppressWarnings("null")
	private Expression<PersistentDataHolder> holders;
	@SuppressWarnings("null")
	private Expression<String> values;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		holders = (Expression<PersistentDataHolder>) exprs[0];
		values = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return holders.check(e,
				holder -> values.check(e,
						value -> (PersistentDataUtils.get(holder, value) != null)
				), isNegated());

	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, holders,
				"persistent data " + (values.isSingle() ? "value " : "values ") + values.toString(e, debug));
	}

}
