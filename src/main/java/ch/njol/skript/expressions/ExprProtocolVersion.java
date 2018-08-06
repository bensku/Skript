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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.ScriptLoader;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Protocol Version")
@Description({"The protocol version that will be sent as the protocol version of the server in a server list ping event. " +
		"For more information and list of protocol versions <a href='http://wiki.vg/Protocol_version_numbers'>visit wiki.vg</a>.",
		"If this protocol version doesn't match with the protocol version of the client, the client will see the <a href='#ExprVersionString'>version string</a>.",
		"But please note that, this expression has no visual effect over the version string. " +
		"For example if the server uses PaperSpigot 1.12.2, and you make the protocol version 107 (1.9), the version string will not be \"Paper 1.9\", it will still be \"Paper 1.12.2\". " +
		"But then you can customize the <a href='#ExprVersionString'>version string</a> as you wish.",
		"Also if protocol version of the player is higher than protocol version of the server, it will say \"Server out of date!\", and if vice-versa \"Client out of date!\" when you hover on the ping bars.",
		"",
		"This can be set in a <a href='events.html#server_list_ping'>server list ping</a> event only (increase and decrease effects cannot be used because doesn't make sense).",
		"",
		"Requires PaperSpigot 1.12.2+."})
@Examples({"on server list ping:",
		"	set the protocol version to 0 # 13w41a (1.7), so it will show the version string always"})
@Since("INSERT VERSION")
public class ExprProtocolVersion extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprProtocolVersion.class, Number.class, ExpressionType.SIMPLE, "[the] [(sent|required|fake)] protocol version [number]");
	}

	@SuppressWarnings("null")
	private Kleenean delay;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class);
		boolean isPaperEvent = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class);
		if (isServerPingEvent) {
			Skript.error("The protocol version expression requires PaperSpigot 1.12.2+");
			return false;
		} else if (!isPaperEvent) {
			Skript.error("The protocol version expression can't be used outside of a server list ping event" + (isServerPingEvent ? " and requires PaperSpigot 1.12.2+" : ""));
			return false;
		}
		delay = isDelayed;
		return true;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	@Nullable
	public Number[] get(Event e) {
		return CollectionUtils.array(((PaperServerListPingEvent) e).getProtocolVersion());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (delay == Kleenean.TRUE) {
			Skript.error("Can't change the protocol version anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		((PaperServerListPingEvent) e).setProtocolVersion(((Number) delta[0]).intValue());
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the protocol version";
	}

}