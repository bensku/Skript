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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.ScriptLoader;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("IP")
@Description({"The IP address of a player, or the connected player in a <a href='events.html#connect'>connect</a>> event, " +
		"or the pinger in a <a href='events.html#server_list_ping'>server list ping</a> event."})
@Examples({"IP-ban the player # is equal to the next line",
		"ban the IP-address of the player",
		"broadcast \"Banned the IP %IP of player%\"",
		"",
		"on connect:",
		"	broadcast \"%player%: %ip%\"",
		"",
		"on server list ping:",
		"	send \"%player%: %ip adress of the pinger%\" to the console"})
@Since("1.4, 2.2-dev26 (when used in connect event)")
public class ExprIP extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.PROPERTY,
				"IP[s][( |-)address[es]] of %players%",
				"%players%'[s] IP[s][( |-)address[es]]",
				"IP[( |-)address]",
				"IP[( |-)address] of [the] connected player",
				"connected [player's] IP[( |-)address]",
				"IP[( |-)address] of [the] pinger",
				"pinger's IP[( |-)address]");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	private boolean isConnectEvent, isServerPingEvent;
	private int pattern;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		isConnectEvent = ScriptLoader.isCurrentEvent(PlayerLoginEvent.class);
		isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class) ||
				(Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class));
		if (pattern < 2) {
			players = (Expression<Player>) exprs[0];
		} else if (!isConnectEvent && !isServerPingEvent) {
			Skript.error("The IP adress of pinger/connected player expression can't be used outside of a connect or server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(final Event e) {
		// If no player is specified
		if (pattern > 1) {
			InetAddress addr;
			if (isConnectEvent)
				// Return IP adress of the connected player in connect event
				addr = ((PlayerLoginEvent) e).getAddress();
			else
				// Return IP adress of the pinger in server list ping event
				addr = ((ServerListPingEvent) e).getAddress();
			return CollectionUtils.array(addr == null ? "unknown" : addr.getHostAddress());
		}

		Player[] ps = players.getAll(e);
		String[] ips = new String[ps.length];
		for (int i = 0; i < ips.length; i++) {
			Player p = ps[i];
			InetAddress addr;
			// Connect event: player has no ip yet, but event has it
			// It is a "feature" of Spigot, apparently
			if (isConnectEvent && ((PlayerLoginEvent) e).getPlayer().equals(p)) {
				addr = ((PlayerLoginEvent) e).getAddress();
			} else {
				InetSocketAddress socketAddr = p.getAddress();
				if (socketAddr == null) {
					ips[i] = "unknown";
					continue;
				}
				addr = socketAddr.getAddress();
			}

			// Check if address is not available, just in case...
			if (addr == null) {
				ips[i] = "unknown";
				continue;
			}

			// Finally, place ip here to array...
			ips[i] = addr.getHostAddress();
		}

		return ips;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e != null)
			return "ip of " + (isServerPingEvent ? "the pinger" : (isConnectEvent ? "the connected player" : players.toString(e, debug)));
		else
			return "ip";
	}

}
