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

import java.lang.reflect.Array;
import java.util.ArrayList;
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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Examples({"set the player's health to 10 - the player's health",
		"loop (argument + 2) / 5 times:",
		"\tmessage \"Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%\"",
		"message \"You have %health of player * 2% half hearts of HP!\""})
@Since("1.4.2")
@SuppressWarnings("null")
public class ExprArithmetic extends SimpleExpression<Number> {
	
	@SuppressWarnings("UnnecessaryBoxing")
	private enum Operator {
		PLUS('+') {
			@SuppressWarnings("null")
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Long.valueOf(n1.longValue() + n2.longValue());
				return Double.valueOf(n1.doubleValue() + n2.doubleValue());
			}
		},
		MINUS('-') {
			@SuppressWarnings("null")
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Long.valueOf(n1.longValue() - n2.longValue());
				return Double.valueOf(n1.doubleValue() - n2.doubleValue());
			}
		},
		MULT('*') {
			@SuppressWarnings("null")
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Long.valueOf(n1.longValue() * n2.longValue());
				return Double.valueOf(n1.doubleValue() * n2.doubleValue());
			}
		},
		DIV('/') {
			@SuppressWarnings("null")
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer) {
					final long div = n2.longValue();
					if (div == 0)
						return Long.MAX_VALUE;
					return Long.valueOf(n1.longValue() / div);
				}
				return Double.valueOf(n1.doubleValue() / n2.doubleValue());
			}
		},
		EXP('^') {
			@SuppressWarnings("null")
			@Override
			public Number calculate(final Number n1, final Number n2, final boolean integer) {
				if (integer)
					return Long.valueOf((long) Math.pow(n1.longValue(), n2.longValue()));
				return Double.valueOf(Math.pow(n1.doubleValue(), n2.doubleValue()));
			}
		};
		
		public final char sign;
		
		Operator(final char sign) {
			this.sign = sign;
		}
		
		public abstract Number calculate(Number n1, Number n2, boolean integer);
		
		@Override
		public String toString() {
			return "" + sign;
		}
		
	}
	
	private static class PatternInfo {
		public final Operator operator;
		public final boolean leftGrouped;
		public final boolean rightGrouped;
		
		public PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
			this.operator = operator;
			this.leftGrouped = leftGrouped;
			this.rightGrouped = rightGrouped;
		}
	}
	
	public interface Gettable {
		Number get(Event event, boolean integer);
	}
	
	public static class ExpressionInfo implements Gettable {
		private final Expression<? extends Number> expression;
		
		public ExpressionInfo(Expression<? extends Number> expression) {
			this.expression = expression;
		}
		
		@Override
		public Number get(Event event, boolean integer) {
			Number number = expression.getSingle(event);
			return number != null ? number : 0;
		}
	}
	
	public static class ArithmeticChain implements Gettable {
		private final Gettable left;
		private final Operator operator;
		private final Gettable right;
		
		public ArithmeticChain(Gettable left, Operator operator, Gettable right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		
		@SuppressWarnings("unchecked")
		public static Gettable parse(List<Object> chain) {
			Checker<Object>[] checkers = new Checker[] {
				o -> o.equals(Operator.PLUS) || o.equals(Operator.MINUS),
				o -> o.equals(Operator.MULT) || o.equals(Operator.DIV),
				o -> o.equals(Operator.EXP)
			};
			
			for (Checker<Object> checker : checkers) {
				int lastIndex = findLastIndex(chain, checker);
				
				if (lastIndex != -1) {
					List<Object> leftChain = chain.subList(0, lastIndex);
					Gettable left = parse(leftChain);
					
					Operator operator = (Operator) chain.get(lastIndex);
					
					List<Object> rightChain = chain.subList(lastIndex + 1, chain.size());
					Gettable right = parse(rightChain);
					
					return new ArithmeticChain(left, operator, right);
				}
			}
			
			if (chain.size() != 1)
				throw new IllegalStateException();
			
			return new ExpressionInfo((Expression<? extends Number>) chain.get(0));
		}
		
		private static <T> int findLastIndex(List<T> list, Checker<T> checker) {
			int lastIndex = -1;
			for (int i = 0; i < list.size(); i++) {
				if (checker.check(list.get(i)))
					lastIndex = i;
			}
			return lastIndex;
		}
		
		@Override
		public Number get(Event event, boolean integer) {
			return operator.calculate(left.get(event, integer), right.get(event, integer), integer);
		}
	}
	
	private final static Patterns<PatternInfo> patterns = new Patterns<>(new Object[][] {

		{"\\(%number%\\)[ ]+[ ]\\(%number%\\)", new PatternInfo(Operator.PLUS, true, true)},
		{"\\(%number%\\)[ ]+[ ]%number%", new PatternInfo(Operator.PLUS, true, false)},
		{"%number%[ ]+[ ]\\(%number%\\)", new PatternInfo(Operator.PLUS, false, true)},
		{"%number%[ ]+[ ]%number%", new PatternInfo(Operator.PLUS, false, false)},
		
		{"\\(%number%\\)[ ]-[ ]\\(%number%\\)", new PatternInfo(Operator.MINUS, true, true)},
		{"\\(%number%\\)[ ]-[ ]%number%", new PatternInfo(Operator.MINUS, true, false)},
		{"%number%[ ]-[ ]\\(%number%\\)", new PatternInfo(Operator.MINUS, false, true)},
		{"%number%[ ]-[ ]%number%", new PatternInfo(Operator.MINUS, false, false)},
		
		{"\\(%number%\\)[ ]*[ ]\\(%number%\\)", new PatternInfo(Operator.MULT, true, true)},
		{"\\(%number%\\)[ ]*[ ]%number%", new PatternInfo(Operator.MULT, true, false)},
		{"%number%[ ]*[ ]\\(%number%\\)", new PatternInfo(Operator.MULT, false, true)},
		{"%number%[ ]*[ ]%number%", new PatternInfo(Operator.MULT, false, false)},
		
		{"\\(%number%\\)[ ]/[ ]\\(%number%\\)", new PatternInfo(Operator.DIV, true, true)},
		{"\\(%number%\\)[ ]/[ ]%number%", new PatternInfo(Operator.DIV, true, false)},
		{"%number%[ ]/[ ]\\(%number%\\)", new PatternInfo(Operator.DIV, false, true)},
		{"%number%[ ]/[ ]%number%", new PatternInfo(Operator.DIV, false, false)},
		
		{"\\(%number%\\)[ ]^[ ]\\(%number%\\)", new PatternInfo(Operator.EXP, true, true)},
		{"\\(%number%\\)[ ]^[ ]%number%", new PatternInfo(Operator.EXP, true, false)},
		{"%number%[ ]^[ ]\\(%number%\\)", new PatternInfo(Operator.EXP, false, true)},
		{"%number%[ ]^[ ]%number%", new PatternInfo(Operator.EXP, false, false)},
		
	});
	
	static {
		Skript.registerExpression(ExprArithmetic.class, Number.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
	}
	
	@SuppressWarnings("null")
	private Expression<? extends Number> first;
	@SuppressWarnings("null")
	private Expression<? extends Number> second;
	@SuppressWarnings("null")
	private Operator op;
	
	@SuppressWarnings("null")
	private Class<? extends Number> returnType;
	private boolean integer;
	
	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();
	
	// A parsed chain, like a tree
	private Gettable gettable;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = (Expression<? extends Number>) exprs[0];
		second = (Expression<? extends Number>) exprs[1];
		
		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		op = patternInfo.operator;
		
		if (op == Operator.DIV || op == Operator.EXP) {
			returnType = Double.class;
		} else {
			Class<?> firstReturnType = first.getReturnType();
			Class<?> secondReturnType = second.getReturnType();
			
			Class<?>[] integers = {Long.class, Integer.class, Short.class, Byte.class};
			
			boolean firstIsInt = false;
			boolean secondIsInt = false;
			for (final Class<?> i : integers) {
				firstIsInt |= i.isAssignableFrom(firstReturnType);
				secondIsInt |= i.isAssignableFrom(secondReturnType);
			}
			
			if (firstIsInt && secondIsInt)
				returnType = Long.class;
			else
				returnType = Double.class;
		}
		
		integer = returnType == Long.class;
		
		// Chaining
		if (first instanceof ExprArithmetic && !patternInfo.leftGrouped) {
			chain.addAll(((ExprArithmetic) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(op);
		if (second instanceof ExprArithmetic && !patternInfo.rightGrouped) {
			chain.addAll(((ExprArithmetic) second).chain);
		} else {
			chain.add(second);
		}
		
		gettable = ArithmeticChain.parse(chain);
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Number[] get(final Event e) {
		Number[] one = (Number[]) Array.newInstance(returnType, 1);
		
		one[0] = gettable.get(e, integer);
		
		return one;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return first.toString(e, debug) + " " + op + " " + second.toString(e, debug);
	}
	
	@SuppressWarnings("null")
	@Override
	public Expression<? extends Number> simplify() {
		if (first instanceof Literal && second instanceof Literal)
			return new SimpleLiteral<>(getArray(null), Number.class, false);
		return this;
	}
	
}
