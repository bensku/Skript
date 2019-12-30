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
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Name("All Scripts")
@Description("Returns all of the scripts, or just the enabled or disabled ones.")
@Examples({"command /scripts:",
		"\ttrigger:",
		"\t\tsend \"All Scripts: %scripts%\" to player",
		"\t\tsend \"Loaded Scripts: %enabled scripts%\" to player",
		"\t\tsend \"Unloaded Scripts: %disabled scripts%\" to player"})
@Since("INSERT VERSION")
public class ExprScripts extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprScripts.class, String.class, ExpressionType.SIMPLE,
				"[all [of the]] scripts [(1¦without [subdirectory] paths)]",
				"[all [of the]] (enabled|loaded) scripts [(1¦without [subdirectory] paths)]",
				"[all [of the]] (disabled|unloaded) scripts [(1¦without [subdirectory] paths)]");
	}

	private boolean includeEnabled;
	private boolean includeDisabled;
	private boolean noPaths;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		includeEnabled = matchedPattern <= 1;
		includeDisabled = matchedPattern != 1;
		noPaths = parseResult.mark == 1;
		return true;
	}

	@Override
	protected String[] get(Event event) {
		List<File> scripts = new ArrayList<>();
		if (includeEnabled)
			scripts.addAll(ScriptLoader.getLoadedFiles());
		if (includeDisabled)
			scripts.addAll(ScriptLoader.getDisabledFiles());
		return formatFiles(scripts).toArray(new String[0]);
	}

	private List<String> formatFiles(List<File> files) {
		List<String> scripts = new ArrayList<>();
		for (File f : files) {
			if (noPaths) {
				scripts.add(f.getName());
			} else {
				scripts.add(f.getPath().split("scripts/")[1]);
			}
		}
		return scripts;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "scripts";
	}
}
