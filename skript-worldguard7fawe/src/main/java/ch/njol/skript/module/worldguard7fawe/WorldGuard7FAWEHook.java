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
package ch.njol.skript.module.worldguard7fawe;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * WorldGuard hook that works with FAWE's WG 7.
 */
public class WorldGuard7FAWEHook extends RegionsPlugin<WorldGuardPlugin> {
    
    public WorldGuard7FAWEHook() throws IOException {}
    
    boolean supportsUUIDs;
    
    @Override
    protected boolean init() {
        supportsUUIDs = Skript.methodExists(DefaultDomain.class, "getUniqueIds");
        
        // Manually load syntaxes for regions, because we're in module package
        try {
            Skript.getAddonInstance().loadClasses("ch.njol.skript.hooks.regions");
        } catch (IOException e) {
            Skript.exception(e);
            return false;
        }
        return super.init();
    }
    
    @Override
    public String getName() {
        return "WorldGuard";
    }
    
    @Override
    public boolean canBuild_i(final Player p, final Location l) {
        if (p.hasPermission("worldguard.region.bypass." + l.getWorld().getName())) return true;
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer(p);
        return query.testState(BukkitAdapter.adapt(l), player, Flags.BUILD);
    }
    
    @YggdrasilID("WorldGuardRegion")
    public final class WorldGuardRegion extends Region {
        
        final World world;
        private transient ProtectedRegion region;
        
        @SuppressWarnings({"null", "unused"})
        private WorldGuardRegion() {
            world = null;
        }
        
        public WorldGuardRegion(final World w, final ProtectedRegion r) {
            world = w;
            region = r;
        }
        
        @Override
        public boolean contains(final Location l) {
            return l.getWorld().equals(world) && region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public boolean isMember(final OfflinePlayer p) {
            if (supportsUUIDs)
                return region.isMember(plugin.wrapOfflinePlayer(p));
            else
                return region.isMember(p.getName());
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public Collection<OfflinePlayer> getMembers() {
            if (supportsUUIDs) {
                final Collection<UUID> ids = region.getMembers().getUniqueIds();
                final Collection<OfflinePlayer> r = new ArrayList<>(ids.size());
                for (final UUID id : ids)
                    r.add(Bukkit.getOfflinePlayer(id));
                return r;
            } else {
                final Collection<String> ps = region.getMembers().getPlayers();
                final Collection<OfflinePlayer> r = new ArrayList<>(ps.size());
                for (final String p : ps)
                    r.add(Bukkit.getOfflinePlayer(p));
                return r;
            }
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public boolean isOwner(final OfflinePlayer p) {
            if (supportsUUIDs)
                return region.isOwner(plugin.wrapOfflinePlayer(p));
            else
                return region.isOwner(p.getName());
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public Collection<OfflinePlayer> getOwners() {
            if (supportsUUIDs) {
                final Collection<UUID> ids = region.getOwners().getUniqueIds();
                final Collection<OfflinePlayer> r = new ArrayList<>(ids.size());
                for (final UUID id : ids)
                    r.add(Bukkit.getOfflinePlayer(id));
                return r;
            } else {
                final Collection<String> ps = region.getOwners().getPlayers();
                final Collection<OfflinePlayer> r = new ArrayList<>(ps.size());
                for (final String p : ps)
                    r.add(Bukkit.getOfflinePlayer(p));
                return r;
            }
        }
        
        @Override
        public Iterator<Block> getBlocks() {
            final BlockVector min = region.getMinimumPoint(), max = region.getMaximumPoint();
            return new AABB(world, new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()), new Vector(max.getBlockX() + 1, max.getBlockY() + 1, max.getBlockZ() + 1)).iterator();
        }
        
        @Override
        public Fields serialize() throws NotSerializableException {
            final Fields f = new Fields(this);
            f.putObject("region", region.getId());
            return f;
        }
        
        @Override
        public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
            final String r = fields.getAndRemoveObject("region", String.class);
            fields.setFields(this);
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            final ProtectedRegion region = regionManager.getRegion(r);
            if (region == null)
                throw new StreamCorruptedException("Invalid region " + r + " in world " + world);
            this.region = region;
        }
        
        @Override
        public String toString() {
            return region.getId() + " in world " + world.getName();
        }
        
        @Override
        public RegionsPlugin<?> getPlugin() {
            return WorldGuard7FAWEHook.this;
        }
        
        @Override
        public boolean equals(final @Nullable Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (!(o instanceof WorldGuardRegion))
                return false;
            return world.equals(((WorldGuardRegion) o).world) && region.equals(((WorldGuardRegion) o).region);
        }
        
        @Override
        public int hashCode() {
            return world.hashCode() * 31 + region.hashCode();
        }
        
    }
    
    @SuppressWarnings("null")
    @Override
    public Collection<? extends Region> getRegionsAt_i(@Nullable final Location l) {
        final ArrayList<Region> r = new ArrayList<>();
        
        if (l == null) // Working around possible cause of issue #280
            return Collections.emptyList();
        if (l.getWorld() == null)
            return Collections.emptyList();
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        if (query == null)
            return r;
        ApplicableRegionSet applicable = query.getApplicableRegions(BukkitAdapter.adapt(l));
        if (applicable == null)
            return r;
        final Iterator<ProtectedRegion> i = applicable.iterator();
        while (i.hasNext())
            r.add(new WorldGuardRegion(l.getWorld(), i.next()));
        return r;
    }
    
    @Override
    @Nullable
    public Region getRegion_i(final World world, final String name) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        final ProtectedRegion r = manager.getRegion(name);
        if (r != null)
            return new WorldGuardRegion(world, r);
        return null;
    }
    
    @Override
    public boolean hasMultipleOwners_i() {
        return true;
    }
    
    @Override
    protected Class<? extends Region> getRegionClass() {
        return WorldGuardRegion.class;
    }
    
}
