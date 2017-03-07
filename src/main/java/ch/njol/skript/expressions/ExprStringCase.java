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
@Name("Upper/lower Case Text")
@Description("Copy of given text in upper or lower case.")
@Examples("\"oops!\" in upper case # OOPS!")
@Since("2.2-dev16")
public class ExprStringCase extends SimpleExpression<String> {

	private final static int UPPER = 0, LOWER = 1;
	
	/**
	 * Helper function which takes nullable string and
	 * uses given mode to it.
	 * @param str Original string.
	 * @param mode See above, UPPER or LOWER.
	 * @return Changed string.
	 */
	@SuppressWarnings("null")
	private static String changeCase(@Nullable String str, int mode) {
		if (str == null)
			return "";
		else if (mode == UPPER)
			return str.toUpperCase();
		else if (mode == LOWER)
			return str.toLowerCase();
		else
			return str;
	}
	
	static {
		Skript.registerExpression(ExprStringCase.class, String.class, ExpressionType.SIMPLE,
				"%string% in (0¦upper|1¦lower) case", "capitalized %string%");
	}
	
	@Nullable
	private Expression<String> origin;
	@Nullable
	private String literal;
	private int mode;
	
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0] instanceof Literal)
			literal = ((Literal<String>) exprs[0]).getSingle();
		else
			origin = (Expression<String>) exprs[0];
		if (matchedPattern == 1)
			mode = UPPER;
		else
			mode = parseResult.mark;
		
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		String str;
		if (literal != null)
			str = changeCase(literal, mode);
		else if (origin != null)
			str = changeCase(origin.getSingle(e), mode);
		else
			str = "";
		
		return new String[] {str};
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
		if (literal != null)
			return changeCase(literal, mode);
		else if (origin != null && e != null)
			return changeCase(origin.getSingle(e), mode);
		
		return "";
	}
	
}
