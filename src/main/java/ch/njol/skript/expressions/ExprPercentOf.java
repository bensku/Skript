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

import java.util.Random;

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
import ch.njol.util.Math2;

@Name("Percentage Of")
@Description("Returns the specified percentage of a number.")
@Examples("5% of 100 #Result is 5")
@Since("INSERT VERSION")
public class ExprPercentOf extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprPercentOf.class, Number.class, ExpressionType.COMBINED, "%number%[ ]percent of %numbers%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> percent, numbers;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		percent = (Expression<Number>) exprs[0];
		numbers = (Expression<Number>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(final Event e) {
		Number percent = this.percent.getSingle(e);
		Number[] numbers = this.numbers.getArray(e);
		if (percent == null || numbers.length == 0)
			return null;
		double dPercent = percent.doubleValue()/100;
		Number[] result = new Number[numbers.length];
		for (int i = 0; i < numbers.length; i++)
			result[i] = dPercent * numbers[i].doubleValue();
		return result;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return percent.toString(e, debug) + " percent of " + numbers.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return numbers.isSingle();
	}
	
}
