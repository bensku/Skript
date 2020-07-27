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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Arabic Numeral")
@Description("The Arabic Numeral of a Roman Numeral")
@Examples("set {_r} to arabic numeral of \"XVI\"")
@Since("INSERT VERSION")
public class ExprArabicNumeral extends SimplePropertyExpression<String, Number> {
	
	private final static HashMap<Character, Integer> map = new HashMap<Character, Integer>() {{
		put('H', 10000);
		put('G', 5000);
		put('M', 1000);
		put('D', 500);
		put('C', 100);
		put('L', 50);
		put('X', 10);
		put('V', 5);
		put('I', 1);
	}};
	
	static {
		register(ExprArabicNumeral.class, Number.class, "arabic num(ber|eral)", "string");
	}
	
	public static int toArabic(String n) {
		if (!n.matches("[HGMDCLXVI]+")) return 0;
		int previous = 0;
		int result = 0;
		int length = n.length();
		for (int i = 0; i < length; i++) {
			Integer num = map.get(n.charAt(i));
			if (num > previous) result += num - previous * 2;
			else result += num;
			previous = num;
		}
		return result;
	}
	
	@Nullable
	@Override
	public Number convert(String s) {
		return toArabic(s);
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "arabic numeral";
	}
	
}
