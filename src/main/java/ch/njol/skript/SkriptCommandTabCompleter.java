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
package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class SkriptCommandTabCompleter implements TabCompleter {
		
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> options = new ArrayList<>();
		
		if (!command.getName().equalsIgnoreCase("skript")) {
			return null;
		}
		
		if (args[0].length() == 0) {
			options.add("help");
			options.add("reload");
			options.add("enable");
			options.add("disable");
			options.add("update");
			options.add("info");
		}
		
		else if (args[0].equalsIgnoreCase("update")) {
			options.add("check");
			options.add("changes");
			options.add("download");
		}
		
		else if (args[0].matches("(?i)(reload|disable|enable)")) {
			File scripts = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER);
			String scriptArg = StringUtils.join(args, " ", 1, args.length); 
			String fs = File.separator;
			
			try {
				Files.walk(scripts.toPath())
					.map(Path::toFile)
					.filter(f -> (!f.isDirectory() && f.getName().toLowerCase().endsWith(".sk")) || f.isDirectory()) // filter folders and skript files only 
					.filter(f -> { // Filtration for enable, disable and reload
						if (args[0].equalsIgnoreCase("enable"))
							return f.getName().startsWith("-");
						else // reload & disable both accepts only non-hyphened files and not hidden folders
							return !f.getName().startsWith("-") && (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory());
					})
					.filter(f -> { // Autocomplete incomplete script name arg
						return scriptArg.length() > 0 ? f.getName().startsWith(scriptArg) : true;
					})
					.forEach(f -> {
						if (!f.toString().equals(scripts.toString()))
							options.add(f.toString()
								.replace(scripts.toPath().toString() + fs, "") // Extract file short path
								.replace(scripts.toPath().toString(), "") // Extract file short path
								+ (f.isDirectory() && f.toString().length() > 0 ? fs : "")); // add File.separator at the end of directories
					}); 
				
			// TODO handle file permissions
			} catch ( IOException e) {
				e.printStackTrace();
			}
			
			// These will be added even if there are incomplete script arg
			options.add("all");
			if (args[0].equalsIgnoreCase("reload")) {
				options.add("config");
				options.add("aliases");
				options.add("scripts");
			}
		}
		
		return options;
	}
	
}
