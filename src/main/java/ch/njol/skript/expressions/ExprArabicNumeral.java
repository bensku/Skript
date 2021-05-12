/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.RomanNumeral;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;


@Name("Arabic Numeral")
@Description("The Arabic Numeral of a Roman Numeral. Even though the usual maximum roman number is 3999 we added 'H' and 'G' respectively for the 10000 and 5000 numbers, to increase the maximum number.")
@Examples("set {_r} to arabic numeral of \"XVI\"")
@Since("INSERT VERSION")
public class ExprArabicNumeral extends SimplePropertyExpression<String, Number> {


	static {
		register(ExprArabicNumeral.class, Number.class, "arabic num(ber|eral)", "string");
	}

	@Nullable
	public static Number toArabic(String romanNumeral) {
		int result = 0;

		List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

		int i = 0;

		while ((romanNumeral.length() > 0) && (i < romanNumerals.size())) {
			RomanNumeral symbol = romanNumerals.get(i);
			if (romanNumeral.startsWith(symbol.name())) {
				result += symbol.getValue();
				romanNumeral = romanNumeral.substring(symbol.name().length());
			} else {
				i++;
			}
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
