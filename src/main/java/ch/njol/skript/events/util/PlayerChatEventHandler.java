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
package ch.njol.skript.events.util;

import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class PlayerChatEventHandler {
	
	private PlayerChatEventHandler() {}
	
	public final static boolean usesAsyncEvent = Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent");
	
	public static void registerChatEvent(final EventPriority priority, final EventExecutor executor, final boolean ignoreCancelled) {
		if (Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent"))
			Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, new Listener() {}, priority, executor, Skript.getInstance(), ignoreCancelled);
		else
			Bukkit.getPluginManager().registerEvent(PlayerChatEvent.class, new Listener() {}, priority, executor, Skript.getInstance(), ignoreCancelled);
	}
	
}
