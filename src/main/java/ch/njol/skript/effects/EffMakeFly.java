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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Fly")
@Description("Forces a player to start/stop flying.")
@Examples({"make player fly", "force all players to stop flying"})
@Since("2.2-dev34")
public class EffMakeFly extends Effect {

    static {
        if (Skript.methodExists(Player.class, "setFlying", boolean.class)) {
            Skript.registerEffect(EffMakeFly.class, "force %players% to [(1¦stop|0¦start)] fly[ing]",
                                                "make %players% (1¦stop|0¦start) flying",
                                                "make %players% fly");
        }
    }

    @SuppressWarnings("null")
    private Expression<Player> players;

    private boolean flying;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        flying = parseResult.mark == 0;
        return true;
    }

    @Override
    protected void execute(Event e) {
        for (Player player : players.getArray(e)) {
           player.setFlying(flying);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "make " + players.toString(e, debug) + (flying ? " start " : " stop ") + "flying";
    }


}
