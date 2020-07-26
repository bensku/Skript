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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Date;

@Name("Last Login Time")
@Description("When a player last logged in")
@Examples({"command /onlinefor:",
	"\ttrigger:",
	"\t\tsend \"You have been online for %difference between player's last login date and now%\""})
@Since("INSERT VERSION")
public class ExprLastLoginTime extends SimplePropertyExpression<Player, Date> {
	
	static {
		register(ExprLastLoginTime.class, Date.class, "last login (date|time)", "players");
	}
	
	@Nullable
	@Override
	public Date convert(Player player) {
		return new Date(player.getLastLogin());
	}
	
	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last login date";
	}
	
}