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
package ch.njol.skript.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public class ScriptCommandEvent extends CommandEvent {
	
	private final ScriptCommand scriptCommand;
	private final String commandLabel;
	private final String rest;

	private boolean cooldownCancelled = false;

	/**
	 * @param scriptCommand The script command executed.
	 * @param sender The executor of this script command.
	 * @param commandLabel The command name (may be the used alias)
	 * @param rest The rest of the command string (the arguments)
	 */
	public ScriptCommandEvent(ScriptCommand scriptCommand, CommandSender sender, String commandLabel, String rest) {
		super(sender, scriptCommand.getLabel(), rest.split(" "));
		this.scriptCommand = scriptCommand;
		this.commandLabel = commandLabel;
		this.rest = rest;
	}

	/**
	 * @return The script command executed.
	 */
	public ScriptCommand getSkriptCommand() {
		return scriptCommand;
	}

	/**
	 * @return The used command label. This may be a command alias.
	 */
	public String getCommandLabel() {
		return commandLabel;
	}

	/**
	 * @return The arguments combined into one string.
	 * @see CommandEvent#getArgs()
	 */
	public String getArgsString() {
		return rest;
	}

	public boolean isCooldownCancelled() {
		return cooldownCancelled;
	}

	public void setCooldownCancelled(boolean cooldownCancelled) {
		this.cooldownCancelled = cooldownCancelled;
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
