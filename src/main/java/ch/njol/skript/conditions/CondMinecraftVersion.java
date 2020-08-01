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
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Version;
import ch.njol.util.Kleenean;

@Name("Running Minecraft")
@Description("Checks if current Minecraft version is given version or newer.")
@Examples({"running minecraft \"1.14\"", "if not running minecraft \"1.16.1\":"})
@Since("2.5")
public class CondMinecraftVersion extends Condition {
	
	static {
		Skript.registerCondition(CondMinecraftVersion.class,
			"[the] [server] [(is|1¦([is] not|isn't))] running [minecraft] [version] %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> version;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		version = (Expression<String>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String ver = version.getSingle(e);
		return ver != null ? Skript.isRunningMinecraft(new Version(ver)) ^ isNegated() : false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "is" + (isNegated() ? " not" : "")  + " running minecraft " + version.toString(e, debug);
	}
	
}
