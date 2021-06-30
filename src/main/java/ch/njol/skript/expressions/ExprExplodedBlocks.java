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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import java.util.List;

import ch.njol.skript.aliases.ItemType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;
import ch.njol.skript.classes.Changer.ChangeMode;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Exploded Blocks")
@Description("Get all the blocks that were destroyed in an block/entity explode event (Can be set/removed from/deleted/added to)")
@Examples({"on block explode:",
	"\tadd exploded blocks to {exploded-blocks::*}",
	"\t",
	"\tloop exploded blocks:",
	"\t\tif loop-block is sand:",
	"\t\t\tset loop-block to stone",
	"\t",
	"\tset exploded blocks to blocks in radius 3 around event-location  # this will clear exploded blocks and set it to the specified blocks",
	"\tremove all chests from exploded blocks",
	"\tremove stone from exploded blocks # will remove only one block that equals to stone block type",
	"\tdelete exploded blocks",
	"\tadd blocks in radius 3 around event-location to exploded blocks",
	"\t",
	"on entity explode:",
	"\tremove all stone from exploded blocks"})
@Events("explode")
@Since("2.5")
public class ExprExplodedBlocks extends SimpleExpression<Block> {

	static {
		Skript.registerExpression(ExprExplodedBlocks.class, Block.class, ExpressionType.SIMPLE, "[the] exploded blocks");
	}
	
	boolean isEntity = false;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (getParser().isCurrentEvent(EntityExplodeEvent.class)) {
			isEntity = true;
		} else if (!getParser().isCurrentEvent(BlockExplodeEvent.class)) {
			Skript.error("Exploded blocks can only be retrieved from an entity/block explode event.");
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected Block[] get(Event e) {
		List<Block> blockList;
		
		if (isEntity)
			blockList = ((EntityExplodeEvent) e).blockList();
		else
			blockList = ((BlockExplodeEvent) e).blockList();
		
		return blockList.toArray(new Block[blockList.size()]);
	}
	
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case DELETE:
				return CollectionUtils.array(Block[].class);
			case REMOVE:
			case REMOVE_ALL:
				return CollectionUtils.array(Block[].class, ItemType[].class);
			case RESET:
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		List<Block> blocks;
		if (isEntity)
			blocks = ((EntityExplodeEvent) e).blockList();
		else
			blocks = ((BlockExplodeEvent) e).blockList();
		
		switch (mode) {
			case SET:
				assert delta != null;
				blocks.clear();
				for (Object de : delta) {
					if (de instanceof ItemType)
						break;
					
					if (((Block) de).getType() != Material.AIR) // Performance
						blocks.add((Block) de);
				}
				break;
				
			case DELETE:
				blocks.clear();
				break;
				
			case ADD:
				assert delta != null;
				for (Object de : delta) {
					if (de instanceof ItemType)
						break;
						
					if (((Block) de).getType() != Material.AIR) // Performance
						blocks.add((Block) de);
				}
				break;
				
			case REMOVE:
			case REMOVE_ALL: // ItemType here will allow `remove [all] %itemtypes% from exploded blocks` to remove all/specific amount of specific block type that matches an itemtype, a shortcut for looping
				assert delta != null;
				for (Object de : delta) {
					if (de instanceof ItemType) {						
						int loopLimit = ((ItemType) de).getAmount() == 0 ? blocks.size() : ((ItemType) de).getAmount();
						if (((ItemType) de).isAll() || mode == ChangeMode.REMOVE_ALL) // all %itemtype% OR REMOVE_ALL
							loopLimit = blocks.size();
						
						for (int i = 0; i < loopLimit; i++) {
							for (Block b : blocks) {
								if (b.getType() == ((ItemType) de).getMaterial()) {
									blocks.remove(b);
									break;
								}
							}
						}
						
					} else {
						blocks.removeIf(b -> b.equals((Block) de));
					}
				}
				break;
		}
	}
	
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "exploded blocks";
	}
	
}
