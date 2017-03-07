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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.CursorSlot;
import ch.njol.skript.util.EquipmentSlot;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;

/**
 * Cursor item slot is not actually an inventory slot, but an item which player
 * has in cursor when any inventory is open.
 * @author bensku
 */
@Name("Cursor Slot")
@Description("The item which player has on their cursor. This slot is always empty if player has no inventories open.")
@Examples({"cursor slot of player is dirt",
		"set cursor slot of player to 64 diamonds"})
@Since("2.2-dev17")
public class ExprCursorSlot extends SimplePropertyExpression<Player, Slot> {
	
	static {
		register(ExprCursorSlot.class, Slot.class, "cursor slot", "players");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		return true;
	}
	
	@Override
	@Nullable
	public Slot convert(final Player p) {
		return new CursorSlot(p);
	}

	@Override
	protected String getPropertyName() {
		return "cursor";
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
}
