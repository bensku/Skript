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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static ch.njol.skript.command.Commands.getCommands;
import static ch.njol.skript.command.Commands.getCommandMap;

@Name("All Server/Skript Commands")
@Description("Get all registered server/skript commands. Returns a list so it can be looped")
@Examples({"send \"%all skript commands%\" to player",
		"loop all server commands:"})
@Since("INSERT VERSION")
public class ExprAllCommands extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprAllCommands.class, String.class, ExpressionType.SIMPLE, "[all] [the] [(server|skript)] commands");
	}
	
	boolean isSkript = false;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		isSkript = parseResult.expr.toLowerCase().contains("skript");
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		if (isSkript) {
			return getCommands().keySet().toArray(new String[0]);
		}
		else {
			return getCommandMap().getCommands().stream()
				.map(c -> c.getLabel().replaceFirst(".+?:", "")) // Filter 'pluginName:' from command label
				.distinct().sorted().toArray(String[]::new);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return isSkript ? "all skript commands" : "all server commands";
	}
	
}
