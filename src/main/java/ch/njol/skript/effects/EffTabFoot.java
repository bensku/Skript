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

@Name("Tablist Footer")
@Description("Add multiple line footers to your tablist")
@Examples({"on join:", "\tset tablist footer \"This is my footer\" for player",
		"every minute:", "\tset tab footer \"Dont forget to VOTE today\" for all players",
		"every 30 seconds:", "\tloop all players:", "\t\tset tab footer " +
		"\"Balance: %balance of loop-player%\" and \"World: %world of loop-player\" for loop-player"})
@RequiredPlugins("Minecraft 1.13+")
@Since("{INSERT VERSION}")
public class EffTabFoot extends Effect {
	
	static {
		if(Skript.isRunningMinecraft(1, 13))
			Skript.registerEffect(EffTabFoot.class, "set tab[list] footer[ to] %strings% for %players%");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> player;
	@SuppressWarnings("null")
	private Expression<String> footer;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		footer = (Expression<String>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		String footer = String.join("\n", this.footer.getArray(e));
		for (Player p : player.getArray(e))
			p.setPlayerListFooter(footer);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return ("set tablist footer to " + footer.toString(e, debug) +  " for " + player.toString(e, debug));
	}
	
}