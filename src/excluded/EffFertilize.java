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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import net.minecraft.server.Item;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

/**
 * 
 * @author Peter Güttinger
 */
public class EffFertilize extends Effect {
	
	static {
//		Skript.registerEffect(EffFertilize.class, "fertili(z|s)e %blocks%");
	}
	
	private Expression<Block> blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final int isDelayed, final ParseResult parser) {
		blocks = (Expression<Block>) vars[0];
		if (!Skript.isRunningCraftBukkit()) {
			Skript.error("The fertilize effect can only be used with CraftBukkit", ErrorQuality.);
return false;
		}
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		for (final Block b : blocks.getArray(e)) {
			Item.INK_SACK.interactWith(CraftItemStack.createNMSItemStack(new ItemStack(Material.INK_SACK, 1, Color.WHITE.getDye())), null, ((CraftWorld) b.getWorld()).getHandle(), b.getX(), b.getY(), b.getZ(), 0, 0, 0, 0);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "fertilize " + blocks.toString(e, debug);
	}
	
}
