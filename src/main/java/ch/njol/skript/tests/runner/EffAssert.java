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
package ch.njol.skript.tests.runner;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;

@Name("Assert")
@Description("Assert that condition is true. Test fails when it is not.")
@Examples("")
@Since("2.5")
public class EffAssert extends Effect  {

	static {
		if (TestMode.ENABLED)
			Skript.registerEffect(EffAssert.class, "assert <.+> with %string%");
	}

	@SuppressWarnings("null")
	private Condition condition;
	
	@SuppressWarnings("null")
	private Expression<String> errorMsg;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String cond = parseResult.regexes.get(0).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		errorMsg = (Expression<String>) exprs[0];
		return condition != null;
	}

	@Override
	protected void execute(Event e) {}
	
	@Nullable
	@Override
	public TriggerItem walk(Event e) {
		if (!condition.check(e)) {
			String msg = errorMsg.getSingle(e);
			TestTracker.testFailed(msg != null ? msg : "assertation failed");
			return null;
		}
		return getNext();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "assert " + condition.toString(e, debug);
	}
}