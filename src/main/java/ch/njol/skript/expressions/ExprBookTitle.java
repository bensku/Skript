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

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.eclipse.jdt.annotation.Nullable;

@Name("Book Title")
@Description("The title of a book.")
@Examples({"on book sign:",
			"\tmessage \"Book Title: %title of event-item%\""})
@Since("2.2-dev31")
public class ExprBookTitle extends SimplePropertyExpression<ItemType, String> {
	
	static {
		register(ExprBookTitle.class, String.class, "book (name|title)", "itemtypes");
	}
	
	private static final ItemType book = Aliases.javaItemType("book with text");
	
	@Override
	protected String getPropertyName() {
		return "title";
	}
	
	@Nullable
	@Override
	public String convert(ItemType item) {
		ItemStack itemStack = item.getRandom();
		assert itemStack != null;
		if (!book.isOfType(itemStack))
			return null;
		return ((BookMeta) itemStack.getItemMeta()).getTitle();
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE){
			return new Class<?>[]{String.class};
		}
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		ItemStack itemStack = getExpr().getSingle(e).getRandom();
		if (itemStack == null || !book.isOfType(itemStack))
			return;
		BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
		switch (mode){
			case SET:
				bookMeta.setTitle(delta == null ? "" : (String) delta[0]);
				break;
			case RESET:
			case DELETE:
				bookMeta.setTitle("");
				break;
				//$CASES-OMITTED$
			default:
				assert false;
		}
		itemStack.setItemMeta(bookMeta);
	}
}
