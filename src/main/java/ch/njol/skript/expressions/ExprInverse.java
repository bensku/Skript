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

import org.apache.commons.lang.WordUtils;
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

@Name("Boolean Inverse")
@Description("Invert a boolean expression")
@Examples({"!(false) # true",
	"!(ai of target entity)"})
@Since("INSERT VERSION")
public class ExprInverse extends SimpleExpression<Boolean> {
	
	static {
		Skript.registerExpression(ExprInverse.class, Boolean.class, ExpressionType.SIMPLE, "!%boolean%");
	}
	
	@SuppressWarnings("null")
	private Expression<Boolean> expr;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<Boolean>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Boolean[] get(Event e) {
		Boolean[] bools = expr.getArray(e);
		return new Boolean[]{ !(bools[0]) };
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
        return "inverse of " + expr.toString(e, debug);
	}
}
