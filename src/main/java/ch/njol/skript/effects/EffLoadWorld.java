/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
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
import ch.njol.util.Kleenean;

@Name("Load/Unload Worlds")
@Description({"Loads or unloads a world.",
	"When loading a world, if this world does not exist, it will create a world with that name.",
	"Note that it takes in the world's name, not the world itself, and you cannot unload your main world."})
@Examples({"on script load:",
	"\tunload world \"world_nether\" and don't save chunks"})
@Since("INSERT VERSION")
public class EffLoadWorld extends Effect {
	
	static {
		Skript.registerEffect(EffLoadWorld.class,
			"load [(a[n]|the)] (1¦(default|normal)|2¦nether|3¦end)] world[s] %strings%",
			"unload [the] world[s] %strings% [(1¦without saving chunks)]");
	}
	
	private boolean load;
	private boolean save;
	@SuppressWarnings("null")
	private Environment environment;
	@SuppressWarnings("null")
	private Expression<String> worldNames;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.load = matchedPattern == 0;
		if (load) {
			if (parseResult.mark == 2) {
				environment = Environment.NETHER;
			} else if (parseResult.mark == 3) {
				environment = Environment.THE_END;
			} else {
				environment = Environment.NORMAL;
			}
		} else {
			this.save = parseResult.mark != 1;
		}
		this.worldNames = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected void execute(Event e) {
		for (String world : this.worldNames.getArray(e)) {
			if (load) {
				if (Bukkit.getWorld(world) == null) {
					Bukkit.createWorld(new WorldCreator(world).environment(environment));
				}
			} else {
				if (Bukkit.getWorld(world) != null) {
					World eventWorld = Bukkit.getWorld(world);
					World fallbackWorld = Bukkit.getWorlds().get(0);
					if (eventWorld == fallbackWorld) return;
					if (eventWorld.getPlayers() != null) {
						for (Player p : eventWorld.getPlayers()) {
							p.teleport(fallbackWorld.getSpawnLocation());
						}
					}
					Bukkit.unloadWorld(eventWorld, save);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (load ? "" : "un") + "load worlds " + worldNames.toString(e, debug);
	}
}
