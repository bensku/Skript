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

import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Data Value")
@Description({"The data value of an item.",
		"You usually don't need this expression as you can check and set items with aliases easily, " +
				"but this expression can e.g. be used to \"add 1 to data of &lt;item&gt;\", e.g. for cycling through all wool colours."})
@Examples({"add 1 to the data value of the clicked block"})
@Since("1.2")
public class ExprDurability extends SimplePropertyExpression<ItemType, Object> {
	
	static {
		register(ExprDurability.class, Object.class, "((data|damage)[s] [value[s]]|durabilit(y|ies))", "itemtypes");
	}
	
	private final static boolean USING_NEW_BLOCK_COMPAT = Skript.isRunningMinecraft(1, 13);
	
	@SuppressWarnings("deprecation")
	@Override
	@Nullable
	public Object convert(final ItemType i) {
		if (i.hasBlock()) {
			BlockValues bv = i.getTypes().get(0).getBlockValues();
			return (bv == null) ? null : ((USING_NEW_BLOCK_COMPAT) ? ((BlockData) bv.getData()).getAsString() : bv.getData());
		}
		ItemStack stack = i.getRandom();
		return (stack == null) ? null : stack.getDurability();
	}
	
	@Override
	public String getPropertyName() {
		return "data";
	}
	
	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return (mode == ChangeMode.REMOVE_ALL) ? null : CollectionUtils.array(Object.class);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (delta == null) return;
		Object[] arr = getExpr().getArray(e);
		ItemType[] newarr = new ItemType[arr.length];
		if (delta[0] instanceof String && USING_NEW_BLOCK_COMPAT) {
			String a = (String) delta[0];
			assert a != null;
			for (int i = 0; i < arr.length; i++) {
				ItemType item = (ItemType) arr[i];
				ItemType newitem;
				switch (mode) {
					case SET:
						newitem = new ItemType(item.getMaterial(), a);
						newitem.setAmount(item.getAmount());
						break;
					case DELETE:
					case RESET:
						newitem = new ItemType(item.getMaterial());
						newitem.setAmount(item.getAmount());
						break;
					case REMOVE_ALL:
						assert false;
					default:
						newitem = item;
				}
				newarr[i] = newitem;
			}
		} else if (delta[0] instanceof Number) {
			int a = ((Number) delta[0]).intValue();
			for (int i = 0; i < arr.length; i++) {
				ItemStack stack = ((ItemType) arr[i]).getRandom();
				assert stack != null;
				switch (mode) {
					case REMOVE:
						stack.setDurability((short) (stack.getDurability() - a));
						break;
					case ADD:
						stack.setDurability((short) (stack.getDurability() + a));
						break;
					case SET:
						stack.setDurability((short) a);
						break;
					case DELETE:
					case RESET:
						stack.setDurability((short) 0);
						break;
					case REMOVE_ALL:
						assert false;
				}
				newarr[i] = new ItemType(stack);
			}
		} else {
			return;
		}
		change(e, newarr, ChangeMode.SET);
	}
	
}
