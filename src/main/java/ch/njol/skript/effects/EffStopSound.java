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

import java.util.Locale;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Stop Sound")
@Description({"Stops a sound from playing to the specified players. Both Minecraft sound names and " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
		"are supported. Playing resource packs sounds are supported too.",
		"",
		"Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."})
@Examples({"stop sound \"block.chest.open\" from playing to player",
		"stop playing sounds \"ambient.underwater.loop\" and \"ambient.underwater.loop.additions\" to the player"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.10.2 or newer")
public class EffStopSound extends Effect {

	static {
		if (Skript.methodExists(Player.class, "stopSound", String.class))
			Skript.registerEffect(EffStopSound.class,
					"stop sound[s] %strings% [(from playing to|for) %players%]",
					"stop playing sound[s] %strings% [(to|for) %players%]");
	}

	private static final boolean SOUND_CATEGORIES_EXIST = Skript.classExists("org.bukkit.SoundCategory");

	@SuppressWarnings("null")
	private Expression<String> sounds;
	@SuppressWarnings("null")
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sounds = (Expression<String>) exprs[0];
		players = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (String sound : sounds.getArray(e)) {
			Sound soundEnum = null;
			try {
				soundEnum = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException ignored) {}
			
			if (soundEnum == null) {
				if (SOUND_CATEGORIES_EXIST) {
					for (Player p : players.getArray(e))
						p.stopSound(sound, SoundCategory.MASTER);
				} else {
					for (Player p : players.getArray(e))
						p.stopSound(sound);
				}
			} else {
				if (SOUND_CATEGORIES_EXIST) {
					for (Player p : players.getArray(e))
						p.stopSound(soundEnum, SoundCategory.MASTER);
				} else {
					for (Player p : players.getArray(e))
						p.stopSound(soundEnum);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e != null)
			return "stop sound " + sounds.toString(e, debug) + " from playing to " + players.toString(e, debug);
		return "stop sound";
	}

}
