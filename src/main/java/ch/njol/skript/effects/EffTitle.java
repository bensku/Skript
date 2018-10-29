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
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Title")
@Description("Sends a title/subtitle to the given player(s) with optional stay/fadein/fadeout times for Minecraft versions 1.11 and above.")
@Examples({"send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds",
		"send title \"Hi %player%\" to player", "send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players",
		"send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player for 5 seconds with 1 second fadein with 1 second fadeout"})
@Since("INSERT VERSION")
public class EffTitle extends Effect {
	
	private final static boolean TIME_SUPPORTED = Skript.methodExists(Player.class,"sendTitle", String.class, String.class, int.class, int.class, int.class);
	
	static {
		if (TIME_SUPPORTED) {
			Skript.registerEffect(EffTitle.class, "send title %string% [with subtitle %-string%] [to %players%] [for %-timespan%] [with %-timespan% fade[(-| )]in] [(with|and with) %-timespan% fade[(-| )]out]");
		} else {
			Skript.registerEffect(EffTitle.class, "send title %string% [with subtitle %-string%] [to %players%]");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<String> title;
	@Nullable
	private Expression<String> subtitle;
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	@Nullable
	private Expression<Timespan> fadeIn, stay, fadeOut;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		title = (Expression<String>) exprs[0];
		subtitle = (Expression<String>) exprs[1];
		recipients = (Expression<Player>) exprs[2];
		if (TIME_SUPPORTED) {
			stay = (Expression<Timespan>) exprs[3];
			fadeIn = (Expression<Timespan>) exprs[4];
			fadeOut = (Expression<Timespan>) exprs[5];
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		String title = this.title.getSingle(e);
		String subtitle = this.subtitle != null ? this.subtitle.getSingle(e) : null;
		if (TIME_SUPPORTED) {
			int fadein = this.fadeIn != null ? (int) this.fadeIn.getSingle(e).getTicks_i() : 10;
			int stay = this.stay != null ? (int) this.stay.getSingle(e).getTicks_i() : 70;
			int fadeout = this.fadeOut != null ? (int) this.fadeOut.getSingle(e).getTicks_i() : 20;
			for (Player p : recipients.getArray(e))
				p.sendTitle(title, subtitle, fadein, stay, fadeout);
		} else {
			for (Player p : recipients.getArray(e))
				p.sendTitle(title, subtitle);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "send title " + title.toString(e, debug) + " to " + recipients.toString(e, debug) +
				(fadeIn != null ? " with fadein " + fadeIn.toString(e, debug) : "") +
				(stay != null ? " for " + stay.toString(e, debug) : "") +
				(fadeOut != null ? " with fadeout " + fadeOut.toString(e, debug) : "");
	}
	
}