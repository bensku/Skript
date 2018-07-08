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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Metadata")
@Description("Checks whether a metadata holder has a metadata tag.")
@Examples("if player has metadata value \"healer\":")
@Since("2.2-dev36")
@SuppressWarnings("null")
public class CondHasMetadata extends Condition {

	@Nullable
	private Expression<Metadatable> holders;
	@Nullable
	private Expression<String> values;

	static {
		Skript.registerCondition(CondHasMetadata.class,
				"%metadataholders% (has|have) metadata [(value|tag)[s]] %strings%",
				"%metadataholders% (doesn't|does not|do not|don't) have metadata [(value|tag)[s]] %strings%"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		holders = (Expression<Metadatable>) exprs[0];
		values = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		String[] values = this.values.getArray(e);
		if (values == null || values.length == 0)
			return false;
		return holders.check(e, h -> {
			for (String value : values) {
				if (!h.hasMetadata(value))
					return false;
			}
			return true;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (!isNegated())
			return holders.toString(e, debug) + " has metadata value " + values.toString(e, debug);
		return holders.toString(e, debug) + " does not have metadata value " + values.toString(e, debug);
	}
}