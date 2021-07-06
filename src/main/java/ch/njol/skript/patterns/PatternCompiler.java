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

	public static SkriptPattern compile(String pattern) {
		if (pattern.isEmpty())
			throw new InvalidPatternException(pattern, "Pattern is empty");

		AtomicInteger atomicInteger = new AtomicInteger(0);
		PatternElement first = compile(pattern, atomicInteger);
		return new SkriptPattern(first, atomicInteger.get());
	}

	private static PatternElement compile(String pattern, AtomicInteger expressionOffset) {
		StringBuilder literalBuilder = new StringBuilder();
		PatternElement first = null;

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '[') {
				if (literalBuilder.length() != 0) {
					first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = SkriptParser.nextBracket(pattern, ']', '[', i + 1, true);
				PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);

				first = setFirst(first, new OptionalPatternElement(patternElement));

				i = end;
			} else if (c == '(') {
				if (literalBuilder.length() != 0) {
					first = setFirst(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = SkriptParser.nextBracket(pattern, ')', '(', i + 1, true);

				List<String> parts = new ArrayList<>();
				int lastFound = ++i;
				for (; i < end; i++) {
					char innerChar = pattern.charAt(i);
					if (innerChar == '(') {
						i = SkriptParser.nextBracket(pattern, ')', '(', i + 1, true);
					} else if (innerChar == '|') {
						parts.add(pattern.substring(lastFound, i));
						lastFound = i + 1;
					} else if (innerChar == '\\') {
						i++;
					}
				}

				parts.add(pattern.substring(lastFound, i));

				List<Choice> choices = new ArrayList<>();
				for (String part : parts) {
					int mark = 0;
					if ((i = part.indexOf('¦')) != -1) {
						String intString = part.substring(0, i);
						part = part.substring(i + 1);
						try {
							mark = Integer.parseInt(intString);
						} catch (NumberFormatException e) {
							throw new InvalidPatternException(pattern, "Invalid parse mark", e);
						}
					}

					PatternElement patternElement = compile(part, expressionOffset);
					choices.add(new Choice(patternElement, mark));
				}

				first = setFirst(first, new ChoicePatternElement(choices.toArray(new Choice[0])));

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

				i = end + 1;
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
			return new LiteralPatternElement("");
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
