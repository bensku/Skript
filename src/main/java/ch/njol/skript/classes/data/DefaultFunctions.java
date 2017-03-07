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
package ch.njol.skript.classes.data;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;

/**
 * FIXME generate/write documentation
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
public class DefaultFunctions {
	public DefaultFunctions() {}
	
	private final static String s(final double n) {
		return StringUtils.toString(n, 4);
	}
	
	static {
		
		final ClassInfo<Number> numberClass = Classes.getExactClassInfo(Number.class);
		final ClassInfo<Long> longClass = Classes.getExactClassInfo(Long.class);
		
		final Parameter<?>[] numberParam = new Parameter[] {new Parameter<>("n", numberClass, true, null)};
		final Parameter<?>[] numbersParam = new Parameter[] {new Parameter<>("ns", numberClass, false, null)};
		
		// basic math functions
		
		Functions.registerFunction(new JavaFunction<Long>("floor", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.floor(((Number) params[0][0]).doubleValue())};
			}
		}.description("Rounds a number down, i.e. returns the closest integer smaller than or equal to the argument.")
				.examples("floor(2.34) = 2", "floor(2) = 2", "floor(2.99) = 2")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Long>("round", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.round(((Number) params[0][0]).doubleValue())};
			}
		}.description("Rounds a number, i.e. returns the closest integer to the argument.")
				.examples("round(2.34) = 2", "round(2) = 2", "round(2.99) = 3", "round(2.5) = 3")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Long>("ceil", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		}.description("Rounds a number down, i.e. returns the closest integer larger than or equal to the argument.")
				.examples("ceil(2.34) = 3", "ceil(2) = 2", "ceil(2.99) = 3")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Long>("ceiling", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		}.description("Alias of <a href='#ceil'>ceil</a>.")
				.examples("ceiling(2.34) = 3", "ceiling(2) = 2", "ceiling(2.99) = 3")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("abs", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Number n = (Number) params[0][0];
				if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
					return new Long[] {Math.abs(n.longValue())};
				return new Double[] {Math.abs(n.doubleValue())};
			}
		}.description("Returns the absolute value of the argument, i.e. makes the argument positive.")
				.examples("abs(3) = 3", "abs(-2) = 2")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("mod", new Parameter[] {new Parameter<>("d", numberClass, true, null), new Parameter<>("m", numberClass, true, null)}, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Number d = (Number) params[0][0];
				final Number m = (Number) params[1][0];
				final double mm = m.doubleValue();
				if (mm == 0)
					return new Double[] {Double.NaN};
				return new Double[] {Math2.mod(d.doubleValue(), mm)};
			}
		}.description("Returns the modulo of the given arguments, i.e. the remainder of the division <code>d/m</code>, where d and m are the arguments of this function.",
				"The returned value is always positive. Returns NaN (not a number) if the second argument is zero.")
				.examples("mod(3, 2) = 1", "mod(256436, 100) = 36", "mod(-1, 10) = 9")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("exp", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.exp(((Number) params[0][0]).doubleValue())};
			}
		}.description("The exponential function. You probably don't need this if you don't know what this is.")
				.examples("exp(0) = 1", "exp(1) = " + s(Math.exp(1)))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("ln", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.log(((Number) params[0][0]).doubleValue())};
			}
		}.description("The natural logarithm. You probably don't need this if you don't know what this is.",
				"Returns NaN (not a number) if the argument is negative.")
				.examples("ln(1) = 0", "ln(exp(5)) = 5", "ln(2) = " + StringUtils.toString(Math.log(2), 4))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("log", new Parameter[] {new Parameter<>("n", numberClass, true, null), new Parameter<>("base", numberClass, true, new SimpleLiteral<Number>(10, false))}, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.log10(((Number) params[0][0]).doubleValue()) / Math.log10(((Number) params[1][0]).doubleValue())};
			}
		}.description("A logarithm, with base 10 if none is specified. This is the inverse operation to exponentiation (for positive bases only), i.e. <code>log(base ^ exponent, base) = exponent</code> for any positive number 'base' and any number 'exponent'.",
				"Another useful equation is <code>base ^ log(a, base) = a</code> for any numbers 'base' and 'a'.",
				"Please note that due to how numbers are represented in computers, these equations do not hold for all numbers, as the computed values may slightly differ from the correct value.",
				"Returns NaN (not a number) if any of the arguments are negative.")
				.examples("log(100) = 2 # 10^2 = 100", "log(16, 2) = 4 # 2^4 = 16")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("sqrt", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.sqrt(((Number) params[0][0]).doubleValue())};
			}
		}.description("The square root, which is the inverse operation to squaring a number (for positive numbers only). This is the same as <code>(argument) ^ (1/2)</code> – other roots can be calculated via <code>number ^ (1/root)</code>, e.g. <code>set {_l} to {_volume}^(1/3)</code>.",
				"Returns NaN (not a number) if the argument is negative.")
				.examples("sqrt(4) = 2", "sqrt(2) = " + s(Math.sqrt(2)), "sqrt(-1) = " + s(Math.sqrt(-1)))
				.since("2.2"));
		
		// trigonometry
		
		Functions.registerFunction(new JavaFunction<Number>("sin", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.sin(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The sine function. It starts at 0° with a value of 0, goes to 1 at 90°, back to 0 at 180°, to -1 at 270° and then repeats every 360°. Uses degrees, not radians.")
				.examples("sin(90) = 1", "sin(60) = " + s(Math.sin(Math.toRadians(60))))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("cos", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.cos(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The cosine function. This is basically the <a href='#sin'>sine</a> shifted by 90°, i.e. <code>cos(a) = sin(a + 90°)</code>, for any number a. Uses degrees, not radians.")
				.examples("cos(0) = 1", "cos(90) = 0")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("tan", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.tan(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The tangent function. This is basically <code><a href='#sin'>sin</a>(arg)/<a href='#cos'>cos</a>(arg)</code>. Uses degrees, not radians.")
				.examples("tan(0) = 0", "tan(45) = 1", "tan(89.99) = " + s(Math.tan(Math.toRadians(89.99))))
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("asin", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.asin(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#sin'>sine</a>, also called arcsin. Returns result in degrees, not radians. Only returns values from -90 to 90.")
				.examples("asin(0) = 0", "asin(1) = 90", "asin(0.5) = " + s(Math.toDegrees(Math.asin(0.5))))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("acos", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.acos(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#cos'>cosine</a>, also called arccos. Returns result in degrees, not radians. Only returns values from 0 to 180.")
				.examples("acos(0) = 90", "acos(1) = 0", "acos(0.5) = " + s(Math.toDegrees(Math.asin(0.5))))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("atan", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.atan(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#tan'>tangent</a>, also called arctan. Returns result in degrees, not radians. Only returns values from -90 to 90.")
				.examples("atan(0) = 0", "atan(1) = 45", "atan(10000) = " + s(Math.toDegrees(Math.atan(10000))))
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("atan2", new Parameter[] {new Parameter<>("x", numberClass, true, null), new Parameter<>("y", numberClass, true, null)}, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.atan2(((Number) params[1][0]).doubleValue(), ((Number) params[0][0]).doubleValue()))};
			}
		}.description("Similar to <a href='#atan'>atan</a>, but requires two coordinates and returns values from -180 to 180.",
				"The returned angle is measured counterclockwise in a standard mathematical coordinate system (x to the right, y to the top).")
				.examples("atan2(0, 1) = 0", "atan2(10, 0) = 90", "atan2(-10, 5) = " + s(Math.toDegrees(Math.atan2(-10, 5))))
				.since("2.2"));
		
		// more stuff
		
		Functions.registerFunction(new JavaFunction<Number>("sum", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double sum = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					sum += ((Number) ns[i]).doubleValue();
				return new Double[] {sum};
			}
		}.description("Sums a list of numbers.")
				.examples("sum(1) = 1", "sum(2, 3, 4) = 9", "sum({some list variable::*})", "sum(2, {_v::*}, and the player's y-coordinate)")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("product", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double product = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					product *= ((Number) ns[i]).doubleValue();
				return new Double[] {product};
			}
		}.description("Calculates the product of a list of numbers.")
				.examples("product(1) = 1", "product(2, 3, 4) = 24", "product({some list variable::*})", "product(2, {_v::*}, and the player's y-coordinate)")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Number>("max", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double max = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					final double d = ((Number) ns[i]).doubleValue();
					if (d > max || Double.isNaN(max))
						max = d;
				}
				return new Double[] {max};
			}
		}.description("Returns the maximum number from a list of numbers.")
				.examples("max(1) = 1", "max(1, 2, 3, 4) = 4", "max({some list variable::*})")
				.since("2.2"));
		Functions.registerFunction(new JavaFunction<Number>("min", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double min = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					final double d = ((Number) ns[i]).doubleValue();
					if (d < min || Double.isNaN(min))
						min = d;
				}
				return new Double[] {min};
			}
		}.description("Returns the minimum number from a list of numbers.")
				.examples("min(1) = 1", "min(1, 2, 3, 4) = 1", "min({some list variable::*})")
				.since("2.2"));
		
		// misc
		
		Functions.registerFunction(new JavaFunction<World>("world", new Parameter[] {new Parameter<>("name", Classes.getExactClassInfo(String.class), true, null)}, Classes.getExactClassInfo(World.class), true) {
			@Override
			@Nullable
			public World[] execute(final FunctionEvent e, final Object[][] params) {
				final World w = Bukkit.getWorld((String) params[0][0]);
				return w == null ? new World[0] : new World[] {w};
			}
		}).description("Gets a world from its name.")
				.examples("set {_nether} to world(\"%{_world}%_nether\")")
				.since("2.2");
		
		// the location expression doesn't work, so why not make a function for the same purpose
		// FIXME document on ExprLocation as well
		Functions.registerFunction(new JavaFunction<Location>("location", new Parameter[] {
				new Parameter<>("x", numberClass, true, null), new Parameter<>("y", numberClass, true, null), new Parameter<>("z", numberClass, true, null),
				new Parameter<>("world", Classes.getExactClassInfo(World.class), true, new EventValueExpression<>(World.class)),
				new Parameter<>("yaw", numberClass, true, new SimpleLiteral<Number>(0, true)), new Parameter<>("pitch", numberClass, true, new SimpleLiteral<Number>(0, true))
		}, Classes.getExactClassInfo(Location.class), true) {
			@Override
			public Location[] execute(final FunctionEvent e, final Object[][] params) {
				return new Location[] {new Location((World) params[3][0],
						((Number) params[0][0]).doubleValue(), ((Number) params[1][0]).doubleValue(), ((Number) params[2][0]).doubleValue(),
						((Number) params[4][0]).floatValue(), ((Number) params[5][0]).floatValue())};
			}
		}.description("Creates a location from a world and 3 coordinates, with an optional yaw and pitch.")
				.examples("location(0, 128, 0)", "location(player's x-coordinate, player's y-coordinate + 5, player's z-coordinate, player's world, 0, 90)")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Date>("date", new Parameter[] {
				new Parameter<>("year", numberClass, true, null), new Parameter<>("month", numberClass, true, null), new Parameter<>("day", numberClass, true, null),
				new Parameter<>("hour", numberClass, true, new SimpleLiteral<Number>(0, true)), new Parameter<>("minute", numberClass, true, new SimpleLiteral<Number>(0, true)), new Parameter<>("second", numberClass, true, new SimpleLiteral<Number>(0, true)), new Parameter<>("millisecond", numberClass, true, new SimpleLiteral<Number>(0, true)),
				new Parameter<>("zone_offset", numberClass, true, new SimpleLiteral<Number>(Double.NaN, true)), new Parameter<>("dst_offset", numberClass, true, new SimpleLiteral<Number>(Double.NaN, true)),
		}, Classes.getExactClassInfo(Date.class), true) {
			private final int[] fields = {
					Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
					Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND,
					Calendar.ZONE_OFFSET, Calendar.DST_OFFSET};
			private final int[] offsets = {
					0, -1, 0,
					0, 0, 0, 0,
					0, 0};
			private final double[] scale = {
					1, 1, 1,
					1, 1, 1, 1,
					1000 * 60, 1000 * 60};
			private final double[] relations = {
					1. / 12, 1. / 30,
					1. / 24, 1. / 60, 1. / 60, 1. / 1000,
					0, 0,
					0};
			{
				assert fields.length == offsets.length && offsets.length == scale.length && scale.length == relations.length && getMaxParameters() == fields.length;
			}
			
			@Override
			@Nullable
			public Date[] execute(final FunctionEvent e, final Object[][] params) {
				final Calendar c = Calendar.getInstance();
				c.setLenient(true);
				double carry = 0;
				for (int i = 0; i < fields.length; i++) {
					final int field = fields[i];
					final Number n = (Number) params[i][0];
					if (n == null)
						return null;
					final double value = n.doubleValue() * scale[i] + offsets[i] + carry;
					final int v = Math2.floorI(value);
					carry = (value - v) * relations[i];
					if (field != Calendar.ZONE_OFFSET || field != Calendar.DST_OFFSET || !Double.isNaN(v))
						c.set(field, v);
				}
				return new Date[] {new Date(c.getTimeInMillis(), c.getTimeZone())};
			}
		}.description("Creates a date from a year, month, and day, and optionally also from hour, minute, second and millisecond.",
				"A time zone and DST offset can be specified as well (in minutes), if they are left out the server's time zone and DST offset are used (the created date will not retain this information).")
				.examples("date(2014, 10, 1) # 0:00, 1st October 2014", "date(1990, 3, 5, 14, 30) # 14:30, 5th May 1990", "date(1999, 12, 31, 23, 59, 59, 999, -3*60, 0) # almost year 2000 in parts of Brazil (-3 hours offset, no DST)")
				.since("2.2"));
		
		Functions.registerFunction(new JavaFunction<Vector>("vector", new Parameter[] {
				new Parameter<>("x", numberClass, true, null), new Parameter<>("y", numberClass, true, null), new Parameter<>("z", numberClass, true, null)
		}, Classes.getExactClassInfo(Vector.class), true) {

			@Override
			@Nullable
			public Vector[] execute(FunctionEvent e, Object[][] params) {
				return new Vector[] {new Vector(((Number)params[0][0]).doubleValue(), ((Number)params[1][0]).doubleValue(), ((Number)params[2][0]).doubleValue())};
			}
			
		}
		.description("Constructs a vector.")
		.examples("vector(0, 0, 0)")
		.since("2.2-dev23")
		);
		
	}
	
}
