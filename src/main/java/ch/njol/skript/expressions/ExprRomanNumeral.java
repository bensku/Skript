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

@Name("Roman Numeral")
@Description("The Roman Numeral of an Arabic Numeral. Even though the usual maximum roman number is 3999 we added 'H' and 'G' respectively for the 10000 and 5000 numbers, to increase the maximum number.")
@Examples("set {_r} to roman numeral of 49")
@Since("INSERT VERSION")
public class ExprRomanNumeral extends SimplePropertyExpression<Number, String> {


	static {
		register(ExprRomanNumeral.class, String.class, "roman num(ber|eral)", "number");
	}

	@Nullable
	public static String toRoman(int number) {
		if ((number <= 0) || (number > 40000)) {
			throw new IllegalArgumentException(number + " is not in range (0,40000]");
		}

		List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

		int i = 0;
		StringBuilder sb = new StringBuilder();

		while ((number > 0) && (i < romanNumerals.size())) {
			RomanNumeral currentSymbol = romanNumerals.get(i);
			if (currentSymbol.getValue() <= number) {
				sb.append(currentSymbol.name());
				number -= currentSymbol.getValue();
			} else {
				i++;
			}
		}
		return sb.toString();
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
