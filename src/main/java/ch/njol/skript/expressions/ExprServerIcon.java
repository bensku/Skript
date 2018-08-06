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
import org.bukkit.util.CachedServerIcon;
import org.eclipse.jdt.annotation.Nullable;

@Name("Server Icon")
@Description({"Icon of the server in the server list. The first pattern doesn't return anything and will not work outside of a <a href='events.html#server_list_ping'>server list ping</a> event. " +
		"But can be set to an icon that loaded using the <a href='effects.html#EffLoadServerIcon'>load server icon</a> effect, or can be reset to the default icon.",
		"The second pattern returns the default server icon (server-icon.png) and cannot be changed."})
@Examples({"on script load:",
		"	set {server-icons::default} to the default server icon"})
@Since("INSERT VERSION")
public class ExprServerIcon extends SimpleExpression<CachedServerIcon> {

	static {
		Skript.registerExpression(ExprServerIcon.class, CachedServerIcon.class, ExpressionType.PROPERTY,
				"[the] [(shown|sent)] [server] icon",
				"[the] default [server] icon");
	}

	private boolean isServerPingEvent;
	private int pattern;

	@SuppressWarnings("null")
	private Kleenean delay;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		pattern = matchedPattern;
		boolean isPaperEvent = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class);
		isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class) || isPaperEvent;
		if (!isServerPingEvent && pattern == 0) {
			Skript.error("The 'shown' server icon expression can't be used outside of a server list ping event");
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
	public CachedServerIcon[] get(Event e) {
		if (isServerPingEvent && pattern == 0)
			return null;
		CachedServerIcon icon = Bukkit.getServerIcon();
		// Returns null if server-icon.png doesn't exist
		return CollectionUtils.array(icon.getData() == null ? null : icon);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (isServerPingEvent && pattern == 0) {
			if (delay == Kleenean.TRUE) {
				Skript.error("Can't change the server icon anymore after the server list ping event has already passed");
				return null;
			}
			if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET)
				return CollectionUtils.array(CachedServerIcon.class);
		}
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		switch (mode) {
			case SET:
				((ServerListPingEvent) e).setServerIcon((CachedServerIcon) delta[0]);
				break;
			case RESET:
				((ServerListPingEvent) e).setServerIcon(Bukkit.getServerIcon());
				break;
		}
	}

	@Override
	public Class<? extends CachedServerIcon> getReturnType() {
		return CachedServerIcon.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + ((!isServerPingEvent || pattern == 1) ? "default server icon" : "shown server icon");
	}

}