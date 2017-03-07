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
package ch.njol.skript.conditions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.Predicate;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Is Banned")
@Description("Checks whether a player or IP is banned.")
@Examples({"player is banned",
		"victim is not IP-banned",
		"\"127.0.0.1\" is banned"})
@Since("1.4")
public class CondIsBanned extends Condition {
	
	static {
		Skript.registerCondition(CondIsBanned.class,
				"%offlineplayers/strings% (is|are) banned", "%players/strings% (is|are) IP(-| |)banned",
				"%offlineplayers/strings% (isn't|is not|aren't|are not) banned", "%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned");
	}
	
	@SuppressWarnings("null")
	private Expression<?> players;
	
	boolean ipBanned;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = exprs[0];
		setNegated(matchedPattern >= 2);
		ipBanned = matchedPattern % 2 != 0;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return players.check(e, new Checker<Object>() {
			@Override
			public boolean check(final Object o) {
				if (o instanceof Player) {
					if (ipBanned) {
						return Bukkit.getIPBans().contains(((Player) o).getAddress().getAddress().getHostAddress());
					} else {
						return ((Player) o).isBanned();
					}
				} else if (o instanceof OfflinePlayer) {
					return ((OfflinePlayer) o).isBanned();
				} else if (o instanceof String) {
					return Bukkit.getIPBans().contains(o) || !ipBanned && 
						CollectionUtils.contains(Bukkit.getBannedPlayers().toArray(new OfflinePlayer[Bukkit.getBannedPlayers().size()]), new Predicate<OfflinePlayer>() {
							@Override
							public boolean test(final @Nullable OfflinePlayer t) {
								return t != null && ((String) o).equals(t.getName());
							}
					});
				}
				assert false;
				return false;
			}
		}, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return players.toString(e, debug) + (players.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + (ipBanned ? "IP-" : "") + "banned";
	}
	
}
