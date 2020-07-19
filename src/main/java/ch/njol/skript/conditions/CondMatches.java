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

import java.util.Arrays;
import java.util.regex.Pattern;

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
import ch.njol.util.Kleenean;

@Name("Matches")
@Description("Test whether strings match defined regular expressions")
@Examples({"on chat:",
	"\tif message matches \"\\d\":",
	"\t\tsend \"Message contains a digit!\""})
@Since("INSERT VERSION")
public class CondMatches extends Condition {
	
	static {
		Skript.registerCondition(CondMatches.class, "%strings% match[es] %strings%");
	}
	
	@SuppressWarnings("null")
	Expression<String> text1;
	@SuppressWarnings("null")
	Expression<String> text2;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		text1 = (Expression<String>) exprs[0];
		text2 = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String[] txt1 = text1.getAll(e);
		String[] txt2 = text2.getAll(e);
		if (txt1 == null || text2 == null) return false;
		Object[] patterns = Arrays.stream(txt2)
			.map((str) -> Pattern.compile(str))
			.toArray();
		return Arrays.stream(txt1)
			.allMatch((str) -> Arrays.stream(patterns)
				.allMatch((pattern) -> ((Pattern) pattern).matcher(str).find()));
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return text1.toString(e, debug) + " matches " + text2.toString(e, debug);
	}
	
}
