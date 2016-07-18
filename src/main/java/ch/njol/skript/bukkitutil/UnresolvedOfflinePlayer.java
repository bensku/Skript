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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.util.Callback;
import ch.njol.util.Closeable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

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

    final static LinkedBlockingQueue<UnresolvedOfflinePlayer> toResolve = new LinkedBlockingQueue<UnresolvedOfflinePlayer>();

    final static Thread resolverThread = Skript.newThread(new Runnable() {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            while (true) {
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

    static {
        resolverThread.start();
        Skript.closeOnDisable(new Closeable() {
            @Override
            public void close() {
                resolverThread.interrupt();
            }
        });
    }

    final String name;
    final Callback<Void, OfflinePlayer> callback;
    @Nullable
    OfflinePlayer bukkitOfflinePlayer = null;

    /**
     * @param name     The player's name
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
    @SuppressWarnings("deprecation")
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
    @Deprecated
    public void setBanned(final boolean banned) {
        bukkitOfflinePlayer.setBanned(banned);
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

}
