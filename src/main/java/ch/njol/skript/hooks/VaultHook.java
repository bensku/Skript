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
package ch.njol.skript.hooks;

import java.io.IOException;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class VaultHook extends Hook<Vault> {
	
	public VaultHook() throws IOException {}
	
	@SuppressWarnings("null")
	public static Economy economy;
	@SuppressWarnings("null")
	public static Chat chat;
	
	@SuppressWarnings("null")
	@Override
	protected boolean init() {
		economy = Bukkit.getServicesManager().getRegistration(Economy.class) == null ? null : Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		chat = Bukkit.getServicesManager().getRegistration(Chat.class) == null ? null : Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
		return economy != null || chat != null;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void loadClasses() throws IOException {
		if (economy != null)
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".economy");
		if (chat != null)
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".chat");
	}
	
	@Override
	public String getName() {
		return "Vault";
	}
	
}
