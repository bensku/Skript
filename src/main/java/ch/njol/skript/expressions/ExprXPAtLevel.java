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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import java.util.Objects;

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

@Name("Exprerience At Level")
@Description("Returns the total experience at a specified minecraft level.")
@Examples({"command /xp:",
	"\ttrigger:",
	"\t\tset {_level} to total exp at level player's level",
	"\t\tsend \"The total XP at your level is %{_level}%!"})
@Since("INSERT VERSION")
public class ExprXPAtLevel extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprXPAtLevel.class, Number.class, ExpressionType.SIMPLE, "[total ]([e]xp|experience) at level %number%");
	}
	
	@Nullable
	private Expression<Number> number;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		number = (Expression<Number>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event event) {
		assert number != null;
		Number num = number.getSingle(event);
		assert num != null;
		int level = num.intValue();
		Number exp = 0;
		if (level >= 1 && level <= 15)
			exp = level * level + 6 * level;
		else if (level >= 16 && level <= 30)
			exp = 2.5 * level * level - 40.5 * level + 360;
		else
			exp = 4.5 * level * level - 162.5 * level + 2220;
		return new Number[]{exp};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "XP at level";
	}
}
