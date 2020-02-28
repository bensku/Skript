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
package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Testable;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Equip")
@Description("Equips an entity with some given armor. This will replace any armor that the entity is wearing.")
@Examples({"equip player with diamond helmet",
		"equip player with all diamond armor"})
@Since("1.0")
public class EffEquip extends Effect implements Testable {
	static {
		Skript.registerEffect(EffEquip.class,
				"equip [%livingentity%] with %itemtypes%",
				"make %livingentity% wear %itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	private Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		entities = (Expression<LivingEntity>) vars[0];
		types = (Expression<ItemType>) vars[1];
		return true;
	}
	
	private final static boolean supportsHorses = Skript.classExists("org.bukkit.entity.Horse");
	private final static boolean newHorses = Skript.classExists("org.bukkit.entity.AbstractHorse");
	private final static boolean supportsLlamas = Skript.classExists("org.bukkit.entity.Llama");
	
	private static final ItemType helmet = Aliases.javaItemType("helmet");
	private static final ItemType chestplate = Aliases.javaItemType("chestplate");
	private static final ItemType leggings = Aliases.javaItemType("leggings");
	private static final ItemType boots = Aliases.javaItemType("boots");
	private static final ItemType horseArmor = Aliases.javaItemType("horse armor");
	private static final ItemType saddle = Aliases.javaItemType("saddle");
	private static final ItemType chest = Aliases.javaItemType("chest");
	private static final ItemType carpet = Aliases.javaItemType("carpet");
	
	@SuppressWarnings("deprecation")
	@Override
	protected void execute(final Event e) {
		final ItemType[] ts = types.getArray(e);
		for (final LivingEntity en : entities.getArray(e)) {
			if (en instanceof Pig) {
				for (final ItemType t : ts) {
					if (t.isOfType(Material.SADDLE)) {
						((Pig) en).setSaddle(true);
						break;
					}
				}
				continue;
			} else if (supportsLlamas && en instanceof Llama) {
				LlamaInventory invi = ((Llama) en).getInventory();
				for (ItemType t : ts) {
					for (ItemStack item : t.getAll()) {
						if (carpet.isOfType(item)) {
							invi.setDecor(item);
						} else if (chest.isOfType(item)) {
							((Llama) en).setCarryingChest(true);
						}
					}
				}
				continue;
			} else if (newHorses && en instanceof AbstractHorse) {
				// Spigot's API is bad, just bad... Abstract horse doesn't have horse inventory!
				final Inventory invi = ((AbstractHorse) en).getInventory();
				for (final ItemType t : ts) {
					for (final ItemStack item : t.getAll()) {
						if (saddle.isOfType(item)) {
							invi.setItem(0, item); // Slot 0=saddle
						} else if (horseArmor.isOfType(item)) {
							invi.setItem(1, item); // Slot 1=armor
						} else if (chest.isOfType(item) && en instanceof ChestedHorse) {
							((ChestedHorse) en).setCarryingChest(true);
						}
					}
				}
				continue;
			} else if (supportsHorses && en instanceof Horse) {
				final HorseInventory invi = ((Horse) en).getInventory();
				for (final ItemType t : ts) {
					for (final ItemStack item : t.getAll()) {
						if (saddle.isOfType(item)) {
							invi.setSaddle(item);
						} else if (horseArmor.isOfType(item)) {
							invi.setArmor(item);
						} else if (chest.isOfType(item)) {
							((Horse) en).setCarryingChest(true);
						}
					}
				}
				continue;
			}
			EntityEquipment equip = en.getEquipment();
			if (equip == null)
				continue;
			for (final ItemType t : ts) {
				for (final ItemStack item : t.getAll()) {
					// Blocks are visible in head slot, too
					// TODO skulls; waiting for decoration aliases
					if (helmet.isOfType(item) || item.getType().isBlock())
						equip.setHelmet(item);
					else if (chestplate.isOfType(item))
						equip.setChestplate(item);
					else if (leggings.isOfType(item))
						equip.setLeggings(item);
					else if (boots.isOfType(item))
						equip.setBoots(item);
					
					// We have no idea where to equip other items
					// User can set them to slot they need custom hats etc.
				}
			}
			if (en instanceof Player)
				PlayerUtils.updateInventory((Player) en);
		}
	}
	
	@Override
	public boolean test(final Event e) {
//		final Iterable<Player> ps = players.getArray(e);
//		for (final ItemType t : types.getArray(e)) {
//			for (final Player p : ps) {
//				//REMIND this + think...
//			}
//		}
		return false;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "equip " + entities.toString(e, debug) + " with " + types.toString(e, debug);
	}
	
}
