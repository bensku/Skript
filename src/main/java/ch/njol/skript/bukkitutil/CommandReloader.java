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
package ch.njol.skript.bukkitutil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import org.bukkit.Server;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * Utilizes CraftServer with reflection to re-send commands to clients.
 */
public class CommandReloader {
	
	@Nullable
	private static Method syncCommandsMethod;
	
	static {
		try {
			Class<?> craftServer = Class.forName("org.bukkit.craftbukkit.v1_13_R2.CraftServer");
			syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands");
			if (syncCommandsMethod != null)
				syncCommandsMethod.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// Ignore except for debugging. This is not necessary or in any way supported functionality
			if (Skript.debug())
				e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to register Bukkit commands to Brigadier and synchronize them
	 * to all clients. This <i>may</i> fail for any reason or no reason at all!
	 * @param server Server to use.
	 * @return Whether it is likely that we succeeded or not.
	 */
	public static boolean syncCommands(Server server) {
		if (syncCommandsMethod == null)
			return false; // Method not available, can't sync
		try {
			assert syncCommandsMethod != null;
			syncCommandsMethod.invoke(server);
			return true; // Sync probably succeeded
		} catch (Throwable e) {
			if (Skript.debug())
				e.printStackTrace();
			return false; // Something went wrong, sync probably failed
		}
	}
}
