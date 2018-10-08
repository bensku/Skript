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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

public class EffActionBar extends Effect {

	static {
		if (Skript.methodExists(Player.class, "sendActionBar", String.class)) {
			Skript.registerEffect(EffActionBar.class, "[send] action bar %string% [to %players%]");
		}
	}

	@SuppressWarnings("null")
	private Expression<String> message;

	@SuppressWarnings("null")
	private Expression<Player> recipients;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		message = (Expression<String>) exprs[0];
		recipients = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (Player player : recipients.getArray(e)) {
			for (String string : message.getArray(e)) {
				player.sendActionBar(string);
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "send action bar " + message.toString(e, debug) + " to " + recipients.toString(e, debug);
	}

}
