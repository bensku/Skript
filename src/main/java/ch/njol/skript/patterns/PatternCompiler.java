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

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.patterns.ChoicePatternElement.Choice;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternCompiler {

	private static final PatternElement EMPTY = new LiteralPatternElement("");

	public static SkriptPattern compile(String pattern) {
		AtomicInteger atomicInteger = new AtomicInteger(0);
		PatternElement first = compile(pattern, atomicInteger);
		return new SkriptPattern(first, atomicInteger.get());
	}

	private static PatternElement compile(String pattern, AtomicInteger expressionOffset) {
		part_parsing: {
			List<String> parts = new ArrayList<>();

			int i = 0;
			int lastFound = 0;
			for (; i < pattern.length(); i++) {
				char c = pattern.charAt(i);
				if (c == '(') {
					i = SkriptParser.nextBracket(pattern, ')', '(', i + 1, true);
				} else if (c == '|') {
					parts.add(pattern.substring(lastFound, i));
					lastFound = i + 1;
				} else if (c == '\\') {
					i++;
				}
			}

			parts.add(pattern.substring(lastFound, i));

			List<Choice> choices = new ArrayList<>();
			for (String part : parts) {
				int mark = 0;
				if ((i = part.indexOf('¦')) != -1) {
					String intString = part.substring(0, i);
					try {
						mark = Integer.parseInt(intString);
						part = part.substring(i + 1);
					} catch (NumberFormatException ignored) {
					} // Do nothing
				}

				if (parts.size() == 1 && mark == 0) {
					break part_parsing;
				}

				PatternElement patternElement = compile(part, expressionOffset);
				choices.add(new Choice(patternElement, mark));
			}

			return new ChoicePatternElement(choices.toArray(new Choice[0]));
		}

		StringBuilder literalBuilder = new StringBuilder();
		PatternElement first = null;

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '[' || c == '(') {
				if (literalBuilder.length() != 0) {
					first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = SkriptParser.nextBracket(pattern, c == '[' ? ']' : ')', c, i + 1, true);
				PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);

				if (c == '[') {
					first = setFirst(first, new OptionalPatternElement(patternElement));
				} else {
					first = setFirst(first, patternElement);
				}

				i = end;
			} else if (c == '%') {
				if (literalBuilder.length() != 0) {
					first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = pattern.indexOf('%', i + 1);
				int exprOffset = expressionOffset.getAndIncrement();
				TypePatternElement typePatternElement = TypePatternElement.fromString(pattern.substring(i + 1, end), exprOffset);

				first = setFirst(first, typePatternElement);

				i = end;
			} else if (c == '<') {
				if (literalBuilder.length() != 0) {
					first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = pattern.indexOf('>', i + 1);
				if (end == -1)
					throw new InvalidPatternException(pattern, "Missing closing regex bracket '>'");

				Pattern regexPattern;
				try {
					regexPattern = Pattern.compile(pattern.substring(i + 1, end));
				} catch (final PatternSyntaxException e) {
					throw new InvalidPatternException(pattern, "Invalid regex <" + pattern.substring(i + 1, end) + ">", e);
				}

				first = setFirst(first, new RegexPatternElement(regexPattern));

				i = end;
			} else if (c == '\\') {
				i++;
				literalBuilder.append(pattern.charAt(i));
			} else {
				literalBuilder.append(c);
			}
		}

		if (literalBuilder.length() != 0) {
			first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
		}

		if (first == null) {
			return EMPTY;
		}

		return first;
	}

	private static PatternElement setFirst(@Nullable PatternElement first, PatternElement next) {
		if (first == null) {
			return next;
		} else {
			PatternElement last = first;
			while (last.next != null)
				last = last.next;

			last.setNext(next);
			last.originalNext = next;
			return first;
		}
	}

}
