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
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@SuppressWarnings({"unchecked"})
@Name("Item Amount")
@Description("Gets the amount of an <a href='../classes/#itemstack'>item stack</a>.")
@Examples("send \"You have got %item amount of player's tool% %player's tool% in your hand !\" to player")
@Since("2.2-dev24")
public class ExprItemAmount extends SimpleExpression<Number>{
	
    static {
        Skript.registerExpression(ExprItemAmount.class, Number.class, ExpressionType.PROPERTY, "item[[ ]stack] (amount|size|number) of %itemstack%");
    }
    
    @SuppressWarnings("null")
	private Expression<ItemStack> itemStackExpression;

	@SuppressWarnings("null")
	@Override
    public boolean init(Expression<?>[] expr, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        itemStackExpression = (Expression<ItemStack>) expr[0];
        return true;
    }

    @Override
    protected Number[] get(Event e) {
    	ItemStack stack = itemStackExpression.getSingle(e);
        return stack != null ? new Number[] {stack.getAmount()} : new Number[] {0};
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
    	ItemStack stack = itemStackExpression.getSingle(e);
    	int newAmount = delta != null ? ((Number) delta[0]).intValue() : 0;
        if (stack != null) {
            switch (mode) {
                case ADD:
                    stack.setAmount(newAmount + stack.getAmount());
                    break;
                case SET:
                    stack.setAmount(newAmount);
                    break;
                case REMOVE:
                    stack.setAmount(stack.getAmount() - newAmount);
                    break;
                case RESET:
                    stack.setAmount(1);
                    break;
					//$CASES-OMITTED$
				default:
					break;
            }
        }
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return (mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.DELETE) ? CollectionUtils.array(Number.class) : null;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "item amount";
    }
}
