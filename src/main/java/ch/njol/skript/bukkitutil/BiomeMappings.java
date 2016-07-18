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
import ch.njol.skript.util.EnumUtils;
import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 1.8 to 1.9 biome name mappings. 1.9 names make no sense.
 * Should be 1.8 compatible, if it's not there is a bug.
 *
 * @author bensku
 */
public abstract class BiomeMappings {

    private final static EnumUtils<To19Mapping> util19 = new EnumUtils<To19Mapping>(To19Mapping.class, "biomes");
    private final static EnumUtils<Biome> util = new EnumUtils<Biome>(Biome.class, "biomes");

    private final static boolean mapFor19 = Skript.isRunningMinecraft(1, 9);

    public static
    @Nullable
    Biome parse(final String name) {
        if (!mapFor19) return util.parse(name);

        To19Mapping mapping = util19.parse(name);
        if (mapping == null)
            return util.parse(name); // Should not happen - incomplete maps are a mess to work with for programmer
        return mapping.getHandle();
    }

    public static String toString(final Biome biome, final int flags) {
        if (!mapFor19) return util.toString(biome, flags);
        To19Mapping mapping = To19Mapping.getMapping(biome);
        if (mapping == null) return "";
        return util19.toString(mapping, flags);
    }

    public static String getAllNames() {
        if (!mapFor19) return util.getAllNames();
        return util19.getAllNames();
    }

    enum To19Mapping {
        SWAMPLAND(Biome.SWAMPLAND),
        FOREST(Biome.FOREST),
        TAIGA(Biome.TAIGA),
        DESERT(Biome.DESERT),
        PLAINS(Biome.PLAINS),
        HELL(Biome.HELL),
        SKY(Biome.SKY),
        OCEAN(Biome.OCEAN),
        RIVER(Biome.RIVER),
        EXTREME_HILLS(Biome.EXTREME_HILLS),
        FROZEN_OCEAN(Biome.FROZEN_OCEAN),
        FROZEN_RIVER(Biome.FROZEN_RIVER),
        ICE_PLAINS(Biome.ICE_FLATS),
        ICE_MOUNTAINS(Biome.ICE_MOUNTAINS),
        MUSHROOM_ISLAND(Biome.MUSHROOM_ISLAND),
        MUSHROOM_SHORE(Biome.MUSHROOM_ISLAND_SHORE),
        BEACH(Biome.BEACHES),
        DESERT_HILLS(Biome.DESERT_HILLS),
        FOREST_HILLS(Biome.FOREST_HILLS),
        TAIGA_HILLS(Biome.TAIGA_HILLS),
        SMALL_MOUNTAINS(Biome.SMALLER_EXTREME_HILLS),
        JUNGLE(Biome.JUNGLE),
        JUNGLE_HILLS(Biome.JUNGLE_HILLS),
        JUNGLE_EDGE(Biome.JUNGLE_EDGE),
        DEEP_OCEAN(Biome.DEEP_OCEAN),
        STONE_BEACH(Biome.STONE_BEACH),
        COLD_BEACH(Biome.COLD_BEACH),
        BIRCH_FOREST(Biome.BIRCH_FOREST),
        BIRCH_FOREST_HILLS(Biome.BIRCH_FOREST_HILLS),
        ROOFED_FOREST(Biome.ROOFED_FOREST),
        COLD_TAIGA(Biome.TAIGA_COLD),
        COLD_TAIGA_HILLS(Biome.TAIGA_COLD_HILLS),
        MEGA_TAIGA(Biome.REDWOOD_TAIGA),
        MEGA_TAIGA_HILLS(Biome.REDWOOD_TAIGA_HILLS),
        EXTREME_HILLS_PLUS(Biome.EXTREME_HILLS_WITH_TREES),
        SAVANNA(Biome.SAVANNA),
        SAVANNA_PLATEAU(Biome.SAVANNA_ROCK),
        MESA(Biome.MESA),
        MESA_PLATEAU_FOREST(Biome.MESA_ROCK),
        MESA_PLATEAU(Biome.MESA_CLEAR_ROCK),
        SUNFLOWER_PLAINS(Biome.MUTATED_PLAINS),
        DESERT_MOUNTAINS(Biome.MUTATED_DESERT),
        FLOWER_FOREST(Biome.MUTATED_FOREST),
        TAIGA_MOUNTAINS(Biome.MUTATED_TAIGA),
        SWAMPLAND_MOUNTAINS(Biome.MUTATED_SWAMPLAND),
        ICE_PLAINS_SPIKES(Biome.MUTATED_ICE_FLATS),
        JUNGLE_MOUNTAINS(Biome.MUTATED_JUNGLE),
        JUNGLE_EDGE_MOUNTAINS(Biome.MUTATED_JUNGLE_EDGE),
        COLD_TAIGA_MOUNTAINS(Biome.MUTATED_TAIGA_COLD),
        SAVANNA_MOUNTAINS(Biome.MUTATED_SAVANNA),
        SAVANNA_PLATEAU_MOUNTAINS(Biome.MUTATED_SAVANNA_ROCK),
        MESA_BRYCE(Biome.MUTATED_MESA),
        MESA_PLATEAU_FOREST_MOUNTAINS(Biome.MUTATED_MESA_ROCK),
        MESA_PLATEAU_MOUNTAINS(Biome.MUTATED_MESA_CLEAR_ROCK),
        BIRCH_FOREST_MOUNTAINS(Biome.MUTATED_BIRCH_FOREST),
        BIRCH_FOREST_HILLS_MOUNTAINS(Biome.MUTATED_BIRCH_FOREST_HILLS),
        ROOFED_FOREST_MOUNTAINS(Biome.MUTATED_ROOFED_FOREST),
        MEGA_SPRUCE_TAIGA(Biome.MUTATED_REDWOOD_TAIGA),
        EXTREME_HILLS_MOUNTAINS(Biome.MUTATED_EXTREME_HILLS),
        EXTREME_HILLS_PLUS_MOUNTAINS(Biome.MUTATED_EXTREME_HILLS_WITH_TREES),
        MEGA_SPRUCE_TAIGA_HILLS(Biome.MUTATED_REDWOOD_TAIGA_HILLS),
        VOID(Biome.VOID);

        private Biome handle;

        To19Mapping(Biome handle) {
            this.handle = handle;
        }

        public static
        @Nullable
        To19Mapping getMapping(Biome biome) {
            To19Mapping[] values = values();

            for (To19Mapping value : values) {
                if (value.getHandle().equals(biome)) {
                    return value;
                }
            }

            return null;
        }

        public Biome getHandle() {
            return this.handle;
        }
    }
}