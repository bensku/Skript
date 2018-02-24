/*
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
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("All Permissions")
@Description("Gets all permissions of players. Note that the modifications to resulting list do not actually change permissions.")
@Examples("")
@Since("2.2-dev33")
public class ExprPermissions extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprPermissions.class, String.class, ExpressionType.PROPERTY,
				"[(all [of]|[the])] permissions of %players%", "%players%'[s] permissions");
	}

	@SuppressWarnings("null")
	private Expression<Player> player;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		player = (Expression<Player>) exprs[0];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected String[] get(final Event e) {
		Player[] players = player.getAll(e);
		List<String> perms = new ArrayList<>();

		for (Player player : players) {
			for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
				perms.add(permissionAttachmentInfo.getPermission());
			}
		}

		return perms.toArray(new String[0]);
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
	public String toString(final @Nullable Event event, final boolean debug) {
		return "all permissions of " + player.toString();
	}
}
