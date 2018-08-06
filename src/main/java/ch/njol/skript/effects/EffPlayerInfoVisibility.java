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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.ScriptLoader;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Player Info Visibility")
@Description({"Sets whether all player related information is hidden in the server list.",
		"The Vanilla Minecraft client will display ??? (dark gray) instead of player counts and will not show the <a href='expressions.html#ExprHoverList'>hover hist</a when hiding player info.",
		"<a href='expressions.html#ExprVersionString'>The version string</a> can override the ???.",
		"Also the <a href='expressions.html#ExprOnlinePlayersCount'>Online Players Count</a> and <a href='expressions.html#ExprMaxPlayers'>Max Players</a> expressions will return -1 when hiding player info.",
		"",
		"Requires PaperSpigot 1.12.2+."})
@Examples({"hide player info",
		"hide player related information in the server list",
		"reveal all player related info"})
@Since("INSERT VERSION")
public class EffPlayerInfoVisibility extends Effect {

	static {
		Skript.registerEffect(EffPlayerInfoVisibility.class,
				"hide [all] player [related] info[rmation] [(in|on|from) [the] server list]",
				"(show|reveal) [all] player [related] info[rmation] [(in|to|on|from) [the] server list]");
	}

	private boolean shouldHide;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class);
		boolean isPaperEvent = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class);
		if (isServerPingEvent) {
			Skript.error("The player info visibility effect requires PaperSpigot 1.12.2+");
			return false;
		} else if (!isPaperEvent) {
			Skript.error("The player info visibility effect can't be used outside of a server list ping event" + (isServerPingEvent ? " and requires PaperSpigot 1.12.2+" : ""));
			return false;
		} else if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't change the player info visibility anymore after the server list ping event has already passed");
			return false;
		}
		shouldHide = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		((PaperServerListPingEvent) e).setHidePlayers(shouldHide);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (shouldHide ? "hide" : "show") + " player info in the server list";
	}

}