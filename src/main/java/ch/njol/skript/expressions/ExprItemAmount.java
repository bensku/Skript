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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Item Amount")
@Description("The amount of an <a href='classes.html#itemstack'>item stack</a>.")
@Examples("send \"You have got %item amount of player's tool% %player's tool% in your hand!\" to player")
@Since("2.2-dev24")
public class ExprItemAmount extends SimplePropertyExpression<ItemType, Number>{
	
    static {
        register(ExprItemAmount.class, Number.class, "item[[ ]stack] (amount|size|number)", "itemtypes");
    }
    
    @Override
	public Class<Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "item[[ ]stack] (amount|size|number)";
	}
	
	@Override
	public Number convert(final ItemType item) {
		return item.getAmount();
	}
	
	@Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Number.class) : null;
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
    	int amount = delta != null ? ((Number) delta[0]).intValue() : 0;
        switch (mode) {
            case ADD:
            	for (ItemType item : getExpr().getArray(event))
            		item.setAmount(item.getAmount() + amount);
                break;
            case SET:
            	for (ItemType item : getExpr().getArray(event))
            		item.setAmount(amount);
                break;
            case REMOVE:
            	for (ItemType item : getExpr().getArray(event))
            		item.setAmount(item.getAmount() - amount);
                break;
            case REMOVE_ALL:
            case RESET:
			case DELETE:
				for (ItemType item : getExpr().getArray(event))
            		item.setAmount(1);
				break;
        }
    }

}
