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

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
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
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hover List")
@Description({"The list when you hover on the player counts of the server in the server list.",
		"This can be changed using texts or players in a <a href='events.html#server_list_ping'>server list ping</a> event only. " +
		"Adding players to the list means adding name of the players.",
		"And note that, for example if there are 5 online players (includes <a href='#ExprOnlinePlayersCount'>fake online count</a>) " +
		"in the server and the hover list is set to 3 values, Minecraft will show \"... and 2 more ...\" at end of the list.",
		"",
		"Requires PaperSpigot 1.12.2+."})
@Examples({"on server list ping:",
		"	clear the hover list",
		"	add \"<light green>Welcome to the <orange>Minecraft <light green>server!\" to the hover list",
		"	add \"\" to the hover list # A blank line",
		"	add \"<light red>There are <orange>%online players count% <light red>online players!\""})
@Since("INSERT VERSION")
public class ExprHoverList extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprHoverList.class, String.class, ExpressionType.SIMPLE,
				"[the] [custom] [(player|server)] (hover|sample) ([message] list|message)",
				"[the] [custom] player [(hover|sample)] list");
	}

	@SuppressWarnings("null")
	private Kleenean delay;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class);
		boolean isPaperEvent = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class);
		if (isServerPingEvent) {
			Skript.error("The hover list expression requires PaperSpigot 1.12.2+");
			return false;
		} else if (!isPaperEvent) {
			Skript.error("The hover list expression can't be used outside of a server list ping event" + (isServerPingEvent ? " and requires PaperSpigot 1.12.2+" : ""));
			return false;
		}
		delay = isDelayed;
		return true;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		return ((PaperServerListPingEvent) e).getPlayerSample().stream()
				.map(PlayerProfile::getName)
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (delay == Kleenean.TRUE) {
			Skript.error("Can't change the hover list anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == Changer.ChangeMode.SET ||
				mode == Changer.ChangeMode.ADD ||
				mode == Changer.ChangeMode.REMOVE ||
				mode == Changer.ChangeMode.DELETE ||
				mode == Changer.ChangeMode.RESET)
			return CollectionUtils.array(String[].class, Player[].class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		List<PlayerProfile> values = new ArrayList<>();
		if (mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.RESET) {
			for (Object o : delta) {
				if (o instanceof Player) {
					Player player = ((Player) o);
					values.add(Bukkit.createProfile(player.getUniqueId(), player.getName()));
				} else {
					values.add(Bukkit.createProfile(UUID.randomUUID(), (String) o));
				}
			}
		}

		List<PlayerProfile> sample = ((PaperServerListPingEvent) e).getPlayerSample();
		switch (mode){
			case SET:
				sample.clear();
				sample.addAll(values);
				break;
			case ADD:
				sample.addAll(values);
				break;
			case REMOVE:
				sample.removeAll(values);
				break;
			case DELETE:
			case RESET:
				sample.clear();
				break;
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the hover list";
	}

}