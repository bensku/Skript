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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("View Distance of Client")
@Description("The view distance of the client. (Can not be changed)")
@Examples({"set {_clientView} to the client view distance of player", "set view distance of player to client view distance of player"})
@RequiredPlugins("1.13.2+")
@Since("INSERT VERSION")
public class ExprClientViewDistance extends SimpleExpression<Number> {
	
	static {
		if (Skript.methodExists(Player.class, "getClientViewDistance")) {
			Skript.registerExpression(ExprClientViewDistance.class, Number.class, ExpressionType.PROPERTY,
				"[the] client view distance of %player%");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<Player> player;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		player = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event e) {
		final Player player = this.player.getSingle(e);
		if (player == null)
			return new Number[0];
		return new Number[] {player.getClientViewDistance()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "client view distance of " + player.toString(e, d);
	}
	
}
