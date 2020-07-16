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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text and returns the replaced text.")
@Examples({"on chat:",
	"\tsend replace all \"look\",\"up\" with \"watch\" in \"You should look up\" #You should watch watch"})
@Since("INSERT VERSION")
public class ExprReplace extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprReplace.class, String.class, ExpressionType.COMBINED, "[regex] replace (1¦first|2¦last|3¦%-number%(st|nd|rd|th)|0¦all|every|) %strings% with %string% in %string% [with case sensitivity]", "[regex] replace (1¦first|2¦last|3¦%-number%(st|nd|rd|th)|0¦all|every|) %strings% in %string% with %string% (|4¦with case sensitivity)");
	}

	@SuppressWarnings("null")
	Expression<Number> occurrenceN = null;
	@SuppressWarnings("null")
	private Expression<String> needles;
	@SuppressWarnings("null")
	private Expression<String> replacement;
	@SuppressWarnings("null")
	private Expression<String> haystack;
	private boolean caseSensitive = false;
	private boolean regex = false;
	private String type = "ALL";

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		if (parseResult.mark == 1) type = "FIRST";
		if (parseResult.mark == 2) type = "LAST";
		if (parseResult.expr.startsWith("regex")) regex = true;
		if (SkriptConfig.caseSensitive.value() || parseResult.expr.endsWith("with case sensitivity"))
			caseSensitive = true;
		if (parseResult.mark == 3) {
			type = "TH";
			occurrenceN = (Expression<Number>) expressions[0];
		}
		if (matchedPattern == 0) {
			needles = (Expression<String>) expressions[1];
			replacement = (Expression<String>) expressions[2];
			haystack = (Expression<String>) expressions[3];
		} else if (matchedPattern == 1) {
			needles = (Expression<String>) expressions[1];
			replacement = (Expression<String>) expressions[3];
			haystack = (Expression<String>) expressions[2];
		}
		return true;
	}

	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event event) {
		Object[] oldNeedles = Arrays.stream(needles.getAll(event)).map((String he) -> ((caseSensitive ? "" : "(?i)") + (regex ? he : Pattern.quote(he)))).toArray();
		String newText = replacement.getSingle(event);
		if (!regex) newText = newText.replace("$", "\\$");
		String newHaystack = haystack.getSingle(event);
		if (type == "FIRST") {
			for (Object s : oldNeedles) {
				newHaystack = newHaystack.replaceFirst(s.toString(), newText);
			}
		} else if (type == "ALL") {
			for (Object s : oldNeedles) {
				newHaystack = newHaystack.replaceAll(s.toString(), newText);
			}
		} else if (type == "LAST") {
			for (Object s : oldNeedles) {
				Matcher matcher = Pattern.compile(s.toString()).matcher(newHaystack);
				if (!matcher.find()) continue;
				int lastMatchStart = 0;
				do {
					lastMatchStart = matcher.start();
				} while (matcher.find());
				matcher.find(lastMatchStart);
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, newText);
				matcher.appendTail(sb);
				newHaystack = sb.toString();
			}
		} else if (type == "TH" && occurrenceN != null) {
			for (Object s : oldNeedles) {
				Matcher matcher = Pattern.compile(s.toString()).matcher(newHaystack);
				Integer number = occurrenceN.getSingle(event).intValue();
				if (!matcher.find()) continue;
				int nthMatchStart = 0;
				int found = 0;
				do {
					nthMatchStart = matcher.start();
					found++;
				} while (matcher.find() && found != number);
				if (found < number) continue;
				matcher.find(nthMatchStart);
				StringBuffer sb = new StringBuffer();
				try {
					matcher.appendReplacement(sb, newText);
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
				matcher.appendTail(sb);
				newHaystack = sb.toString();
			}
		}
		return new String[]{newHaystack};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "replace " + type + " " + needles.toString(e, debug) + " with " + replacement.toString(e, debug) + " in " + haystack.toString(e, debug);
	}

}
