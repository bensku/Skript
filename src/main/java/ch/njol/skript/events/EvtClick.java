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
	
	final static boolean twoHanded = Skript.isRunningMinecraft(1, 9);
	
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
		Class<? extends PlayerEvent> clickEvent;
		if (twoHanded) // Armor stand support!
			clickEvent = PlayerInteractAtEntityEvent.class;
		else
			clickEvent = PlayerInteractEntityEvent.class;
		
		Class<? extends PlayerEvent>[] eventTypes = CollectionUtils.array(PlayerInteractEvent.class, clickEvent);
		Skript.registerEvent("Click", EvtClick.class, eventTypes,
				"[(" + RIGHT + "¦right|" + LEFT + "¦left)(| |-)][mouse(| |-)]click[ing] [on %-entitydata/itemtype%] [(with|using|" + HOLDING + "¦holding) %itemtype%]",
				"[(" + RIGHT + "¦right|" + LEFT + "¦left)(| |-)][mouse(| |-)]click[ing] (with|using|" + HOLDING + "¦holding) %itemtype% on %entitydata/itemtype%")
				.description("Called when a user clicks on a block, an entity or air with or without an item in their hand.",
						"Please note that rightclick events with an empty hand while not looking at a block are not sent to the server, so there's no way to detect them.")
				.examples("on click",
						"on rightclick holding a fishing rod",
						"on leftclick on a stone or obsidian",
						"on rightclick on a creeper",
						"on click with a sword")
				.since("1.0");
	}
	
	@Nullable
	private Literal<?> types = null;
	@Nullable
	private Literal<ItemType> tools;
	
	private int click = ANY;
	boolean isHolding = false;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		//Skript.info("matchedPattern is " + matchedPattern);
		//Skript.info("args is " + Arrays.toString(args));
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
			if (twoHanded) {
				//ItemStack mainHand = clickEvent.getPlayer().getInventory().getItemInMainHand();
				//ItemStack offHand = clickEvent.getPlayer().getInventory().getItemInOffHand();
				
				Player player = clickEvent.getPlayer();
				assert player != null;
				boolean useOffHand = checkUseOffHand(player, click, null, clickEvent.getRightClicked());
				//Skript.info("useOffHand: " + useOffHand);
				//Skript.info("Event hand: " + clickEvent.getHand());
				if ((useOffHand && clickEvent.getHand() == EquipmentSlot.HAND) || (!useOffHand && clickEvent.getHand() == EquipmentSlot.OFF_HAND)) {
					return false;
				}
			}
			
			if (click == LEFT || types == null) // types == null  will be handled by the PlayerInteractEvent that is fired as well
				return false;
			entity = clickEvent.getRightClicked();
			block = null;
		} else if (e instanceof PlayerInteractEvent) {
			PlayerInteractEvent clickEvent = ((PlayerInteractEvent) e);
			if (twoHanded) {
				//ItemStack mainHand = clickEvent.getPlayer().getInventory().getItemInMainHand();
				//ItemStack offHand = clickEvent.getPlayer().getInventory().getItemInOffHand();
				
				Player player = clickEvent.getPlayer();
				assert player != null;
				boolean useOffHand = checkUseOffHand(player, click, clickEvent.getClickedBlock(), null);
				//Skript.info("useOffHand: " + useOffHand);
				//Skript.info("Event hand: " + clickEvent.getHand());
				if ((useOffHand && clickEvent.getHand() == EquipmentSlot.HAND) || (!useOffHand && clickEvent.getHand() == EquipmentSlot.OFF_HAND)) {
					return false;
				}
			}
			
			final Action a = clickEvent.getAction();
			final int click;
			switch (a) {
				case LEFT_CLICK_AIR:
				case LEFT_CLICK_BLOCK:
					click = LEFT;
					break;
				case RIGHT_CLICK_AIR:
				case RIGHT_CLICK_BLOCK:
					click = RIGHT;
					break;
				case PHYSICAL:
				default:
					return false;
			}
			if ((this.click & click) == 0)
				return false;
			block = clickEvent.getClickedBlock();
			entity = null;
		} else {
			assert false;
			return false;
		}
		
		if (e instanceof PlayerInteractEvent && tools != null && !tools.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				if (isHolding && twoHanded) {
					PlayerInventory invi = ((PlayerInteractEvent) e).getPlayer().getInventory();
					return t.isOfType(invi.getItemInMainHand()) || t.isOfType(invi.getItemInOffHand());
				} else {
					return t.isOfType(((PlayerInteractEvent) e).getItem());
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
	
	public static boolean checkUseOffHand(Player player, int clickType, @Nullable Block block, @Nullable Entity entity) {
		if (clickType != RIGHT) return false; // Attacking with off hand is not possible
		
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
		
		switch (offHand.getType()) {
			case BOW:
			case EGG:
			case SPLASH_POTION:
			case SNOW_BALL:
			case BUCKET:
			case FISHING_ROD:
			case FLINT_AND_STEEL:
			case WOOD_HOE:
			case STONE_HOE:
			case IRON_HOE:
			case GOLD_HOE:
			case DIAMOND_HOE:
			case LEASH:
			case SHEARS:
			case WOOD_SPADE:
			case STONE_SPADE:
			case IRON_SPADE:
			case GOLD_SPADE:
			case DIAMOND_SPADE:
			case SHIELD:
			case ENDER_PEARL:
			case MONSTER_EGG:
				offUsable = true;
				break;
				//$CASES-OMITTED$
			default:
				offUsable = false;
		}
		
		// Seriously? Empty hand -> block in hand, since id of AIR < 256 :O
		if ((offMat.isBlock() && offMat != Material.AIR) || PlayerUtils.canEat(player, offMat)) {
			offUsable = true;
		}
		
		switch (mainHand.getType()) {
			case BOW:
			case EGG:
			case SPLASH_POTION:
			case SNOW_BALL:
			case BUCKET:
			case FISHING_ROD:
			case FLINT_AND_STEEL:
			case WOOD_HOE:
			case STONE_HOE:
			case IRON_HOE:
			case GOLD_HOE:
			case DIAMOND_HOE:
			case LEASH:
			case SHEARS:
			case WOOD_SPADE:
			case STONE_SPADE:
			case IRON_SPADE:
			case GOLD_SPADE:
			case DIAMOND_SPADE:
			case ENDER_PEARL:
			case EYE_OF_ENDER:
			case MONSTER_EGG:
				mainUsable = true;
				break;
				//$CASES-OMITTED$
			default:
				mainUsable = false;
		}
		
		// Seriously? Empty hand -> block in hand, since id of AIR < 256 :O
		if ((mainMat.isBlock() && mainMat != Material.AIR) || PlayerUtils.canEat(player, mainMat)) {
			mainUsable = true;
		}
		
		boolean blockUsable = false;
		boolean mainOnly = false;
		if (block != null) {
			switch (block.getType()) {
				case ANVIL:
				case BEACON:
				case BED:
				case BREWING_STAND:
				case CAULDRON:
				case CHEST:
				case TRAPPED_CHEST:
				case ENDER_CHEST:
				case WORKBENCH:
				case ENCHANTMENT_TABLE:
				case FURNACE:
				case WOODEN_DOOR:
				case ACACIA_DOOR:
				case JUNGLE_DOOR:
				case DARK_OAK_DOOR:
				case SPRUCE_DOOR:
				case BIRCH_DOOR:
				case IRON_DOOR:
				case TRAP_DOOR:
				case IRON_TRAPDOOR:
				case FENCE_GATE:
				case ACACIA_FENCE_GATE:
				case JUNGLE_FENCE_GATE:
				case DARK_OAK_FENCE_GATE:
				case SPRUCE_FENCE_GATE:
				case BIRCH_FENCE_GATE:
				case HOPPER:
				case DISPENSER:
				case DROPPER:
				case LEVER:
				case WOOD_BUTTON:
				case STONE_BUTTON:
				case COMMAND:
				case ITEM_FRAME:
				case SIGN_POST:
				case WALL_SIGN: // 2 signs...
					blockUsable = true;
					break;
				case CAKE_BLOCK:
					mainOnly = true;
					break;
					//$CASES-OMITTED$
				default:
					blockUsable = false;
			}
		} else if (entity != null) {
			switch (entity.getType()) {
				case ITEM_FRAME:
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
				if (offHand.getType() != Material.AIR) return false;
				if (mainHand.getType() != Material.AIR) return true;
				//Skript.info("Sneak checks didn't pass.");
			} else { // When not sneaking, main hand is ALWAYS used
				return false;
			}
		} else if (mainOnly) {
			return false;
		}
		
		//Skript.info("Check for usable items...");
		if (mainUsable) return false;
		if (offUsable) return true;
		//Skript.info("No hand has usable item");
		
		// Still not returned?
		if (mainHand.getType() != Material.AIR) return false;
		//Skript.info("Main hand is not item.");
		if (offHand.getType() != Material.AIR) return true;
		
		//Skript.info("Final return!");
		return false; // Both hands are AIR material!
	}
	
}