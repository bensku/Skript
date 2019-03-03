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

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

@Name("Play Sound")
@Description({"Plays a sound at given location for everyone or just for given players, or plays a sound to specified players. " +
		"Both Minecraft sound names and " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
		"are supported. Playing resource packs sounds are supported too.",
		"",
		"Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."})
@Examples({"play sound \"block.note_block.pling\" at player # It is block.note.pling in 1.12.2",
		"play sound \"entity.experience_orb.pickup\" with volume 0.5 for the player"})
@Since("2.2-dev28")
public class EffPlaySound extends Effect {

	static {
		Skript.registerEffect(EffPlaySound.class,
				"play sound[s] %strings% [with volume %-number%] [(and|with) pitch %-number%] at %location% [for %-players%]",
				"play sound[s] %strings% [with volume %-number%] [(and|with) pitch %-number%] [(to|for) %players%]");
	}

	private static final boolean SOUND_CATEGORIES_EXIST = Skript.classExists("org.bukkit.SoundCategory");

	@SuppressWarnings("null")
	private Expression<String> sounds;
	@Nullable
	private Expression<Number> volume;
	@Nullable
	private Expression<Number> pitch;
	@SuppressWarnings("null")
	private Expression<Location> location;
	@SuppressWarnings("null")
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sounds = (Expression<String>) exprs[0];
		volume = (Expression<Number>) exprs[1];
		pitch = (Expression<Number>) exprs[2];
		if (matchedPattern == 0) {
			location = (Expression<Location>) exprs[3];
			players = (Expression<Player>) exprs[4];
		} else {
			players = (Expression<Player>) exprs[3];
		}
		return true;
	}

	@Override
	protected void execute(Event e) {
		float volume = 1, pitch = 1;
		if (this.volume != null) {
			Number volumeNumber = this.volume.getSingle(e);
			if (volumeNumber == null)
				return;
			volume = volumeNumber.floatValue();
		}
		if (this.pitch != null) {
			Number pitchNumber = this.pitch.getSingle(e);
			if (pitchNumber == null)
				return;
			pitch = pitchNumber.floatValue();
		}
		for (String sound : sounds.getArray(e)) {
			Sound soundEnum = null;
			try {
				soundEnum = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException ignored) { }
			
			if (location != null) {
				Location location = this.location.getSingle(e);
				if (location == null)
					return;
				if (players != null) {
					if (soundEnum == null) {
						if (SOUND_CATEGORIES_EXIST) {
							for (Player p : players.getArray(e))
								p.playSound(location, sound, SoundCategory.MASTER, volume, pitch);
						} else {
							for (Player p : players.getArray(e))
								p.playSound(location, sound, volume, pitch);
						}
					} else {
						if (SOUND_CATEGORIES_EXIST) {
							for (Player p : players.getArray(e))
								p.playSound(location, soundEnum, SoundCategory.MASTER, volume, pitch);
						} else {
							for (Player p : players.getArray(e))
								p.playSound(location, soundEnum, volume, pitch);
						}
					}
				} else {
					if (soundEnum == null)
						location.getWorld().playSound(location, sound, volume, pitch);
					else
						location.getWorld().playSound(location, soundEnum, volume, pitch);
				}
			} else {
				if (soundEnum == null) {
					for (Player p : players.getArray(e))
						p.playSound(p.getLocation(), sound, volume, pitch);
				} else {
					for (Player p : players.getArray(e))
						p.playSound(p.getLocation(), soundEnum, volume, pitch);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e != null) {
			if (location != null)
				return "play sound " + sounds.toString(e, debug) +
						(volume != null ? " with volume " + volume.toString(e, debug) : "") +
						(pitch != null ? " with pitch " + pitch.toString(e, debug) : "") +
						" at " + location.toString(e, debug) +
						(players != null ? " for " + players.toString(e, debug) : "");
			else
				return "play sound " + sounds.toString(e, debug) +
						(volume != null ? " with volume " + volume.toString(e, debug) : "") +
						(pitch != null ? " with pitch " + pitch.toString(e, debug) : "") +
						" for " + players.toString(e, debug);
		}
		return "play sound";
	}

}
