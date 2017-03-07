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

import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Click Action")
@Description("The <a href='../classes/#inventoryaction'>click action</a> of an inventory event. Please click on the link for more information.")
@Examples("click action is left mouse button")
@Since("2.2-dev20")
public class ExprClickAction extends EventValueExpression<ClickType> {

	static {
		Skript.registerExpression(ExprClickAction.class, ClickType.class, ExpressionType.SIMPLE, "[the] click action");
	}
	
	public ExprClickAction() {
		super(ClickType.class);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the click action";
	}
	
}