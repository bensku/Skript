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

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text and returns the replaced text.")
@Examples({"on chat:",
	"\tsend replace all \"look\",\"up\" with \"watch\" in \"You should look up\" #You should watch watch"})
@Since("INSERT VERSION")
public class EffReplace extends Effect {
	static {
		Skript.registerEffect(EffReplace.class,
			"[regex] replace (1¦first|2¦last|3¦%-number%(st|nd|rd|th)|0¦all|every|) %strings% with %string% in %string% [with case sensitivity]",
			"[regex] replace (1¦first|2¦last|3¦%-number%(st|nd|rd|th)|0¦all|every|) %strings% in %string% with %string% (|4¦with case sensitivity)",
			"replace (all|every|) %itemtypes% with %itemtype% in %inventories%",
			"replace (all|every|) %itemtypes% in %inventories% with %itemtype%"
		);
	}

	@SuppressWarnings("null")
	Expression<Number> occurrenceN = null;
	@SuppressWarnings("null")
	private Expression<?> haystack, needles, replacement;
	private boolean caseSensitive = false;
	private boolean regex = false;
	private boolean replaceString = false;
	private String type = "ALL";

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern < 3) {
			replaceString = true;
			if (parseResult.mark == 1) type = "FIRST";
			if (parseResult.mark == 2) type = "LAST";
			if (parseResult.expr.startsWith("regex")) regex = true;
			if (SkriptConfig.caseSensitive.value() || parseResult.expr.endsWith("with case sensitivity"))
				caseSensitive = true;
			if (parseResult.mark == 3) {
				type = "TH";
				occurrenceN = (Expression<Number>) exprs[0];
			}
		}
		int start = replaceString ? 1 : 0;
		if (matchedPattern % 2 == 1) {
			needles = exprs[start];
			replacement = exprs[start + 2];
			haystack = exprs[start + 1];
		} else {
			needles = exprs[start];
			replacement = exprs[start + 1];
			haystack = exprs[start + 2];
		}
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected void execute(Event e) {
		Object[] haystack = this.haystack.getAll(e);
		Object[] needles = this.needles.getAll(e);
		Object replacement = this.replacement.getSingle(e);
		if (replaceString) {
			if (haystack == null || needles == null || replacement == null) return;
			Object[] oldNeedles = Arrays.stream((String[]) needles).map((he) -> ((caseSensitive ? "" : "(?i)") + (regex ? he : Pattern.quote((String) he)))).toArray();
			String newText = (String) replacement;
			if (!regex) newText = newText.replace("$", "\\$");
			if (type == "FIRST") {
				for (int x = 0; x < haystack.length; x++) {
					for (Object s : oldNeedles) {
						haystack[x] = ((String) haystack[x]).replaceFirst(s.toString(), newText);
					}
				}
			} else if (type == "ALL") {
				for (int x = 0; x < haystack.length; x++) {
					for (Object s : oldNeedles) {
						haystack[x] = ((String) haystack[x]).replaceAll(s.toString(), newText);
					}
				}
			} else if (type == "LAST") {
				for (int x = 0; x < haystack.length; x++) {
					for (Object s : oldNeedles) {
						Matcher matcher = Pattern.compile(s.toString()).matcher((String) haystack[x]);
						if (!matcher.find()) continue;
						int lastMatchStart = 0;
						do {
							lastMatchStart = matcher.start();
						} while (matcher.find());
						matcher.find(lastMatchStart);
						StringBuffer sb = new StringBuffer();
						matcher.appendReplacement(sb, newText);
						matcher.appendTail(sb);
						haystack[x] = sb.toString();
					}
				}
			} else if (type == "TH" && occurrenceN != null) {
				for (int x = 0; x < haystack.length; x++) {
					for (Object s : oldNeedles) {
						Matcher matcher = Pattern.compile(s.toString()).matcher((String) haystack[x]);
						Integer number = occurrenceN.getSingle(e).intValue();
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
						} catch (IndexOutOfBoundsException exception) {
							continue;
						}
						matcher.appendTail(sb);
						haystack[x] = sb.toString();
					}
				}
			}
			this.haystack.change(e, haystack, Changer.ChangeMode.SET);
		} else {
			for (Inventory inv : (Inventory[]) haystack)
				for (ItemType item : (ItemType[]) needles)
					for (Integer slot : inv.all(item.getRandom()).keySet()) {
						inv.setItem(slot.intValue(), ((ItemType) replacement).getRandom());
					}
		}
	}


	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "replace " + type + " " + needles.toString(e, debug) + " with " + replacement.toString(e, debug) + " in " + haystack.toString(e, debug);

	}
}
