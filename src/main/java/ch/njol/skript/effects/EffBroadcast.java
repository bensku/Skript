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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Broadcast")
@Description({"Broadcasts a message to the server. Only formatting options supported by console",
		"(i.e. colors) are supported. If you need to use advanced chat formatting, send the",
		"message to all players instead of broadcasting it."})
@Examples({"broadcast \"Welcome %player% to the server!\"",
		"broadcast \"Woah! It's a message!\""})
@Since("1.0, INSERT VERSION (send multiple times)")
public class EffBroadcast extends Effect {

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %strings% [(to|in) %-worlds%] [%-number% times]");
	}

	@SuppressWarnings("null")
	private Expression<String> messages;

	@Nullable
	private Expression<World> worlds;

	@Nullable
	private Expression<Number> repeat;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		worlds = (Expression<World>) exprs[1];
		repeat = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	public void execute(final Event e) {
		int times = 1;
		if (repeat != null) {
            Number n = repeat.getSingle(e);
            if (n != null) {
            	times = n.intValue();
            }
		}
		for (final String m : messages.getArray(e)) {
			final Expression<World> worlds = this.worlds;
			if (worlds == null) {
				// not Bukkit.broadcastMessage to ignore permissions
				for (final Player p : PlayerUtils.getOnlinePlayers()) {
					for (int i = 1; i <= times; i++) {
						p.sendMessage(m);
					}
				}
				for (int i = 1; i <= times; i++) {
					Bukkit.getConsoleSender().sendMessage(m);
				}
			} else {
				for (final World w : worlds.getArray(e)) {
					for (final Player p : w.getPlayers()) {
						for (int i = 1; i <= times; i++) {
							p.sendMessage(m);
						}
					}
				}
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<World> worlds = this.worlds;
		final Expression<Number> repeat = this.repeat;
		return "broadcast " + messages.toString(e, debug) + (worlds == null ? "" : " to " + worlds.toString(e, debug)) + (repeat == null ? "" : " " + repeat.toString(e, debug)) + " times";
	}
}
