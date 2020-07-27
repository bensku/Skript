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
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Roman Numeral")
@Description("The Roman Numeral of an Arabic Numeral. Even though the usual maximum roman number is 3999 we added 'H' and 'G' respectively for the 10000 and 5000 numbers, to make the maximum be 49,999.")
@Examples("set {_r} to roman numeral of 49")
@Since("INSERT VERSION")
public class ExprRomanNumeral extends SimplePropertyExpression<Number, String> {
	
	private final static HashMap<Integer, String> map = new HashMap<Integer, String>() {{
		put(10000, "H");
		put(5000, "G");
		put(1000, "M");
		put(500, "D");
		put(100, "C");
		put(50, "L");
		put(10, "X");
		put(5, "V");
		put(1, "I");
	}};
	
	static {
		register(ExprRomanNumeral.class, String.class, "roman num(ber|eral)", "number");
	}
	
	@Nullable
	public static String toRoman(int number) {
		if (number >= 50000 || number < 1) return null;
		int significantDigit = 1;
		while (number >= significantDigit) significantDigit *= 10;
		significantDigit /= 10;
		
		String result = "";
		while (number > 0) {
			int lastNum = Math.floorDiv(number, significantDigit);
			if (lastNum <= 3)
				result += StringUtils.repeat(map.get(significantDigit), lastNum);
			else if (lastNum == 4)
				result += (map.get(significantDigit) + map.get(significantDigit * 5));
			else if (lastNum <= 8)
				result += (map.get(significantDigit * 5) + StringUtils.repeat(map.get(significantDigit), lastNum - 5));
			else if (lastNum == 9)
				result += (map.get(significantDigit) + map.get(significantDigit * 10));
			number = (int) Math.floor(number % significantDigit);
			significantDigit /= 10;
		}
		return result;
	}
	
	@Nullable
	@Override
	public String convert(Number number) {
		return toRoman(number.intValue());
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "roman numeral";
	}
	
}
