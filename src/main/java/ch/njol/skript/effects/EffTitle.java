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
@Description("Sends a title/subtitle to the given player(s) with optional fadein/stay/fadeout times")
@Examples({"send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds",
		"send title \"Hi %player%\" to player", "send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players",
		"send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player with fadein 1 second for 5 seconds with fadeout 1 second"})
@Since("INSERT VERSION")
public class EffTitle extends Effect {
	
	static {
		Skript.registerEffect(EffTitle.class, "send title %string% [with subtitle %-string%] to %players% [with fade[(-| )]in %-timespan%] [for %-timespan%] [with fade[(-| )]out %-timespan%]");
	}
	
	@SuppressWarnings("null")
	private Expression<String> title, subtitle;
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	@SuppressWarnings("null")
	private Expression<Timespan> fadein, stay, fadeout;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		title = (Expression<String>) exprs[0];
		subtitle = (Expression<String>) exprs[1];
		recipients = (Expression<Player>) exprs[2];
		fadein = (Expression<Timespan>) exprs[3];
		stay = (Expression<Timespan>) exprs[4];
		fadeout = (Expression<Timespan>) exprs[5];
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		String msg1 = title.getSingle(e);
		String msg2 = subtitle != null ? subtitle.getSingle(e) : null;
		int fadein = this.fadein != null ? (int) this.fadein.getSingle(e).getTicks_i() : 10;
		int stay = this.stay != null ? (int) this.stay.getSingle(e).getTicks_i() : 70;
		int fadeout = this.fadeout != null ? (int) this.fadeout.getSingle(e).getTicks_i() : 20;
		
		for (Player player : recipients.getArray(e)) {
			player.sendTitle(msg1, msg2, fadein, stay, fadeout);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "send title " + title.toString(e, debug) + " to " + recipients.toString(e, debug) +
				(fadein != null ? " with fadein " + fadein.toString(e, debug) : "") +
				(stay != null ? " for " + stay.toString(e, debug) : "") +
				(fadeout != null ? " with fadeout " + fadeout.toString(e, debug) : "");
	}
	
}