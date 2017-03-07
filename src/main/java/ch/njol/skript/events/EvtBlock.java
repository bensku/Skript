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

import java.lang.invoke.MethodHandle;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class EvtBlock extends SkriptEvent {
	
	static {
		// TODO 'block destroy' event for any kind of block destruction (player, water, trampling, fall (sand, toches, ...), etc) -> BlockPhysicsEvent?
		// REMIND attacking an item frame first removes its item; include this in on block damage?
		Skript.registerEvent("Break / Mine", EvtBlock.class, new Class[] {BlockBreakEvent.class, PlayerBucketFillEvent.class, Skript.isRunningMinecraft(1, 4, 3) ? HangingBreakEvent.class : HangingBreakEvent.class}, "[block] (break[ing]|1¦min(e|ing)) [[of] %itemtypes%]")
				.description("Called when a block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger.")
				.examples("on mine", "on break of stone", "on mine of any ore")
				.since("1.0 (break), <i>unknown</i> (mine)");
		Skript.registerEvent("Burn", EvtBlock.class, BlockBurnEvent.class, "[block] burn[ing] [[of] %itemtypes%]")
				.description("Called when a block is destroyed by fire.")
				.examples("on burn", "on burn of wood, fences, or chests")
				.since("1.0");
		Skript.registerEvent("Place", EvtBlock.class, new Class[] {BlockPlaceEvent.class, PlayerBucketEmptyEvent.class, Skript.isRunningMinecraft(1, 4, 3) ? HangingPlaceEvent.class : HangingPlaceEvent.class}, "[block] (plac(e|ing)|build[ing]) [[of] %itemtypes%]")
				.description("Called when a player places a block.")
				.examples("on place", "on place of a furnace, workbench or chest")
				.since("1.0");
		Skript.registerEvent("Fade", EvtBlock.class, BlockFadeEvent.class, "[block] fad(e|ing) [[of] %itemtypes%]")
				.description("Called when a block 'fades away', e.g. ice or snow melts.")
				.examples("on fade of snow or ice")
				.since("1.0");
		Skript.registerEvent("Form", EvtBlock.class, BlockFormEvent.class, "[block] form[ing] [[of] %itemtypes%]")
				.description("Called when a block is created, but not by a player, e.g. snow forms due to snowfall, water freezes in cold biomes, or a block spreads (see <a href='#spread'>spread event</a>).")
				.examples("on form of snow", "on form of a mushroom")
				.since("1.0");
	}
	
	@Nullable
	private Literal<ItemType> types;
	
	private boolean mine = false;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<ItemType>) args[0];
		mine = parser.mark == 1;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(final Event e) {
		if (mine && e instanceof BlockBreakEvent) {
			if (((BlockBreakEvent) e).getBlock().getDrops(((BlockBreakEvent) e).getPlayer().getItemInHand()).isEmpty())
				return false;
		}
		if (types == null)
			return true;
		int id = 0;
		short durability = 0;
		if (e instanceof BlockEvent) {
			id = ((BlockEvent) e).getBlock().getTypeId();
			durability = ((BlockEvent) e).getBlock().getData();
		} else if (e instanceof BlockFormEvent) {
			id = ((BlockFormEvent) e).getNewState().getTypeId();
			durability = ((BlockFormEvent) e).getNewState().getRawData();
		} else if (e instanceof PlayerBucketFillEvent) {
			id = ((PlayerBucketEvent) e).getBlockClicked().getRelative(((PlayerBucketEvent) e).getBlockFace()).getTypeId();
			durability = ((PlayerBucketEvent) e).getBlockClicked().getRelative(((PlayerBucketEvent) e).getBlockFace()).getData();
		} else if (e instanceof PlayerBucketEmptyEvent) {
			id = ((PlayerBucketEmptyEvent) e).getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER.getId() : Material.STATIONARY_LAVA.getId();
			durability = 0;
		} else if (Skript.isRunningMinecraft(1, 4, 3) && e instanceof HangingEvent) {
			final EntityData<?> d = EntityData.fromEntity(((HangingEvent) e).getEntity());
			return types.check(e, new Checker<ItemType>() {
				@Override
				public boolean check(final @Nullable ItemType t) {
					return t != null && Relation.EQUAL.is(DefaultComparators.entityItemComparator.compare(d, t));
				}
			});
		} else {
			assert false;
			return false;
		}
		
		//Hacky code to fix Java 8 compilation problems... TODO maybe use lambdas instead and break Java 7?
		final int idFinal = id;
		final short durFinal = durability;
		
		return types.check(e, new Checker<ItemType>() {
			@Override
			public boolean check(final @Nullable ItemType t) {
				return t != null && t.isOfType(idFinal, durFinal);
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "break/place/burn/fade/form of " + Classes.toString(types);
	}
	
}
