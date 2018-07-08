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
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
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
@Description("Equips a player with some given armor. This will replace any armor that the player is wearing.")
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
	
	@SuppressWarnings("deprecation")
	@Override
	protected void execute(final Event e) {
		final ItemType[] ts = types.getArray(e);
		for (final LivingEntity en : entities.getArray(e)) {
			if (en instanceof Pig) {
				for (final ItemType t : ts) {
					if (t.isOfType(Material.SADDLE.getId(), (short) 0)) {
						((Pig) en).setSaddle(true);
						break;
					}
				}
				continue;
			} else if (newHorses && en instanceof AbstractHorse) {
				// Spigot's API is bad, just bad... Abstract horse doesn't have horse inventory!
				final Inventory invi = ((AbstractHorse) en).getInventory();
				for (final ItemType t : ts) {
					for (final ItemStack item : t.getAll()) {
						if (item.getType() == Material.SADDLE) {
							invi.setItem(0, item); // Slot 0=saddle
						} else if (item.getType() == Material.IRON_BARDING || item.getType() == Material.GOLD_BARDING || item.getType() == Material.DIAMOND_BARDING) {
							invi.setItem(1, item); // Slot 1=armor
						} else if (item.getType() == Material.CHEST && en instanceof ChestedHorse) {
							((ChestedHorse) en).setCarryingChest(true);
						}
					}
				}
				continue;
			} else if (supportsHorses && en instanceof Horse) {
				final HorseInventory invi = ((Horse) en).getInventory();
				for (final ItemType t : ts) {
					for (final ItemStack item : t.getAll()) {
						if (item.getType() == Material.SADDLE) {
							invi.setSaddle(item);
						} else if (item.getType() == Material.IRON_BARDING || item.getType() == Material.GOLD_BARDING || item.getType() == Material.DIAMOND_BARDING) {
							invi.setArmor(item);
						} else if (item.getType() == Material.CHEST) {
							((Horse) en).setCarryingChest(true);
						}
					}
				}
				continue;
			}
			if (en.getEquipment() == null)
				continue;
			for (final ItemType t : ts) {
				for (final ItemStack item : t.getAll()) {
					switch (item.getType()) {// TODO !Update with every version [items]
						case LEATHER_BOOTS:
						case IRON_BOOTS:
						case GOLD_BOOTS:
						case CHAINMAIL_BOOTS:
						case DIAMOND_BOOTS:
							en.getEquipment().setBoots(item);
							break;
						case LEATHER_LEGGINGS:
						case IRON_LEGGINGS:
						case GOLD_LEGGINGS:
						case CHAINMAIL_LEGGINGS:
						case DIAMOND_LEGGINGS:
							en.getEquipment().setLeggings(item);
							break;
						case LEATHER_CHESTPLATE:
						case IRON_CHESTPLATE:
						case GOLD_CHESTPLATE:
						case CHAINMAIL_CHESTPLATE:
						case DIAMOND_CHESTPLATE:
							en.getEquipment().setChestplate(item);
							break;
						//$CASES-OMITTED$
						default:
							if (!(item.getType().isBlock() || item.getTypeId() == 397 /* mob head */))
								continue;
							//$FALL-THROUGH$
						case LEATHER_HELMET:
						case IRON_HELMET:
						case GOLD_HELMET:
						case CHAINMAIL_HELMET:
						case DIAMOND_HELMET:
							en.getEquipment().setHelmet(item);
					}
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
