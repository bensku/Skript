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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Repeated")
@Description("A string repeated a number times.")
@Examples({"on chat:",
	"\tset {_t} to message repeated 3 times",
	"\tsend {_t}"})
@Since("INSERT VERSION")
public class ExprRepeated extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprRepeated.class, String.class, ExpressionType.COMBINED, "%string% repeat[ed] %number% time[s]", "repeat[ed] %string% %number% time[s]");
	}
	
	@SuppressWarnings("null")
	private Expression<String> text;
	
	@SuppressWarnings("null")
	private Expression<Number> times;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		text = (Expression<String>) exprs[0];
		times = (Expression<Number>) exprs[1];
		return true;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event e) {
		String string = text.getSingle(e);
		Number num = times.getSingle(e);
		if (string == null || num == null) return null;
		int number = num.intValue();
		if (number < 0) return null;
		else if (number == 0) return new String[]{""};
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < number; i++) {
			builder.append(string);
		}
		return new String[]{builder.toString()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return text.toString(e, debug) + " repeated " + times.toString(e, debug) + " times";
	}
	
}
