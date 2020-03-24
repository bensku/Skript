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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;

/**
 * @author Peter G�ttinger
 */
@Name("Numbers")
@Description({"All numbers between two given numbers, useful for looping.",
		"Use 'numbers' if your start is not an integer and you want to keep the fractional part of the start number constant, or use 'integers' if you only want to loop integers.",
		"When using 'numbers', the precision of the first number will be taken into account.",
		"You may want to use the 'times' expression instead, for instance 'loop 5 times:'"})
@Examples({"loop numbers from 2.5 to 5.5: # loops 2.5, 2.6, 2.7, ..., 5.4 and 5.5",
		"loop numbers from 2.21 to 2.3: # loops 2.21, 2.22, ..., 2.29 and 2.3, because we use the precision of the first value!",
		"loop integers from 2.9 to 5.1: # same as '3 to 5', i.e. loops 3, 4, 5"})
@Since("1.4.6")
public class ExprNumbers extends SimpleExpression<Number> {
	static {
		Skript.registerExpression(ExprNumbers.class, Number.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] (numbers|1�integers) (between|from) %number% (and|to) %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> start, end;
	boolean integer;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		start = (Expression<Number>) exprs[0];
		end = (Expression<Number>) exprs[1];
		integer = parseResult.mark == 1;
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(final Event event) {
		Number s = start.getSingle(event), f = end.getSingle(event);
		if (s == null || f == null)
			return null;
		final boolean reverse = s.doubleValue() > f.doubleValue();
		if (reverse) {
			Number temp = s;
			s = f;
			f = temp;
		}
		
		final BigDecimal decimals = BigDecimal.valueOf(s.doubleValue());
		final double increment = integer ? 1 : Math.pow(10, -1 * decimals.scale()); 
		double startValue = integer ? Math.ceil(s.doubleValue()) : s.doubleValue();
		double endValue = integer ? Math.floor(f.doubleValue()) : f.doubleValue();
		
		for (double i = startValue; i < endValue; i += increment) {
		    list.add(integer ? (long) i : i);
		}
	
		if (reverse) Collections.reverse(list);
		return list.toArray(new Number[0]);
	}
	
	@Override
	@Nullable
	public Iterator<Number> iterator(final Event event) {
		Number s = start.getSingle(event), f = end.getSingle(event);
		if (s == null || f == null)
			return null;
		final boolean reverse = s.doubleValue() > f.doubleValue();
		if (reverse) {
			Number temp = s;
			s = f;
			f = temp;
		}
		final Number starting = s, finish = f;
		return new Iterator<Number>() {
			double i = integer ? Math.ceil(starting.doubleValue()) : starting.doubleValue(), max = integer ? Math.floor(finish.doubleValue()) : finish.doubleValue();
			
			@Override
			public boolean hasNext() {
				return i <= max;
			}
			
			@SuppressWarnings("null")
			@Override
			public Number next() {
				if (!hasNext())
					throw new NoSuchElementException();
				if (integer)
					return (long) (reverse ? max-- : i++);
				else
					return reverse ? max-- : i++;
			}
		};
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (integer ? "integers" : "numbers") + " from " + start.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return integer && (s.equalsIgnoreCase("integer") || s.equalsIgnoreCase("int"));
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return integer ? Long.class : Double.class;
	}
}
