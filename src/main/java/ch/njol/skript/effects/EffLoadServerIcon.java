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
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.eclipse.jdt.annotation.Nullable;

@Name("Load Server Icon")
@Description({"Loads server icons from the given files. You can get the loaded icon using the <a href='expressions.html#ExprLastLoadedServerIcon'>last loaded server icon</a> expression.",
		"Please note that the image must be 64x64 and the file path starts from the server folder."})
@Examples({"on load:",
		"	clear {server-icons::*}",
		"	loop 5 times:",
		"		load server icon from file \"icons/%loop-number%.png\"",
		"		add the last loaded server icon to {server-icons::*}",
		"",
		"on server list ping:",
		"	set the icon to a random server icon out of {server-icons::*}"})
@Since("INSERT VERSION")
public class EffLoadServerIcon extends AsyncEffect {

	static {
		Skript.registerEffect(EffLoadServerIcon.class, "(load|cache) [the] server icon (of|from) [image] [file] %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> path;

	@Nullable
	public static CachedServerIcon lastLoaded = null;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		path = (Expression<String>) exprs[0];
		return true;
	}

    @Override
    protected void execute(Event e) {
		File f = new File(path.getSingle(e));
		if (f.exists() && f.isFile()) {
			try {
				lastLoaded = Bukkit.loadServerIcon(f);
			} catch (NullPointerException npe) {
				Skript.warning("'" + path + "' is not an image!");
			} catch (IllegalArgumentException iae) {
				Skript.warning("The image to be set as the server icon must be 64x64 pixels! ('" + path + "')");
			} catch (Exception ex) {
				Skript.exception(ex);
			}
		} else {
			Skript.warning("'" + path.getSingle(e) + "' is not a valid file path!");
		}
    }

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "load server icon from file '" + path.toString(e, debug) + "'";
	}

}