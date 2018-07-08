/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ch.njol.skript.util.Utils;

public class CommandHandler {
	
	/**
	 * holds information for the console as well
	 */
	final private static HashMap<String, PlayerCommand> playerCommandDatas = new HashMap<String, PlayerCommand>();
	
	final static int linesPerPage = 8;
	final private static String[] commandHelp = {
			" -- actions --",
			" a command should start with one of these",
			"s[elect]: selects an item. same as --s",
			"a[dd]/n[ew]: creates a new node in the selected node",
			"r[ename]: renames the selected node",
			"v[alue]: changes the value of the node",
			"m[ove]: moves the selected node, /s m for more info",
			"d[elete]: deletes the selected node",
			"l[ist]: prints the subnodes of the selected node",
			"e[nable]/d[isable]: en/disable the selected config file",
			"save: saves the config file. same as --save",
			"",
			"-- flags --",
			" these can be added to the command after the action and will be processed beforehand",
			"--s[elect]: selects an item",
			"--c[onfig]: switches to a different config file and selects it's main node",
			"--a[ccept]: accepts all questions of the next option, or the main action if this is the last flag",
			"--v, --vv etc.: sets the verbosity; the move vs the higher",
			"--save: saves the config file after the operation"
	};
	
	public static boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!sender.hasPermission("skript.config")) {
			sender.sendMessage("You do not have permission to use this command");
			return true;
		}
		
		if (args.length == 0 || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("help")) {
			int page = 1;
			try {
				page = Math.min((int) Math.ceil(commandHelp.length / linesPerPage), Math.max(1, Integer.parseInt(args[1])));
			} catch (final Exception e) {}
			sender.sendMessage("§8== Skript help (page " + page + " of " + Math.ceil(commandHelp.length / linesPerPage) + ")");
			for (int i = (page - 1) * linesPerPage; i < page * linesPerPage; i++) {
				sender.sendMessage(commandHelp[i]);
			}
		}
		
		// output the input, so one can see what the messages refer to.
		sender.sendMessage("&7" + label + Utils.join(args, " "));
		
		PlayerCommand data = playerCommandDatas.get(sender.getName());
		if (data == null) {
			data = new PlayerCommand();
			playerCommandDatas.put(sender.getName(), data);
		}
		
		data.onCommand(sender, command, label, args);
		
		return true;
	}
	
}
