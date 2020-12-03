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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Is Identical")
@Description("Checks if two strings are identical. This is case sensitive.")
@Examples({"command /compare <text> <text>:",
	"\ttrigger:",
	"\t\tif arg-1 is identical to arg-2:",
	"\t\t\tsend \"Identical!\""})
@Since("INSERT VERSION")
public class CondIsIdentical extends Condition {
	
	static {
		PropertyCondition.register(CondIsIdentical.class, "identical to %strings%", "strings");
	}
	
	@SuppressWarnings("null")
	private Expression<String> stringOne;
	@SuppressWarnings("null")
	private Expression<String> stringTwo;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		stringOne = (Expression<String>) exprs[0];
		stringTwo = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean check(Event e) {
		return (stringOne.getSingle(e).equals(stringTwo.getSingle(e)));
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "identical to";
	}
}
