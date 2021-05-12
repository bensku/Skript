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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum RomanNumeral {
	I(1), IV(4), V(5), IX(9), X(10),
	XL(40), L(50), XC(90), C(100),
	CD(400), D(500), CM(900), M(1000), MG(4000), G(5000), MH(9000), H(10000);

	private int value;

	RomanNumeral(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static List<RomanNumeral> getReverseSortedValues() {
		return Arrays.stream(values())
			.sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
			.collect(Collectors.toList());
	}
}