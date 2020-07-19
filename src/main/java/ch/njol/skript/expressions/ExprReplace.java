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


enum ReplacementTypes {
	ALL,
	FIRST,
	LAST
}

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text and returns the replaced text.")
@Examples({"on chat:",
		"\tset {_hey} to replace all \"hello\" in message with \"hey\" #this will replace all the values without changing the message",
		"\tsend {_hey}",
		"set {_no} to case-sensitive replace first \"yes\" in \"Yes, yes\" with \"no\" #Only the second yes gets replaced with no"
})
@Since("INSERT VERSION")
public class ExprReplace extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprReplace.class, String.class, ExpressionType.COMBINED,
				"[(4¦case-sensitive)] replace (1¦first|2¦last|0¦all|every|) %strings% with %string% in %string%",
				"regex replace (1¦first|2¦last|0¦all|every|) %strings% with %string% in %string%");
	}
	
	String type = "ALL";
	@SuppressWarnings("null")
	private Expression<String> textToReplace, replacement, storage;
	private boolean caseSensitive = false;
	private boolean regex;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		int mark = parseResult.mark;
		if (matchedPattern == 1) regex = true;
		if (SkriptConfig.caseSensitive.value() || (parseResult.mark ^ 4) < 4)
			caseSensitive = true;
		textToReplace = (Expression<String>) expressions[0];
		replacement = (Expression<String>) expressions[1];
		storage = (Expression<String>) expressions[2];
		type = ReplacementTypes.values()[mark < 4 ? mark : (mark ^ 4)].name();
		return true;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event event) {
		Object[] oldtextToReplace = Arrays.stream(textToReplace.getAll(event)).map((String he) -> ((caseSensitive && !regex ? "" : "(?i)") + (regex ? he : Pattern.quote(he)))).toArray();
		String newText = replacement.getSingle(event);
		if (!regex) newText = newText.replace("$", "\\$");
		String newStorage = storage.getSingle(event);
		switch (type) {
			case "ALL":
				for (Object s : oldtextToReplace) {
					newStorage = newStorage.replaceAll((String) s, newText);
				}
			case "FIRST":
				for (Object s : oldtextToReplace) {
					newStorage = newStorage.replaceFirst((String) s, newText);
				}
				break;
			case "LAST":
				for (Object s : oldtextToReplace) {
					Matcher matcher = Pattern.compile((String) s).matcher(newStorage);
					if (!matcher.find()) continue;
					int lastMatchStart = 0;
					do {
						lastMatchStart = matcher.start();
					} while (matcher.find());
					matcher.find(lastMatchStart);
					StringBuffer sb = new StringBuffer();
					matcher.appendReplacement(sb, newText);
					matcher.appendTail(sb);
					newStorage = sb.toString();
				}
				break;
			
		}
		return new String[]{newStorage};
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
		return "replace " + type + " " + textToReplace.toString(e, debug) + " with " + replacement.toString(e, debug) + " in " + storage.toString(e, debug);
	}
	
}
