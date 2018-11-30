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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Tablist Header")
@Description("Add multiple line headers to your tablist")
@Examples({"on join:", "\tset tablist header \"This is my header\" for player",
		"every 5 seconds:", "\tset tab header \"Welcome to our server\" with footer \"\" for all players",
		"every minute:", "\tloop all players:", "\t\tset tab header \"&aMyServer\" and \"Thanks for stopping in\" for loop-player"})
@RequiredPlugins("Minecraft 1.13+")
@Since("{INSERT VERSION}")
public class EffTabHead extends Effect {
	
	static {
		if(Skript.isRunningMinecraft(1, 13))
			Skript.registerEffect(EffTabHead.class, "set tab[list] header[ to] %strings% for %players%");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> player;
	@SuppressWarnings("null")
	private Expression<String> header;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		header = (Expression<String>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		String header = String.join("\n", this.header.getArray(e));
		for (Player p : player.getArray(e))
			p.setPlayerListHeader(header);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return ("set tablist header to " + header.toString(e, debug) + " for " + player.toString(e, debug));
	}
	
}