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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hotbar Slot")
@Description({"The slot number of the currently selected hotbar slot. Starts at 0."})
@Examples({"message \"%player's current hotbar slot%\"",
            "set player's selected hotbar slot to 4"})
@Since("INSERT VERSION")
public class ExprHotbarSlot extends SimplePropertyExpression<Player, Integer> {

    static {
        register(ExprHotbarSlot.class, Integer.class, "[([currently] selected|current)] hotbar slot", "players");
    }

    @Override
    public Integer convert(Player player) {
        return player.getInventory().getHeldItemSlot();
    }

    @Override
    protected String getPropertyName() {
        return "hotbar slot";
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET)
            return new Class[] {
                Number.class,
            };
        return null;
    }

    @Override
    @SuppressWarnings("null")
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        int slot = ((Number) delta[0]).intValue();
        if (slot < 0 || slot > 8)
            return;

        for (Player player : getExpr().getArray(e)) {
            player.getInventory().setHeldItemSlot(slot);
        }

    }
}
