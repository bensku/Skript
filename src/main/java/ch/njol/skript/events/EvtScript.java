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
package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
public class EvtScript extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Script Load/Unload", EvtScript.class, ScriptEvent.class, "[script] (load|init|enable)", "[script] (unload|stop|disable)")
				.description("Called directly after the trigger is loaded, or directly before the whole script is unloaded.")
				.examples("on load:",
						"	set {running::%script%} to true",
						"on unload:",
						"	set {running::%script%} to false")
				.since("2.0");
	}
	
	private boolean load;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		load = matchedPattern == 0;
		return true;
	}
	
	@Nullable
	private Trigger t;
	
	@Override
	public void register(final Trigger t) {
		this.t = t;
		if (load)
			t.execute(new ScriptEvent());
	}
	
	@Override
	public void unregister(final Trigger t) {
		assert t == this.t;
		if (!load)
			t.execute(new ScriptEvent());
		this.t = null;
	}
	
	@Override
	public void unregisterAll() {
		if (!load && t != null)
			t.execute(new ScriptEvent());
		t = null;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "script " + (load ? "" : "un") + "load";
	}
	
}
