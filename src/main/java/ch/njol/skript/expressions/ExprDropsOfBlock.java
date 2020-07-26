/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprDropsOfBlock extends SimpleExpression<ItemStack> {
	
	static {
		Skript.registerExpression(ExprDropsOfBlock.class, ItemStack.class, ExpressionType.COMBINED, "drops of %block% [(using|with) %-itemstack% [as %-entity%]]", "%block%'s drops [(using|with) %-itemstack% [as %-entity%]]");
	}
	
	@SuppressWarnings("null")
	private Expression<Block> block;
	@SuppressWarnings("null")
	private Expression<ItemStack> item;
	@SuppressWarnings("null")
	private Expression<Entity> entity;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		block = (Expression<Block>) exprs[0];
		item = (Expression<ItemStack>) exprs[1];
		entity = (Expression<Entity>) exprs[2];
		return true;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	protected ItemStack[] get(Event e) {
		Block block = this.block.getSingle(e);
		
		if (block != null) {
			if (this.item == null) {
				return block.getDrops().toArray(new ItemStack[block.getDrops().size()]);
			} else if (entity != null) {
				ItemStack item = this.item.getSingle(e);
				Entity entity = this.entity.getSingle(e);
				return block.getDrops(item, entity).toArray(new ItemStack[block.getDrops().size()]);
			} else {
				ItemStack item = this.item.getSingle(e);
				return block.getDrops(item).toArray(new ItemStack[block.getDrops().size()]);
			}
		}
		return null;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "drops of " + block.toString(e, debug);
	}
	
}
