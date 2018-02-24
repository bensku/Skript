/*
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
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Inventory Holder/Viewers/Rows")
@Description("Gets the rows/size/viewers/holder of an inventory.")
@Examples({"event-inventory's amount of rows",
		"holder of player's top inventory",
		"{_inventory}'s viewers"})
@Since("2.2-dev34")
public class ExprInventoryInfo extends PropertyExpression<Inventory, Object> {
	private final static int HOLDER = 1, VIEWERS = 2, ROWS = 3;

	static {
		PropertyExpression.register(ExprInventoryInfo.class, Object.class,
				"(" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows)", "inventories");
	}

	private int type;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		setExpr((Expression<? extends Inventory>) exprs[0]);
		type = parseResult.mark;
		return true;
	}

	@Override
	protected Object[] get(final Event e, final Inventory[] source) {
		List<Object> info = new ArrayList<>();
		for (Inventory inv : source) {
			switch (type) {
				case HOLDER:
					info.add(inv.getHolder());
				case VIEWERS:
					info.addAll(inv.getViewers());
				default:
					info.add(inv.getSize() / 9);
			}
		}
		return info.toArray(new Object[info.size()]);
	}

	@Override
	public Class<?> getReturnType() {
		return type == HOLDER ? InventoryHolder.class :
				type == VIEWERS ? Player.class : Number.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return type == HOLDER ? "inventory holder of " :
				type == VIEWERS ? "inventory viewers of " : "inventory rows of " + getExpr().toString(e, debug);
	}
}
