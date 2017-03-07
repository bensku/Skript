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

import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.registrations.Converters;

/**
 * @author Peter Güttinger
 */
@Name("Type of")
@Description("The type of a block/item or entity. The type of an item is only it's id and data value, i.e. it ignores the amount, enchantments etc., and the type of an entity is e.g. 'wolf' or 'player'.")
@Examples({"on rightclick on an entity:",
		"	message \"This is a %type of clicked entity%!\""})
@Since("1.4")
public class ExprTypeOf extends SimplePropertyExpression<Object, Object> {
	static {
		register(ExprTypeOf.class, Object.class, "type", "entitydatas/itemstacks");
	}
	
	@Override
	protected String getPropertyName() {
		return "type";
	}
	
	@Override
	@Nullable
	public Object convert(final Object o) {
		if (o instanceof EntityData) {
			return ((EntityData<?>) o).getSuperType();
		} else if (o instanceof ItemStack) {
			return new ItemStack(((ItemStack) o).getType(), 1, ((ItemStack) o).getDurability());
		}
		assert false;
		return null;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return EntityData.class.isAssignableFrom(getExpr().getReturnType()) ? EntityData.class
				: ItemStack.class.isAssignableFrom(getExpr().getReturnType()) ? ItemStack.class : Object.class;
	}
	
	@Override
	@Nullable
	protected <R> ConvertedExpression<Object, ? extends R> getConvertedExpr(final Class<R>... to) {
		if (!Converters.converterExists(EntityData.class, to) && !Converters.converterExists(ItemStack.class, to))
			return null;
		return super.getConvertedExpr(to);
	}
}
