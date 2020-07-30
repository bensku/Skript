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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;


@Name("Command Info")
@Description("Get information about a command.")
@Examples({"main name of command \"skript\"",
	"description of command \"help\"",
	"label of command \"pl\"",
	"usage of command \"help\"",
	"aliases of command \"bukkit:help\"",
	"permission of command \"/op\"",
	"command \"op\"'s permission message",
	"command \"sk\"'s plugin owner"})
@Since("INSERT VERSION")
public class ExprCommandInfo extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprCommandInfo.class, String.class, ExpressionType.SIMPLE,
			"[the] main [command] [name] of command %string%", "command %string%'s main [command] [name]",
			"[the] description of command %string%", "command %string%'s description",
			"[the] label of command %string%", "command %string%'s label",
			"[the] usage of command %string%", "command %string%'s usage",
			"[the] aliases of command %string%", "command %string%'s aliases",
			"[the] permission of command %string%", "command %string%'s permission",
			"[the] permission message of command %string%", "command %string%'s permission message",
			"[the] plugin [owner] of command %string%", "command %string%'s plugin [owner]");
	}
	
	@SuppressWarnings("null")
	InfoType type;
	@SuppressWarnings("null")
	Expression<String> commandName;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		commandName = (Expression<String>) exprs[0];
		type = InfoType.values()[Math.floorDiv(matchedPattern, 2)];
		return true;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event e) {
		String commandName = this.commandName.getSingle(e);
		if (commandName == null)
			return null;
		commandName = commandName.toLowerCase().split(" ")[0];
		if (commandName.startsWith("/"))
			commandName = commandName.substring(1);
		Command command = Commands.getCommandMap().getCommand(commandName);
		if (command == null)
			return null;
		switch (type) {
			case NAME:
				return new String[]{command.getName()};
			case DESCRIPTION:
				return new String[]{command.getDescription()};
			case LABEL:
				return new String[]{command.getLabel()};
			case USAGE:
				return new String[]{command.getUsage()};
			case ALIASES:
				return command.getAliases().toArray(new String[0]);
			case PERMISSION:
				return new String[]{command.getPermission()};
			case PERMISSION_MESSAGE:
				return new String[]{command.getPermissionMessage()};
			case PLUGIN:
				if (command instanceof PluginCommand) {
					return new String[]{((PluginCommand) command).getPlugin().getName()};
				} else if (command instanceof BukkitCommand) {
					return new String[]{"Bukkit"};
				} else if (command.getClass().getPackage().getName().startsWith("org.spigot")) {
					return new String[]{"Spigot"};
				} else if (command.getClass().getPackage().getName().startsWith("com.destroystokyo.paper")) {
					return new String[]{"Paper"};
				}
		}
		return new String[0];
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + type.name().toLowerCase().replace("_", " ") + " of command " + commandName.toString(e, debug);
	}
	
	private enum InfoType {
		NAME,
		DESCRIPTION,
		LABEL,
		USAGE,
		ALIASES,
		PERMISSION,
		PERMISSION_MESSAGE,
		PLUGIN,
	}
	
}
