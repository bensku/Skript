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
package ch.njol.skript.events;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtClick extends SkriptEvent {
	
	/**
	 * Two hands available.
	 */
	final static boolean twoHanded = Skript.isRunningMinecraft(1, 9);
	
	/**
	 * If a hand has item, it will always be used when the other hand has
	 * nothing.
	 */
	final static boolean alwaysPreferItem = !Skript.isRunningMinecraft(1, 13);
	
	/**
	 * Click types.
	 */
	private final static int RIGHT = 1, LEFT = 2, ANY = RIGHT | LEFT;
	
	/**
	 * If we used "holding" somewhere there, we must check if either hand
	 * contains the tool.
	 */
	private final static int HOLDING = 4;
	
	static {
		/*
		 * On 1.9 and above, handling entity click events is a mess, because
		 * just listening for one event is not enough.
		 * 
		 * PlayerInteractEntityEvent
		 * Good: when it is fired, you can cancel it
		 * Bad: not fired for armor stands, at all
		 * 
		 * PlayerInteractAtEntityEvent
		 * Good: catches clicks on armor stands
		 * Bad: cannot be cancelled if entity is item frame or villager (!)
		 * 
		 * So, we listen to both of those, filter our At when it is not needed
		 * and use it for armor stands. Seems to work.
		 */
		Class<? extends PlayerEvent>[] eventTypes = CollectionUtils.array(PlayerInteractEvent.class, PlayerInteractEntityEvent.class);
		
		Skript.registerEvent("Click", EvtClick.class, eventTypes,
				"[(" + RIGHT + "¦right|" + LEFT + "¦left)(| |-)][mouse(| |-)]click[ing] [on %-entitydata/itemtype%] [(with|using|" + HOLDING + "¦holding) %itemtype%]",
				"[(" + RIGHT + "¦right|" + LEFT + "¦left)(| |-)][mouse(| |-)]click[ing] (with|using|" + HOLDING + "¦holding) %itemtype% on %entitydata/itemtype%")
				.description("Called when a user clicks on a block, an entity or air with or without an item in their hand.",
						"Please note that rightclick events with an empty hand while not looking at a block are not sent to the server, so there's no way to detect them.")
				.examples("on click:",
						"on rightclick holding a fishing rod:",
						"on leftclick on a stone or obsidian:",
						"on rightclick on a creeper:",
						"on click with a sword:")
				.since("1.0");
	}
	
	/**
	 * Only trigger when one of these is interacted with.
	 */
	@Nullable
	private Literal<?> types = null;
	
	/**
	 * Only trigger when then item player clicks with is one of these.
	 */
	@Nullable
	private Literal<ItemType> tools;
	
	/**
	 * Click types to trigger.
	 */
	private int click = ANY;
	
	/**
	 * When true, check both hands for {@link #tools}.
	 */
	boolean isHolding = false;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		click = parser.mark == 0 ? ANY : parser.mark;
		types = args[matchedPattern];
		if (types != null && !ItemType.class.isAssignableFrom(types.getReturnType())) {
			if (click == LEFT) {
				Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.", ErrorQuality.SEMANTIC_ERROR);
				return false;
			} else if (click == ANY) {
				Skript.warning("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event. Change this event to a rightclick to disable this warning message.");
			}
		}
		tools = (Literal<ItemType>) args[1 - matchedPattern];
		isHolding = (parser.mark & HOLDING) != 0; // Check if third-least significant byte is 1
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final Block block;
		final Entity entity;
		
		if (e instanceof PlayerInteractEntityEvent) {
			PlayerInteractEntityEvent clickEvent = ((PlayerInteractEntityEvent) e);
			Entity clicked = clickEvent.getRightClicked();
			
			// Usually, don't handle these events
			if (clickEvent instanceof PlayerInteractAtEntityEvent) {
				// But armor stands are an exception
				// Later, there may be more exceptions...
				if (!(clicked instanceof ArmorStand))
					return false;
			}
			
			if (click == LEFT) // Lefts clicks on entities don't work
				return false;
			
			// PlayerInteractAtEntityEvent called only once for armor stands
			if (twoHanded && !(e instanceof PlayerInteractAtEntityEvent)) {
				//ItemStack mainHand = clickEvent.getPlayer().getInventory().getItemInMainHand();
				//ItemStack offHand = clickEvent.getPlayer().getInventory().getItemInOffHand();
				
				Player player = clickEvent.getPlayer();
				assert player != null;
				// Server fires two click events but only one of them can be cancelled
				// Try to figure if it is this one
				boolean useOffHand = checkUseOffHand(player, click, null, clicked);
				//Skript.info("useOffHand: " + useOffHand);
				//Skript.info("Event hand: " + clickEvent.getHand());
				if ((useOffHand && clickEvent.getHand() == EquipmentSlot.HAND) || (!useOffHand && clickEvent.getHand() == EquipmentSlot.OFF_HAND)) {
					return false;
				}
			}
			
			entity = clicked;
			block = null;
		} else if (e instanceof PlayerInteractEvent) {
			PlayerInteractEvent clickEvent = ((PlayerInteractEvent) e);
			
			// Figure out click type, filter non-click events
			Action a = clickEvent.getAction();
			int click;
			switch (a) {
				case LEFT_CLICK_AIR:
				case LEFT_CLICK_BLOCK:
					click = LEFT;
					break;
				case RIGHT_CLICK_AIR:
				case RIGHT_CLICK_BLOCK:
					click = RIGHT;
					break;
				case PHYSICAL: // Not a click event
				default:
					return false;
			}
			if ((this.click & click) == 0)
				return false; // We don't want to handle this kind of events
			
			if (twoHanded) {
				//ItemStack mainHand = clickEvent.getPlayer().getInventory().getItemInMainHand();
				//ItemStack offHand = clickEvent.getPlayer().getInventory().getItemInOffHand();
				
				Player player = clickEvent.getPlayer();
				assert player != null;
				// Server fires two click events but only one of them can be cancelled
				// Try to figure if it is this one
				boolean useOffHand = checkUseOffHand(player, click, clickEvent.getClickedBlock(), null);
				//Skript.info("useOffHand: " + useOffHand);
				//Skript.info("Event hand: " + clickEvent.getHand());
				if ((useOffHand && clickEvent.getHand() == EquipmentSlot.HAND) || (!useOffHand && clickEvent.getHand() == EquipmentSlot.OFF_HAND)) {
					return false;
				}
			}
			
			block = clickEvent.getClickedBlock();
			entity = null;
		} else {
			assert false;
			return false;
		}
		
		if (tools != null && !tools.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				if (e instanceof PlayerInteractEvent) {
					if (isHolding) {
						PlayerInventory invi = ((PlayerInteractEvent) e).getPlayer().getInventory();
						return t.isOfType(invi.getItemInMainHand()) || t.isOfType(invi.getItemInOffHand());
					} else {
						return t.isOfType(((PlayerInteractEvent) e).getItem());
					}
				} else { // PlayerInteractEntityEvent doesn't have item associated with it
					PlayerInventory invi = ((PlayerInteractEntityEvent) e).getPlayer().getInventory();
					if (isHolding) {
						return t.isOfType(invi.getItemInMainHand()) || t.isOfType(invi.getItemInOffHand());
					} else {
						ItemStack item = ((PlayerInteractEntityEvent) e).getHand() == EquipmentSlot.HAND
								? invi.getItemInMainHand() : invi.getItemInOffHand();
						return t.isOfType(item);
					}
				}
			}
		})) {
			return false;
		}
		
		if (types != null) {
			return types.check(e, new Checker<Object>() {
				@Override
				public boolean check(final Object o) {
					if (entity != null) {
						return o instanceof EntityData ? ((EntityData<?>) o).isInstance(entity) : Relation.EQUAL.is(DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(entity), (ItemType) o));
					} else {
						return o instanceof EntityData ? false : ((ItemType) o).isOfType(block);
					}
				}
			});
		}
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (click == LEFT ? "left" : click == RIGHT ? "right" : "") + "click" + (types != null ? " on " + types.toString(e, debug) : "") + (tools != null ? " holding " + tools.toString(e, debug) : "");
	}
	
	private static final ItemType offUsableItems = Aliases.javaItemType("usable in off hand");
	private static final ItemType mainUsableItems = Aliases.javaItemType("usable in main hand");
	private static final ItemType usableBlocks = Aliases.javaItemType("usable block");
	private static final ItemType usableBlocksMainOnly = Aliases.javaItemType("block usable with main hand");
	
	public static boolean checkUseOffHand(Player player, int clickType, @Nullable Block block, @Nullable Entity entity) {
		if ((clickType & RIGHT) == 0)
			return false; // Attacking with off hand is not possible
		
		boolean mainUsable = false; // Usable item
		boolean offUsable = false;
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		ItemStack offHand = player.getInventory().getItemInOffHand();
		
		Material mainMat = mainHand.getType();
		Material offMat = offHand.getType();
		assert mainMat != null;
		assert offMat != null;
		
		//Skript.info("block is " + block);
		//Skript.info("entity is " + entity);
		
		if (offUsableItems.isOfType(offHand))
			offUsable = true;
		
		// Seriously? Empty hand -> block in hand, since id of AIR < 256 :O
		if ((offMat.isBlock() && offMat != Material.AIR) || PlayerUtils.canEat(player, offMat)) {
			offUsable = true;
		}
		
		if (mainUsableItems.isOfType(mainHand))
			mainUsable = true;
		
		// Seriously? Empty hand -> block in hand, since id of AIR < 256 :O
		if ((mainMat.isBlock() && mainMat != Material.AIR) || PlayerUtils.canEat(player, mainMat)) {
			mainUsable = true;
		}
		
		boolean blockUsable = false;
		boolean mainOnly = false;
		if (block != null) {
			if (usableBlocks.isOfType(block))
				blockUsable = true;
			if (usableBlocksMainOnly.isOfType(block))
				mainOnly = true;
		} else if (entity != null) {
			switch (entity.getType()) {
				case ITEM_FRAME:
				case VILLAGER:
					mainOnly = true;
					break;
					//$CASES-OMITTED$
				default:
					mainOnly = false;
			}
			
			if (entity instanceof Vehicle)
				mainOnly = true;
		}
		
		boolean isSneaking = player.isSneaking();
		//boolean blockInMain = mainHand.getType().isBlock() && mainHand.getType() != Material.AIR;
		//boolean blockInOff = offHand.getType().isBlock() && offHand.getType() != Material.AIR;
		
		if (blockUsable) { // Special behavior
			if (isSneaking) {
				//Skript.info("Is sneaking on usable block!");
				if (offHand.getType() == Material.AIR) return false;
				if (mainHand.getType() == Material.AIR) return true;
				//Skript.info("Sneak checks didn't pass.");
			} else { // When not sneaking, main hand is ALWAYS used
				return false;
			}
		} else if (mainOnly) {
			return false;
		}
		
		//Skript.info("Check for usable items...");
		if (alwaysPreferItem) {
			if (offHand.getType() == Material.AIR) return false;
			if (mainHand.getType() == Material.AIR) return true;
		} else {
			if (mainUsable) return false;
			if (offUsable) return true;
		}
		//Skript.info("No hand has usable item")
		
		//Skript.info("Final return!");
		return false; // Use main hand by default
	}
	
}
