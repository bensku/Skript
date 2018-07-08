/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptLogger;
import ch.njol.skript.SkriptLogger.SubLog;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class EffExec extends Effect {
	
	static {
//		Skript.registerEffect(EffExec.class, "exec[ute] %string%");
	}
	
	private Expression<String> input;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, boolean isDelayed, final ParseResult parser) {
		input = (Expression<String>) vars[0];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		final String s = input.getSingle(e);
		if (s == null)
			return;
		final SubLog log = SkriptLogger.startSubLog();
		final Effect eff = Effect.parse(s, "can't understand this effect: '" + s + "'");
		log.stop();
		if (eff != null) {
			eff.run(e);
		} else {
			final CommandSender sender = Skript.getEventValue(e, CommandSender.class, 0);
			log.printErrors(sender == null ? Bukkit.getConsoleSender() : sender, null);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "exec " + input.toString(e, debug);
	}
	
}
