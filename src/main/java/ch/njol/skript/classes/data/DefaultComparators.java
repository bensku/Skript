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
package ch.njol.skript.classes.data;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;

import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.entity.BoatData;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

@SuppressWarnings({"rawtypes"})
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// Number - Number
		Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			@Override
			public Relation compare(final Number n1, final Number n2) {
				if (n1 instanceof Long && n2 instanceof Long)
					return Relation.get(n1.longValue() - n2.longValue());
				Double d1 = n1.doubleValue(),
					   d2 = n2.doubleValue();
				if (d1.isNaN() || d2.isNaN()) {
					return Relation.SMALLER;
				} else if (d1.isInfinite() || d2.isInfinite()) {
					return d1 > d2 ? Relation.GREATER : d1 < d2 ? Relation.SMALLER : Relation.EQUAL;
				} else {
					final double diff = d1 - d2;
					if (Math.abs(diff) < Skript.EPSILON)
						return Relation.EQUAL;
					return Relation.get(diff);
				}
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Slot - Slot
		Comparators.registerComparator(Slot.class, Slot.class, new Comparator<Slot, Slot>() {

			@Override
			public Relation compare(Slot o1, Slot o2) {
				if (o1.isSameSlot(o2))
					return Relation.EQUAL;
				return Relation.NOT_EQUAL;
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// Slot - Integer
		Comparators.registerComparator(Slot.class, Integer.class, new Comparator<Slot, Integer>() {

			@Override
			public Relation compare(Slot o1, Integer o2) {
				if (o1 instanceof SlotWithIndex) {
					boolean same = ((SlotWithIndex) o1).getIndex() == o2;
					if (same) // Slot has index and the index is same with number
						return Relation.EQUAL;
				}
				return Relation.NOT_EQUAL;
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// ItemStack - ItemType
		Comparators.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
			@Override
			public Relation compare(final ItemStack is, final ItemType it) {
				return Relation.get(it.isOfType(is));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - ItemType
		Comparators.registerComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
			@Override
			public Relation compare(final Block b, final ItemType it) {
				return Relation.get(it.isOfType(b));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// ItemType - ItemType
		Comparators.registerComparator(ItemType.class, ItemType.class, new Comparator<ItemType, ItemType>() {
			@Override
			public Relation compare(final ItemType i1, final ItemType i2) {
				return Relation.get(i2.isSupertypeOf(i1));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - Block
		Comparators.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
			@Override
			public Relation compare(final Block b1, final Block b2) {
				return Relation.get(b1.equals(b2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Entity - EntityData
		Comparators.registerComparator(Entity.class, EntityData.class, new Comparator<Entity, EntityData>() {
			@Override
			public Relation compare(final Entity e, final EntityData t) {
				return Relation.get(t.isInstance(e));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		// EntityData - EntityData
		Comparators.registerComparator(EntityData.class, EntityData.class, new Comparator<EntityData, EntityData>() {
			@Override
			public Relation compare(final EntityData t1, final EntityData t2) {
				return Relation.get(t2.isSupertypeOf(t1));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
	}
	
	// EntityData - ItemType
	final static LinkedHashMap<Class<? extends Entity>, Material> entityMaterials = new LinkedHashMap<>();
	static {
		// to fix comparisons of eggs, arrows, etc. (e.g. 'projectile is an arrow')
		// TODO !Update with every version [entities]
		entityMaterials.put(Boat.class, Material.BOAT);
		entityMaterials.put(Painting.class, Material.PAINTING);
		entityMaterials.put(Arrow.class, Material.ARROW);
		entityMaterials.put(Egg.class, Material.EGG);
		entityMaterials.put(Chicken.class, Material.RAW_CHICKEN);
		entityMaterials.put(EnderPearl.class, Material.ENDER_PEARL);
		entityMaterials.put(Snowball.class, Material.SNOW_BALL);
		entityMaterials.put(ThrownExpBottle.class, Material.EXP_BOTTLE);
//		entityMaterials.put(Fish.class, Material.RAW_FISH); // TODO 1.7
		entityMaterials.put(TNTPrimed.class, Material.TNT);
		entityMaterials.put(Slime.class, Material.SLIME_BALL);
		if (Skript.classExists("org.bukkit.entity.ItemFrame"))
			entityMaterials.put(ItemFrame.class, Material.ITEM_FRAME);
		if (Skript.classExists("org.bukkit.entity.Firework"))
			entityMaterials.put(Firework.class, Material.FIREWORK);
		if (Skript.classExists("org.bukkit.entity.minecart.StorageMinecart")) {
			entityMaterials.put(org.bukkit.entity.minecart.StorageMinecart.class, Material.STORAGE_MINECART);
			entityMaterials.put(org.bukkit.entity.minecart.PoweredMinecart.class, Material.POWERED_MINECART);
			entityMaterials.put(RideableMinecart.class, Material.MINECART);
			entityMaterials.put(HopperMinecart.class, Material.HOPPER_MINECART);
			entityMaterials.put(ExplosiveMinecart.class, Material.EXPLOSIVE_MINECART);
			entityMaterials.put(Minecart.class, Material.MINECART);
		} else { // 1.11 does not have these classes, and we compile against 1.11+ API now
			try {
				@SuppressWarnings("unchecked")
				Class<Entity> storageCart = (Class<Entity>) Class.forName("org.bukkit.entity.StorageMinecart");
				entityMaterials.put(storageCart, Material.STORAGE_MINECART);
				@SuppressWarnings("unchecked")
				Class<Entity> poweredCart = (Class<Entity>) Class.forName("org.bukkit.entity.PoweredMinecart");
				entityMaterials.put(poweredCart, Material.POWERED_MINECART);
				entityMaterials.put(Minecart.class, Material.MINECART);
			} catch (ClassNotFoundException e) {
				Skript.exception(e, "Cannot initialize material support for minecarts");
			}
		}
		if (Skript.classExists("org.bukkit.entity.ArmorStand"))
			entityMaterials.put(ArmorStand.class, Material.ARMOR_STAND);
	}
	public final static Comparator<EntityData, ItemType> entityItemComparator = new Comparator<EntityData, ItemType>() {
		@Override
		public Relation compare(final EntityData e, final ItemType i) {
			if (e instanceof Item)
				return Relation.get(i.isOfType(((Item) e).getItemStack()));
			if (e instanceof ThrownPotion)
				return Relation.get(i.isOfType(Material.POTION.getId(), PotionEffectUtils.guessData((ThrownPotion) e)));
			if (Skript.classExists("org.bukkit.entity.WitherSkull") && e instanceof WitherSkull)
				return Relation.get(i.isOfType(Material.SKULL_ITEM.getId(), (short) 1));
			if (e instanceof BoatData)
				return Relation.get(((BoatData)e).isOfItemType(i));
			if (entityMaterials.containsKey(e.getType()))
				return Relation.get(i.isOfType(entityMaterials.get(e.getType()).getId(), (short) 0));
			for (final Entry<Class<? extends Entity>, Material> m : entityMaterials.entrySet()) {
				if (m.getKey().isAssignableFrom(e.getType()))
					return Relation.get(i.isOfType(m.getValue().getId(), (short) 0));
			}
			return Relation.NOT_EQUAL;
		}
		
		@Override
		public boolean supportsOrdering() {
			return false;
		}
	};
	static {
		Comparators.registerComparator(EntityData.class, ItemType.class, entityItemComparator);
	}
	
	static {
		// CommandSender - CommandSender
		Comparators.registerComparator(CommandSender.class, CommandSender.class, new Comparator<CommandSender, CommandSender>() {
			@Override
			public Relation compare(final CommandSender s1, final CommandSender s2) {
				return Relation.get(s1.equals(s2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// OfflinePlayer - OfflinePlayer
		Comparators.registerComparator(OfflinePlayer.class, OfflinePlayer.class, new Comparator<OfflinePlayer, OfflinePlayer>() {
			@Override
			public Relation compare(final OfflinePlayer p1, final OfflinePlayer p2) {
				return Relation.get(Objects.equals(p1.getName(), p2.getName()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// OfflinePlayer - String
		Comparators.registerComparator(OfflinePlayer.class, String.class, new Comparator<OfflinePlayer, String>() {
			@Override
			public Relation compare(final OfflinePlayer p, final String name) {
				String offlineName = p.getName();
				return offlineName == null ? Relation.NOT_EQUAL : Relation.get(offlineName.equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// World - String
		Comparators.registerComparator(World.class, String.class, new Comparator<World, String>() {
			@Override
			public Relation compare(final World w, final String name) {
				return Relation.get(w.getName().equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// String - String
		Comparators.registerComparator(String.class, String.class, new Comparator<String, String>() {
			@Override
			public Relation compare(final String s1, final String s2) {
				return Relation.get(StringUtils.equals(s1, s2, SkriptConfig.caseSensitive.value()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Date - Date
		Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
			@Override
			public Relation compare(final Date d1, final Date d2) {
				return Relation.get(d1.compareTo(d2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Time
		Comparators.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
			@Override
			public Relation compare(final Time t1, final Time t2) {
				return Relation.get(t1.getTime() - t2.getTime());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Timespan - Timespan
		Comparators.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
			@Override
			public Relation compare(final Timespan t1, final Timespan t2) {
				return Relation.get(t1.getMilliSeconds() - t2.getMilliSeconds());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Timeperiod
		Comparators.registerComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
			@Override
			public Relation compare(final Time t, final Timeperiod p) {
				return Relation.get(p.contains(t));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// StructureType - StructureType
		Comparators.registerComparator(StructureType.class, StructureType.class, new Comparator<StructureType, StructureType>() {
			@Override
			public Relation compare(final StructureType s1, final StructureType s2) {
				return Relation.get(CollectionUtils.containsAll(s2.getTypes(), s2.getTypes()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Object - ClassInfo
		Comparators.registerComparator(Object.class, ClassInfo.class, new Comparator<Object, ClassInfo>() {
			@Override
			public Relation compare(final Object o, final ClassInfo c) {
				return Relation.get(c.getC().isInstance(o) || o instanceof ClassInfo && c.getC().isAssignableFrom(((ClassInfo<?>) o).getC()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// DamageCause - ItemType
		Comparators.registerComparator(DamageCause.class, ItemType.class, new Comparator<DamageCause, ItemType>() {
			@Override
			public Relation compare(final DamageCause dc, final ItemType t) {
				switch (dc) {
					case FIRE:
						return Relation.get(t.isOfType(Material.FIRE.getId(), (short) -1));
					case LAVA:
						return Relation.get(t.isOfType(Material.LAVA.getId(), (short) -1) && t.isOfType(Material.STATIONARY_LAVA.getId(), (short) -1));
					case MAGIC:
						return Relation.get(t.isOfType(Material.POTION.getId(), (short) -1));
						//$CASES-OMITTED$
					default:
						return Relation.NOT_EQUAL;
				}
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		// DamageCause - EntityData
		Comparators.registerComparator(DamageCause.class, EntityData.class, new Comparator<DamageCause, EntityData>() {
			@Override
			public Relation compare(final DamageCause dc, final EntityData e) {
				switch (dc) {
					case ENTITY_ATTACK:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Entity.class)));
					case PROJECTILE:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Projectile.class)));
					case WITHER:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Wither.class)));
					case FALLING_BLOCK:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(FallingBlock.class)));
						//$CASES-OMITTED$
					default:
						return Relation.NOT_EQUAL;
				}
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
	}
	
}
