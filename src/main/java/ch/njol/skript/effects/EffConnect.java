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

import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

public class EffConnect extends Effect {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String GET_SERVERS_CHANNEL = "GetServers";
	public static final String CONNECT_CHANNEL = "Connect";

	static {
		Skript.registerEffect(EffConnect.class,
				"connect %players% to [server] %string%",
				"send %players% to server %string%"
		);
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings("null")
	private Expression<String> server;

	@Override
	protected void execute(Event e) {
		String server = this.server.getSingle(e);
		if (server == null)
			return;

		Player[] players = this.players.getArray(e);
		if (players == null || players.length == 0)
			return;

		// the message channel is case sensitive so let's fix that
		Utils.sendPluginMessage(BUNGEE_CHANNEL, r -> GET_SERVERS_CHANNEL.equals(r.readUTF()), GET_SERVERS_CHANNEL)
			.thenAccept(response ->
				Stream.of(response.readUTF().split(", "))
						.filter(s -> s.equalsIgnoreCase(server))
						.findFirst()
						.ifPresent(s -> {
							for (Player player : players)
								Utils.sendPluginMessage(player, BUNGEE_CHANNEL, CONNECT_CHANNEL, s);
						})
			);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "connect " + players.toString(e, debug) + " to " + server.toString(e, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		server = (Expression<String>) exprs[1];
		return true;
	}
}
