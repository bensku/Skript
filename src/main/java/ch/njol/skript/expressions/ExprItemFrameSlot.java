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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.DroppedItemSlot;
import ch.njol.skript.util.slot.ItemFrameSlot;
import ch.njol.skript.util.slot.Slot;

@Name("Item of an Entity")
@Description("An item associated with an entity. For dropped item entities, it gets, obviously, the item that was dropped. "
		+ "For item frames, the item inside the frame is returned. Other entities do not have items associated with them.")
@Examples("")
@Since("2.2-dev35, 2.2-dev36 (improved)")
public class ExprItemFrameSlot extends SimplePropertyExpression<Entity, Slot> {
	
	static {
		register(ExprItemFrameSlot.class, Slot.class, "item", "entities");
	}
	
	@Override
	@Nullable
	public Slot convert(Entity e) {
		if (e instanceof ItemFrame)
			return new ItemFrameSlot((ItemFrame) e);
		else if (e instanceof Item)
			return new DroppedItemSlot((Item) e);
		return null; // Other entities don't have associated items
	}

	@Override
	protected String getPropertyName() {
		return "item of entity";
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
}
