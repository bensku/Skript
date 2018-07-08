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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

@Name("Starts/Ends With")
@Description("Checks if a text starts or ends with another.")
@Examples({"if the argument starts with \"test\":",
		"	send \"Stop!\""})
@Since("2.2-dev36")
public class CondStartsWith extends Condition {

	static {
		Skript.registerCondition(CondStartsWith.class,
				"%strings% (start|1¦end)[s] with %string%",
				"%strings% do[es](n't| not) (start|1¦end) with %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> strings;
	@SuppressWarnings("null")
	private Expression<String> prefix;
	private boolean ends;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		prefix = (Expression<String>) exprs[1];
		ends = parseResult.mark == 1;
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		String p = prefix.getSingle(e);

		if (p == null)
			return false;

		return strings.check(e, new Checker<String>() {
			@Override
			public boolean check(String s) {
				return ends ? s.startsWith(p) : s.endsWith(p);
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + (isNegated() ? " don't " : " ") + (ends ? "start" : "end") +  " with " + prefix.toString(e, debug);
	}


}
