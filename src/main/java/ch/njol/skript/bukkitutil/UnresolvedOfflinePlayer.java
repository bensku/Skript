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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.util.Callback;
import ch.njol.util.Closeable;

/**
 * An {@link OfflinePlayer} that has only a name but no UUID set.
 * <p>
 * Should only be used in {@link Commands} and the parser for offline players.
 * <p>
 * Will produce {@link NullPointerException}s if used incorrectly.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
public class UnresolvedOfflinePlayer implements OfflinePlayer {
	
	static LinkedBlockingQueue<UnresolvedOfflinePlayer> toResolve;
	final static Thread resolverThread;
	
	static {
		resolverThread = Skript.newThread(new Runnable() {
			@SuppressWarnings({"deprecation", "unused"})
			@Override
			public void run() {				
				while (true) {
					if (toResolve == null) {
						toResolve = new LinkedBlockingQueue<>();
					}
					
					try {
						final UnresolvedOfflinePlayer p = toResolve.take();
						p.bukkitOfflinePlayer = Bukkit.getOfflinePlayer(p.name);
						p.callback.run(p);
					} catch (final InterruptedException e) {
						break;
					}
				}
			}
		}, "Skript offline player resolver thread (fetches UUIDs from the minecraft servers)");
		resolverThread.start();
		Skript.closeOnDisable(new Closeable() {
			@Override
			public void close() {
				resolverThread.interrupt();
			}
		});
	}
	
	final String name;
	@Nullable
	OfflinePlayer bukkitOfflinePlayer = null;
	final Callback<Void, OfflinePlayer> callback;
	
	/**
	 * @param name The player's name
	 * @param callback A callback that will be run when the player has been resolved. It will be called on the resolver thread which should not be blocked.
	 */
	public UnresolvedOfflinePlayer(final String name, final Callback<Void, OfflinePlayer> callback) {
		this.name = name;
		this.callback = callback;
		
		toResolve.add(this);
	}
	
	@Override
	public String getName() {
		return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getName() : name;
	}
	
	@Override
	public boolean isOnline() {
		return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.isOnline() : getPlayer() != null;
	}
	
	@Override
	@Nullable
	public Player getPlayer() {
		return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getPlayer() : Bukkit.getPlayerExact(name);
	}
	
	@Override
	public boolean isOp() {
		return bukkitOfflinePlayer.isOp();
	}
	
	@Override
	public void setOp(final boolean value) {
		bukkitOfflinePlayer.setOp(value);
	}
	
	@Override
	public UUID getUniqueId() {
		return bukkitOfflinePlayer.getUniqueId();
	}
	
	@Override
	public Map<String, Object> serialize() {
		return bukkitOfflinePlayer.serialize();
	}
	
	@Override
	public boolean isBanned() {
		return bukkitOfflinePlayer.isBanned();
	}
	
	@Override
	public boolean isWhitelisted() {
		return bukkitOfflinePlayer.isWhitelisted();
	}
	
	@Override
	public void setWhitelisted(final boolean value) {
		bukkitOfflinePlayer.setWhitelisted(value);
	}
	
	@Override
	public long getFirstPlayed() {
		return bukkitOfflinePlayer.getFirstPlayed();
	}
	
	@Override
	@Deprecated
	public long getLastPlayed() {
		return bukkitOfflinePlayer.getLastPlayed();
	}
	
	@Override
	public boolean hasPlayedBefore() {
		return bukkitOfflinePlayer.hasPlayedBefore();
	}
	
	@Override
	public Location getBedSpawnLocation() {
		return bukkitOfflinePlayer.getBedSpawnLocation();
	}

	@Override
	public long getLastLogin() {
		return bukkitOfflinePlayer.getLastLogin();
	}

	@Override
	public long getLastSeen() {
		return bukkitOfflinePlayer.getLastSeen();
	}
	
	@Override
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
		bukkitOfflinePlayer.incrementStatistic(statistic);
	}
	
	@Override
	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
		bukkitOfflinePlayer.decrementStatistic(statistic);
	}
	
	@Override
	public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		bukkitOfflinePlayer.incrementStatistic(statistic, amount);
	}
	
	@Override
	public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		bukkitOfflinePlayer.decrementStatistic(statistic, amount);
	}
	
	@Override
	public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
		bukkitOfflinePlayer.setStatistic(statistic, newValue);
	}
	
	@Override
	public int getStatistic(Statistic statistic) throws IllegalArgumentException {
		return bukkitOfflinePlayer.getStatistic(statistic);
	}
	
	@Override
	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		bukkitOfflinePlayer.incrementStatistic(statistic, material);
	}
	
	@Override
	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		bukkitOfflinePlayer.decrementStatistic(statistic, material);
	}
	
	@Override
	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		return bukkitOfflinePlayer.getStatistic(statistic, material);
	}
	
	@Override
	public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		bukkitOfflinePlayer.incrementStatistic(statistic, material, amount);
	}
	
	@Override
	public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		bukkitOfflinePlayer.decrementStatistic(statistic, material, amount);
	}
	
	@Override
	public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
		bukkitOfflinePlayer.setStatistic(statistic, material, newValue);
	}
	
	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
	
	}
	
	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		bukkitOfflinePlayer.decrementStatistic(statistic, entityType);
	}
	
	@Override
	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		return bukkitOfflinePlayer.getStatistic(statistic, entityType);
	}
	
	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {
		bukkitOfflinePlayer.incrementStatistic(statistic, entityType, amount);
	}
	
	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
		bukkitOfflinePlayer.decrementStatistic(statistic, entityType, amount);
	}
	
	@Override
	public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
		bukkitOfflinePlayer.setStatistic(statistic, entityType, newValue);
	}
	
}
