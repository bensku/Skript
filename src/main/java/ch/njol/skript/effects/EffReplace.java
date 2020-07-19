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
package ch.njol.skript.effects;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

enum ReplacementTypes {
	ALL,
	FIRST,
	LAST
}

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text and returns the replaced text.")
@Examples({"replace \"<item>\" in {textvar} with \"%item%\"",
	"replace every \"&\" with \"§\" in line 1",
	"# The following acts as a simple chat censor:",
	"on chat:",
	"\treplace all \"kys\", \"idiot\" and \"noob\" with \"****\" in the message",
	"#If you'd like a more advanced chat censor you can use regex:",
	"on chat:",
	"\treplace all \"(i|1)d(i|1)(o|0)(t|7)\" with \"****\" in message # which will block 'idiot' but also words in which a vowel is replaced with a number",
	"replace all stone and dirt in player's inventory and player's top inventory with diamond"})
@Since("2.0, 2.2-dev24 (replace in muliple strings and replace items in inventory), INSERT VERSION (replace first, replace last, case sensitivity and regex support)")
public class EffReplace extends Effect {
	static {
		Skript.registerEffect(EffReplace.class,
			"[(4¦case-sensitive)] replace (1¦first|2¦last|0¦all|every|) %strings% with %string% in %strings%",
			"[(4¦case-sensitive)] replace (1¦first|2¦last|0¦all|every|) %strings% in %strings% with %string%",
			"regex replace (1¦first|2¦last|0¦all|every|) %strings% with %string% in %strings%",
			"replace (all|every|) %itemtypes% in %inventories% with %itemtype%",
			"replace (all|every|) %itemtypes% with %itemtype% in %inventories%"
		);
	}
	
	private String type = "ALL";
	@SuppressWarnings("null")
	private Expression<?> storage, textToReplace, replacement;
	private boolean caseSensitive = false;
	private boolean regex = false;
	private boolean replaceString = false;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern < 3) {
			replaceString = true;
			int mark = parseResult.mark;
			regex = matchedPattern == 2;
			if (SkriptConfig.caseSensitive.value() || (parseResult.mark ^ 4) < 4)
				caseSensitive = true;
			type = ReplacementTypes.values()[mark < 4 ? mark : (mark ^ 4)].name();
		}
		if (matchedPattern % 2 == 1) {
			textToReplace = expressions[0];
			replacement = expressions[2];
			storage = expressions[1];
		} else {
			textToReplace = expressions[0];
			replacement = expressions[1];
			storage = expressions[2];
		}
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected void execute(Event e) {
		Object[] storage = this.storage.getAll(e);
		Object[] textToReplace = this.textToReplace.getAll(e);
		Object replacement = this.replacement.getSingle(e);
		if (replaceString) {
			if (storage == null || textToReplace == null || replacement == null) return;
			Object[] oldtextToReplace = Arrays.stream((String[]) textToReplace).map((he) -> ((caseSensitive ? "" : "(?i)") + (regex ? he : Pattern.quote((String) he)))).toArray();
			String newText = (String) replacement;
			if (!regex) newText = newText.replace("$", "\\$");
			switch (type) {
				case "ALL":
					for (int x = 0; x < storage.length; x++) {
						for (Object s : oldtextToReplace) {
							storage[x] = ((String) storage[x]).replaceAll((String) s, newText);
						}
					}
					break;
				case "FIRST":
					for (int x = 0; x < storage.length; x++) {
						for (Object s : oldtextToReplace) {
							storage[x] = ((String) storage[x]).replaceFirst((String) s, newText);
						}
					}
					break;
				case "LAST":
					for (int x = 0; x < storage.length; x++) {
						for (Object s : oldtextToReplace) {
							Matcher matcher = Pattern.compile((String) s).matcher((String) storage[x]);
							if (!matcher.find()) continue;
							int lastMatchStart = 0;
							do {
								lastMatchStart = matcher.start();
							} while (matcher.find());
							matcher.find(lastMatchStart);
							StringBuffer sb = new StringBuffer();
							matcher.appendReplacement(sb, newText);
							matcher.appendTail(sb);
							storage[x] = sb.toString();
						}
					}
					break;
			}
			this.storage.change(e, storage, Changer.ChangeMode.SET);
		} else {
			for (Inventory inv : (Inventory[]) storage)
				for (ItemType item : (ItemType[]) textToReplace)
					for (Integer slot : inv.all(item.getRandom()).keySet()) {
						inv.setItem(slot.intValue(), ((ItemType) replacement).getRandom());
					}
		}
	}
	
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "replace " + type + " " + textToReplace.toString(e, debug) + " with " + replacement.toString(e, debug) + " in " + storage.toString(e, debug);
		
	}
}
