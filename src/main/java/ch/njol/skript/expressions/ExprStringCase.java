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

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author bensku
 */
@Name("Lower/Upper Case Text")
@Description("Copy of given text in lower or upper case.")
@Examples("\"oops!\" in upper case # OOPS!")
@Since("2.2-dev16")
public class ExprStringCase extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprStringCase.class, String.class, ExpressionType.SIMPLE,
				"%strings% in (0¦upper|1¦lower)[ ]case", 
				"(0¦upper|1¦lower)[ ]case %strings%",
				"capitali(s|z)ed %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> expr;
	
	private boolean uppercase = true;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<String>) exprs[0];
		uppercase = (matchedPattern == 2) ? true : ((parseResult.mark == 0) ? true : false);
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		String[] strs = expr.getAll(e);
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] != null)
				strs[i] = uppercase ? strs[i].toUpperCase(Locale.ENGLISH) : strs[i].toLowerCase(Locale.ENGLISH);
		}
		return strs;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return uppercase ? "uppercase" : "lowercase";
	}
	
}
