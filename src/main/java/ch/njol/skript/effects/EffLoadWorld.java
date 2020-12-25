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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
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
import ch.njol.util.Kleenean;

@Name("Load/Unload Worlds")
@Description({"Loads or unloads a world.",
	"When loading a world, if this world does not exist, it will create a world with that name.",
	"Note that it takes in the worlds name, not the world itself."})
@Examples({"on script load:",
	"\tunload world \"world_nether\""})
@Since("INSERT VERSION")
public class EffLoadWorld extends Effect {
	
	static {
		Skript.registerEffect(EffLoadWorld.class, "(0¦load|1¦unload) world %strings%");
	}
	
	private boolean load;
	@SuppressWarnings("null")
	private Expression<String> worlds;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.load = parseResult.mark == 0;
		this.worlds = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		for (String world : this.worlds.getArray(e)) {
			if (load) {
				Bukkit.getServer().createWorld(new WorldCreator(world));
			} else {
				Bukkit.getServer().unloadWorld(world, true);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return String.format("%s world %s", load ? "load" : "unload", this.worlds.toString(e, debug));
	}
}
