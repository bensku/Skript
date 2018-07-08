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

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@Name("Join & Split")
@Description("Joins several texts with a common delimiter (e.g. \", \"), or splits a text into multiple texts at a given delimiter.")
@Examples({"message \"Online players: %join all players with \" | \"%\" # %all players% would use the default \"x, y, and z\"",
		"set {_s::} to the string argument split at \",\""})
@Since("2.1")
public class ExprJoinSplit extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprJoinSplit.class, String.class, ExpressionType.COMBINED,
				"(concat[enate]|join) %objects% [(with|using|by) [[the] delimiter] %-string%]",
				"split %string% (at|using|by) [[the] delimiter] %string%", "%string% split (at|using|by) [[the] delimiter] %string%");
	}
	
	private boolean join;
	@SuppressWarnings("null")
	private Expression<String> things;
	@Nullable
	private Expression<String> delimiter;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		join = matchedPattern == 0;
		things = LiteralUtils.defendExpression(exprs[0]);
		delimiter = (Expression<String>) exprs[1];
		return LiteralUtils.canInitSafely(things);
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final Object[] t = things.getArray(e);
		final String d = delimiter != null ? delimiter.getSingle(e) : "";
		if (t.length == 0 || d == null)
			return new String[0];
		if (join) {
			return new String[] {
					StringUtils.join(
							Stream.of(t).map(Classes::toString).toArray(String[]::new),
							d
					)
			};
		} else {
			return ((String) t[0]).split(Pattern.quote(d), -1);
		}
	}
	
	@Override
	public boolean isSingle() {
		return join;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return join ? "join " + things.toString(e, debug) + (delimiter != null ? " with " + delimiter.toString(e, debug) : "") : "split " + things.toString(e, debug) + (delimiter != null ? " at " + delimiter.toString(e, debug) : "");
	}
	
}
