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
package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

// TODO handle spaces properly
public class LiteralPatternElement extends PatternElement {

	private final boolean startSpace;
	private final String literal;

	public LiteralPatternElement(String literal) {
		this.startSpace = literal.startsWith(" ");
		if (literal.startsWith(" "))
			literal = literal.substring(1);
		this.literal = literal.toLowerCase();
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		int exprOffset = matchResult.exprOffset;
		if (startSpace) {
			if (expr.charAt(exprOffset) == ' ') {
				exprOffset++;
			} else if (exprOffset == 0 || expr.charAt(exprOffset - 1) != ' ') {
				return null;
			}
		}

		if (expr.toLowerCase().startsWith(literal, exprOffset)) {
			matchResult.exprOffset = exprOffset + literal.length();
			return matchNext(expr, matchResult);
		}
		return null;
	}

	@Override
	public String toString() {
		return (startSpace ? " " : "") + literal;
	}

}
