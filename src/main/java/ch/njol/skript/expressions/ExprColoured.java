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

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Coloured / Uncoloured")
@Description("Parses &lt;colour&gt;s (including chat styles) in a message or removes any colours & chat styles from the message.")
@Examples({"on chat:",
		"	set message to coloured message",
		"command /fade <player>:",
		"	trigger:",
		"		set display name of the player-argument to uncoloured display name of the player-argument"})
@Since("2.0")
public class ExprColoured extends PropertyExpression<String, String> {
	static {
		Skript.registerExpression(ExprColoured.class, String.class, ExpressionType.COMBINED,
				"(colo[u]r-|colo[u]red )%strings%", "(un|non)[-](colo[u]r-|colo[u]red )%strings%");
	}
	
	boolean color;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends String>) exprs[0]);
		color = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected String[] get(final Event e, final String[] source) {
		return get(source, new Converter<String, String>() {
			@Override
			public String convert(final String s) {
				return color ? Utils.replaceChatStyles(s) : "" + ChatColor.stripColor(s);
			}
		});
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (color ? "" : "un") + "coloured " + getExpr().toString(e, debug);
	}
	
}
