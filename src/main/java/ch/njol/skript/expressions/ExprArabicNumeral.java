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
	
	static {
		register(ExprArabicNumeral.class, Number.class, "arabic num(ber|eral)", "string");
	}
	
	public static int arabic(String n) {
		if (n.startsWith("M")) return arabic(n.substring(1)) + 1000;
		if (n.startsWith("CM")) return arabic(n.substring(2)) + 900;
		if (n.startsWith("D")) return arabic(n.substring(1)) + 500;
		if (n.startsWith("CD")) return arabic(n.substring(2)) + 400;
		if (n.startsWith("C")) return arabic(n.substring(1)) + 100;
		if (n.startsWith("XC")) return arabic(n.substring(2)) + 90;
		if (n.startsWith("L")) return arabic(n.substring(1)) + 50;
		if (n.startsWith("XL")) return arabic(n.substring(2)) + 40;
		if (n.startsWith("X")) return arabic(n.substring(1)) + 10;
		if (n.startsWith("IX")) return arabic(n.substring(2)) + 9;
		if (n.startsWith("V")) return arabic(n.substring(1)) + 5;
		if (n.startsWith("IV")) return arabic(n.substring(2)) + 4;
		if (n.startsWith("I")) return arabic(n.substring(1)) + 1;
		return 0;
	}
	
	@Nullable
	@Override
	public Number convert(String s) {
		return arabic(s);
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
