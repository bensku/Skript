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
import ch.njol.skript.bukkitutil.PlayerUtils;
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

@Name("Online Players Count")
@Description({"The count of online players. This can be changed in a <a href='events.html#server_list_ping'>server list ping</a> event only to show fake online players count.",
		"'real online players count' returns the real count of online players always and can't be changed.",
		"",
		"Fake online players count requires PaperSpigot 1.12.2+."})
@Examples({"on server list ping:",
		"	# This will make the max players count 5 if there are 4 players online.",
		"	set the fake max players count to (online players count + 1)"})
@Since("INSERT VERSION")
public class ExprOnlinePlayersCount extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprOnlinePlayersCount.class, Number.class, ExpressionType.PROPERTY,
				"[the] [(1¦(real|default)|(2¦fake|shown|displayed))] online players (count|amount|number|size)",
				"[the] [(1¦(real|default)|(2¦fake|shown|displayed))] (count|amount|number|size) of online players");
	}

	@SuppressWarnings("null")
	private Kleenean delay;

	private boolean isReal;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class);
		boolean isPaperEvent = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class);
		if (parseResult.mark == 2) {
			if (isServerPingEvent) {
				Skript.error("The 'shown' online players count expression requires PaperSpigot 1.12.2+");
				return false;
			} else if (!isPaperEvent) {
				Skript.error("The 'shown' online players count expression can't be used outside of a server list ping event" + (isServerPingEvent ? " and requires PaperSpigot 1.12.2+" : ""));
				return false;
			}
		}
		isReal = (parseResult.mark == 0 && !isPaperEvent) || parseResult.mark == 1;
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
		if (isReal)
			return CollectionUtils.array(PlayerUtils.getOnlinePlayers().size());
		else
			return CollectionUtils.array(((PaperServerListPingEvent) e).getNumPlayers());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (!isReal) {
			if (delay == Kleenean.TRUE) {
				Skript.error("Can't change the shown online players count anymore after the server list ping event has already passed");
				return null;
			}
			if (mode == Changer.ChangeMode.SET ||
					mode == Changer.ChangeMode.ADD ||
					mode == Changer.ChangeMode.REMOVE ||
					mode == Changer.ChangeMode.DELETE ||
					mode == Changer.ChangeMode.RESET)
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		PaperServerListPingEvent event = (PaperServerListPingEvent) e;
		switch (mode){
			case SET:
				event.setNumPlayers(((Number) delta[0]).intValue());
				break;
			case ADD:
				event.setNumPlayers(event.getNumPlayers() + ((Number) delta[0]).intValue());
				break;
			case REMOVE:
				event.setNumPlayers(event.getNumPlayers() - ((Number) delta[0]).intValue());
				break;
			case DELETE:
			case RESET:
				event.setNumPlayers(PlayerUtils.getOnlinePlayers().size());
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the count of " + (isReal ? "real max players" : "max players");
	}

}