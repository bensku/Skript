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
package ch.njol.skript.expressions;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@Name("Drops")
@Description("Only works in block break and death events. Holds the drops of the dying creature. Drops can be prevented by removing them with \"remove ... from drops\", e.g. \"remove all pickaxes from the drops\", or \"clear drops\" if you don't want any drops at all. " +
		"In break events, drops can only be cleared, and is only available in 1.12+")
@Examples({"clear drops",
		"remove 4 planks from the drops", "on break of diamond ore:", "\tclear drops"})
@Since("1.0, 2.4 (block break event drops)")
@Events("break / mine, death")
public class ExprDrops extends SimpleExpression<ItemStack> {
	static {
		Skript.registerExpression(ExprDrops.class, ItemStack.class, ExpressionType.SIMPLE, "[the] drops");
	}
	
	private final boolean BREAK_DROPS = Skript.methodExists(BlockBreakEvent.class, "setDropItems", boolean.class);
	
	@SuppressWarnings("null")
	private Kleenean delayed;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (BREAK_DROPS) {
			if (!ScriptLoader.isCurrentEvent(EntityDeathEvent.class, BlockBreakEvent.class)) {
				Skript.error("The expression 'drops' can only be used in block break and death events", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		} else {
			if (!ScriptLoader.isCurrentEvent(EntityDeathEvent.class)) {
				Skript.error("The expression 'drops' can only be used in death events", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		delayed = isDelayed;
		return true;
	}
	
	@Override
	@Nullable
	protected ItemStack[] get(final Event e) {
		if (!(e instanceof EntityDeathEvent))
			return new ItemStack[0];
		return ((EntityDeathEvent) e).getDrops().toArray(new ItemStack[0]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return null;
		if (delayed.isTrue()) {
			Skript.error("Can't change the drops anymore after the event has already passed");
			return null;
		}
		return CollectionUtils.array(ItemType[].class, Inventory[].class, Experience[].class);
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public void change(final Event e, final @Nullable Object[] deltas, final ChangeMode mode) {
		assert mode != ChangeMode.RESET;
		if (e instanceof BlockBreakEvent) {
			if (mode == ChangeMode.DELETE) {
				((BlockBreakEvent) e).setDropItems(false);
				return;
			}
		}
		if (!(e instanceof EntityDeathEvent)) {
			assert false;
			return;
		}
		
		final List<ItemStack> drops = ((EntityDeathEvent) e).getDrops();
		if (mode == ChangeMode.DELETE) {
			drops.clear();
			return;
		}
		boolean cleared = false;
		
		assert deltas != null;
		for (final Object delta : deltas) {
			if (delta instanceof Experience) {
				if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.REMOVE && ((Experience) delta).getInternalXP() == -1) {
					((EntityDeathEvent) e).setDroppedExp(0);
				} else if (mode == ChangeMode.SET) {
					((EntityDeathEvent) e).setDroppedExp(((Experience) delta).getXP());
				} else {
					((EntityDeathEvent) e).setDroppedExp(Math.max(0, ((EntityDeathEvent) e).getDroppedExp() + (mode == ChangeMode.ADD ? 1 : -1) * ((Experience) delta).getXP()));
				}
			} else {
				switch (mode) {
					case SET:
						if (!cleared) {
							drops.clear();
							cleared = true;
						}
						//$FALL-THROUGH$
					case ADD:
						if (delta instanceof Inventory) {
							for (final ItemStack is : new IteratorIterable<>(((Inventory) delta).iterator())) {
								if (is != null)
									drops.add(is);
							}
						} else {
							((ItemType) delta).addTo(drops);
						}
						break;
					case REMOVE:
					case REMOVE_ALL:
						if (delta instanceof Inventory) {
							for (final ItemStack is : new IteratorIterable<>(((Inventory) delta).iterator())) {
								if (is == null)
									continue;
								if (mode == ChangeMode.REMOVE)
									new ItemType(is).removeFrom(drops);
								else
									new ItemType(is).removeAll(drops);
							}
						} else {
							if (mode == ChangeMode.REMOVE)
								((ItemType) delta).removeFrom(drops);
							else
								((ItemType) delta).removeAll(drops);
						}
						break;
					case DELETE:
					case RESET:
						assert false;
				}
			}
		}
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return "the drops";
		return Classes.getDebugMessage(getAll(e));
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
}
