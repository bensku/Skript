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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Reversed List")
@Description("Reverses given list.")
@Examples({"set {_list::*} to reversed {_list::*}"})
@Since("2.4")
public class ExprReversedList extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprReversedList.class, Object.class, ExpressionType.COMBINED, "reversed %objects%");
	}
	
	@SuppressWarnings("null")
	private Expression<Object> list;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		list = (Expression<Object>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected Object[] get(Event e) {
		List<Object> reversed = Arrays.asList(list.getAll(e).clone());
		Collections.reverse(reversed);
		return reversed.toArray();
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "reversed list";
	}
}
