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

import java.util.TreeMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Indices of List")
@Description("Gets indices of a list variable.")
@Examples("set {l::*} to \"some\", \"cool\" and \"values\"\n" +
		"broadcast \"%all indices of {l::*}%\" # Result is 1, 2 and 3")
@Since("2.2-dev37")
public class ExprIndexes extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIndexes.class, String.class, ExpressionType.PROPERTY,
				"[(all [[of] the]|the)] indices of %objects%",
				"%objects%'s indices"
		);
	}

	@SuppressWarnings("null")
	private Variable<?> list;

	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (exprs[0] instanceof Variable<?> && ((Variable<?>) exprs[0]).isList()) {
			list = (Variable<?>) exprs[0];
			return true;
		}

		// Things like "all indexes of fake expression" shouldn't have any output at all
		if (!(exprs[0] instanceof UnparsedLiteral)) {
			Skript.error("The indexes expression may only be used with list variables");
		}

		return false;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	protected String[] get(Event e) {
		TreeMap<String, Object> valueMap = (TreeMap<String, Object>) list.getRaw(e);
		if (valueMap == null) {
			return null;
		}
		return valueMap.keySet().toArray(new String[0]);
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
		// We need to provide a null event otherwise the string value is what's held in the var
		return "all indexes of " + list.toString(null, debug);
	}

}
