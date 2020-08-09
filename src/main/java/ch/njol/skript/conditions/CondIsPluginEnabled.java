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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Plugin Enabled")
@Description("Check if a plugin is enabled on the server.")
@Examples({"if plugin \"Vault\" is enabled:", "if plugin \"WorldGuard\" is not enabled:", "if plugins \"Essentials\" and \"Vault\" are enabled:"})
@Since("INSERT VERSION")
public class CondIsPluginEnabled extends Condition {
	
	static {
		Skript.registerCondition(CondIsPluginEnabled.class, "plugin[s] %strings% (is|are) enabled",
				"plugin[s] %strings% (is|are)(n't| not) enabled");
	}
	
	@SuppressWarnings("null")
	private Expression<String> plugins;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		plugins = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return plugins.check(e, plugin -> {
			Plugin p = Bukkit.getPluginManager().getPlugin(plugin);
			return p != null && p.isEnabled();
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String plugin = plugins.isSingle() ? "plugin " : "plugins ";
		String plural = plugins.isSingle() ? " is" : " are";
		String neg = isNegated() ? " not" : "";
		return plugin + plugins.toString(e, debug) + plural + neg + "  enabled.";
	}
	
}
