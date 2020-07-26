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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Flight Mode")
@Description("Whether the player(s) are allowed to fly. Use <a href=effects.html#EffMakeFly>Make Fly</a> effect to force player(s) to fly.")
@Examples({"set flight mode of player to true", "send \"%flying state of all players%\""})
@Since("2.2-dev34, INSERT VERSION (toggle support)")
public class ExprFlightMode extends SimplePropertyExpression<Player, Boolean> {
	
	static {
		register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
	}
	
	@Override
	public Boolean convert(final Player player) {
		return player.getAllowFlight();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.TOGGLE) {
			return CollectionUtils.array(Boolean.class);
		}
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.TOGGLE) {
			for (Player player : getExpr().getArray(event))
				player.setAllowFlight(!player.getAllowFlight());
			return;
		}
		boolean state = mode != ChangeMode.RESET && delta != null && (boolean) delta[0];
		for (Player player : getExpr().getArray(event)) {
			player.setAllowFlight(state);
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "flight mode";
	}
	
	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}
}
