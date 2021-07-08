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

public class ChoicePatternElement extends PatternElement {

	public static class Choice {
		private final PatternElement patternElement;
		private final int mark;

		public Choice(PatternElement patternElement, int mark) {
			this.patternElement = patternElement;
			this.mark = mark;
		}

		public PatternElement getPatternElement() {
			return patternElement;
		}

		public int getMark() {
			return mark;
		}
	}

	private final Choice[] choices;

	public ChoicePatternElement(Choice[] choices) {
		this.choices = choices;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		for (Choice choice : choices)
			choice.patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		for (Choice choice : choices) {
			MatchResult matchResultCopy = matchResult.copy();
			matchResultCopy.mark ^= choice.mark;
			MatchResult newMatchResult = choice.patternElement.match(expr, matchResultCopy);
			if (newMatchResult != null)
				return newMatchResult;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder()
			.append("(");
		int i = choices.length;
		for (Choice choice : choices) {
			i--;
			if (choice.mark != 0) {
				stringBuilder
					.append(choice.mark)
					.append("¦");
			}
			stringBuilder.append(choice.patternElement.toFullString());
			if (i != 0)
				stringBuilder.append("|");
		}
		return stringBuilder.append(")").toString();
	}
}
