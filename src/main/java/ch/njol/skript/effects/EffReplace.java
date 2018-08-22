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
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@Name("Replace")
@Description({"Replaces all occurrences of a given text with another text. Please note that you can only change variables and a few expressions, e.g. a <a href='../expressions.html#ExprMessage'>message</a> or a line of a sign.",
		"Starting with 2.2-dev24, you can replace items in a inventory too."})
@Examples({"replace \"<item>\" in {textvar} with \"%item%\"",
		"replace every \"&\" with \"§\" in line 1",
		"# The following acts as a simple chat censor, but it will e.g. censor mass, hassle, assassin, etc. as well:",
		"on chat:",
		"	replace all \"fuck\", \"bitch\" and \"ass\" with \"****\" in the message",
		" ",
		"replace all stone and dirt in player's inventory and player's top inventory with diamond"})
@Since("2.0, 2.2-dev24 (replace in muliple strings and replace items in inventory)")
public class EffReplace extends Effect {
	static {
		Skript.registerEffect(EffReplace.class,
				"replace (all|every|) %strings% in %strings% with %string%",
				"replace (all|every|) %strings% with %string% in %strings%",
				"replace (all|every|) %itemstacks% in %inventories% with %itemstack%",
				"replace (all|every|) %itemstacks% with %itemstack% in %inventories%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> haystack, needles, replacement;
	private boolean replaceString = true;
	@SuppressWarnings({"null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		haystack =  exprs[1 + matchedPattern % 2];
		replaceString = matchedPattern < 2;
		if (replaceString && !ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
			Skript.error(haystack + " cannot be changed and can thus not have parts replaced.");
			return false;
		}
		needles = exprs[0];
		replacement = exprs[2 - matchedPattern % 2];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		Object[] haystack = this.haystack.getAll(e);
		Object[] needles = this.needles.getAll(e);
		Object replacement = this.replacement.getSingle(e);
		if (replacement == null || haystack == null || haystack.length == 0 || needles == null || needles.length == 0)
			return;
		if (replaceString) {
			for (int x = 0; x < haystack.length; x++)
				for (final Object n : needles) {
					assert n != null;
					haystack[x] = StringUtils.replace((String)haystack[x], (String)n, Matcher.quoteReplacement((String)replacement), SkriptConfig.caseSensitive.value());
				}
			this.haystack.change(e, haystack, ChangeMode.SET);
		} else {
			for (Inventory inv : (Inventory[])haystack)
				for (ItemStack item : (ItemStack[]) needles)
					for (Integer slot : inv.all(item).keySet()){
						inv.setItem(slot.intValue(), (ItemStack)replacement);
					}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "replace " + needles.toString(e, debug) + " in " + haystack.toString(e, debug) + " with " + replacement.toString(e, debug);
	}
	
}
