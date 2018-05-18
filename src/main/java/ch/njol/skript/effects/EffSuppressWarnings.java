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

import java.io.File;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.ScriptOptions;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

@Name("Locally Suppress Warning")
@Description("Suppresses target warnings from the current script.")
@Examples({"locally suppress conflict warnings",
			"suppress variable save warnings"})
@Since("INSERT VERSION")
public class EffSuppressWarnings extends Effect {

	static {
		Skript.registerEffect(EffSuppressWarnings.class, "[local[ly]] suppress (1¦conflict|2¦variable save|3¦[missing] conjunction[s]|4¦starting [with] expression[s]) warning[s]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		File scriptFile = ScriptLoader.currentScript.getFile();
		switch (parseResult.mark) {
			case 1: { //Possible variable conflicts
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "conflict");
				break;
			}
			case 2: { //Variables cannot be saved
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "instance var");
				break;
			}
			case 3: { //Missing "and" or "or"
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "conjunction");
				break;
			}
			case 4: { //Variable starts with expression
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "start expression");
				break;
			}
			default: { //How did this happen?
				Skript.error("Skript returned an invalid parse mark, this should never happen. Please report this!");
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString(Event e, boolean debug) {
		return "Locally Suppress Warning";
	}

	@Override
	protected void execute(Event e) {

	}

}
