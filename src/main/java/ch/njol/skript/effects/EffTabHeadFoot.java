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

@Name("Tablist Header/Footer")
@Description("Add multiple line headers and footers to your tablist")
@Examples({"on join:", "\tset tablist header \"This is my header\" with footer \"This is my footer\" for player",
		"every 5 seconds:", "\tset tab header \"Welcome to our server\" with footer \"\" for all players",
		"every minute:", "\tloop all players:", "\t\tset tab header \"&aMyServer\" and \"Thanks for stopping in\" with footer " +
		"\"Balance: %balance of loop-player%\" and \"World: %world of loop-player\" for loop-player"})
@RequiredPlugins("Minecraft 1.13+")
@Since("{INSERT VERSION}")
public class EffTabHeadFoot extends Effect {
	
	static {
		if(Skript.isRunningMinecraft(1, 13))
			Skript.registerEffect(EffTabHeadFoot.class, "set tab[list] header[ to] %strings% (with|and) footer[ to] %strings% for %players%");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> player;
	@SuppressWarnings("null")
	private Expression<String> header;
	@SuppressWarnings("null")
	private Expression<String> footer;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		header = (Expression<String>) exprs[0];
		footer = (Expression<String>) exprs[1];
		player = (Expression<Player>) exprs[2];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		String header = String.join("\n", this.header.getArray(e));
		String footer = String.join("\n", this.footer.getArray(e));
		for (Player p : player.getArray(e))
			p.setPlayerListHeaderFooter(header, footer);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return ("set tablist header to " + header.toString(e, debug) + " and footer to " + footer.toString(e, debug) +
				" for " + player.toString(e, debug));
	}
	
}