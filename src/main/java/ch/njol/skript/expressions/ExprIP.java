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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("IP")
@Description("The IP address of a player.")
@Examples({"IP-ban the player # is equal to the next line",
		"ban the IP-address of the player",
		"broadcast \"Banned the IP %IP of player%\""})
@Since("1.4, 2.2-dev26 (when used in connect event)")
public class ExprIP extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.PROPERTY, "IP[s][( |-)address[es]] of %players%",
				"%players%'[s] IP[s][( |-)address[es]]");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> players;
	private boolean connectEvent;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		connectEvent = ScriptLoader.isCurrentEvent(PlayerLoginEvent.class);
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		Player[] ps = players.getAll(e);
		String[] ips = new String[ps.length];
		for (int i = 0; i < ips.length; i++) {
			Player p = ps[i];
			InetAddress addr;
			// Connect event: player has no ip yet, but event has it
			// It is a "feature" of Spigot, apparently
			if (connectEvent && ((PlayerLoginEvent) e).getPlayer().equals(p)) {
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
			return "ip of " + players.toString(e, debug);
		else
			return "ip";
	}
	
}
