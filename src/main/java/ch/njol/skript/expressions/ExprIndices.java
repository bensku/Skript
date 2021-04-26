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
package ch.njol.skript.expressions;

import java.util.Map;

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
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Comparators;
import ch.njol.util.Kleenean;

@Name("Indices of List")
@Description(
	"Returns all the indices of a list variable, optionally sorted by their values",
	"To sort the indices, all objects in the list must be comparable;",
	"if they're not, this expression will return the indices without sorting."
)
@Examples("set {l::*} to \"some\", \"cool\" and \"values\"\n" +
		"broadcast \"%all indexes of {l::*}%\" # result is 1, 2 and 3")
@Since("2.4 (indices), INSERT-VERSION (sorting)")
public class ExprIndices extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprIndices.class, String.class, ExpressionType.COMBINED,
				"[the] (indexes|indices) of %objects% [(1¦sorted by value[ in (2¦ascending|3¦descending) order])]",
				"(all of the|all the|all) (indices|indexes) of %objects% [(1¦sorted by value[ in (2¦ascending|3¦descending) order])]"
		);
	}
	
	@SuppressWarnings({"null", "NotNullFieldNotInitialized"})
	private Variable<?> list;
	
	private boolean shouldSort = false;
	private boolean descending = false;
	
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	protected String[] get(Event e) {
		Map<String, Object> valueMap = (Map<String, Object>) list.getRaw(e);
		if (valueMap == null) {
			return null;
		}
		
		if (shouldSort) {
			int direction = descending ? -1 : 1;
			
			return valueMap.entrySet().stream()
				.sorted((a, b) ->
					Comparators.compare(
						a.getValue(),
						b.getValue()
					).getRelation() * direction)
				.map(Map.Entry::getKey)
				.toArray(String[]::new);
			
		} else {
			return valueMap.keySet().toArray(new String[0]);
		}
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
        String text = "all indices of ";
		
		// we need to provide a null event otherwise the string value is what's held in the var
		text += list.toString(null, debug);
		
		if (shouldSort) {
            text += " sorted by value in ";
            text += (descending ? "descending" : "ascending");
            text += " order";
		}
		
		return text;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		shouldSort = parseResult.mark > 0;
		descending = parseResult.mark == 2; // Because parse marks are XORd, and 1 ⊕ 3 = 2
		
		if (exprs[0] instanceof Variable<?> && ((Variable<?>) exprs[0]).isList()) {
			list = (Variable<?>) exprs[0];
			return true;
		}
		
		// things like "all indices of fake expression" shouldn't have any output at all
		if (!(exprs[0] instanceof UnparsedLiteral)) {
			Skript.error("The indices expression may only be used with list variables");
		}
		
		return false;
	}
	
}
