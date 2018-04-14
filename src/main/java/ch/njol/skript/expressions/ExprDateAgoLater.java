/*
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
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Date Ago/Later")
@Description("A date the specified timespan before/after another date.")
@Examples({"set {_yesterday} to 1 day ago"})
@Since("2.2-dev33")
public class ExprDateAgoLater extends SimpleExpression<Date> {

	static {
		Skript.registerExpression(ExprDateAgoLater.class, Date.class, ExpressionType.COMBINED,
				"%timespan% (ago|in the past|before [the] [date] %-date%)",
				"%timespan% (later|(from|after) [the] [date] %-date%)");
	}

	@SuppressWarnings("null")
	private Expression<Timespan> timespan;
	@SuppressWarnings("null")
	private Expression<Date> date;
	@SuppressWarnings("null")
	private boolean ago;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		timespan = (Expression<Timespan>) exprs[0];
		date = (Expression<Date>) exprs[1];
		ago = matchedPattern == 0;
		return true;
	}

	@Nullable
	@Override
	protected Date[] get(final Event e) {
		Timespan timespan = this.timespan.getSingle(e);
		Date date = this.date == null ? new Date() : this.date.getSingle(e);
		if (timespan == null || date == null) {
			return null;
		}
		if (ago) {
			date.subtract(timespan);
		} else {
			date.add(timespan);
		}
		return new Date[]{date};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return timespan.toString(e, debug) + " " + (ago ? "ago" : "later");
	}
}
