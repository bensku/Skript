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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtAdvancement;
import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class EvtAdvancement extends SimpleEvent {
    static {
        Skript.registerEvent("Advancement done", EvtAdvancement.class, PlayerAdvancementDoneEvent.class, "[on] advancement (done|finish)")
                .description("Called when a player does an advancement")
                .examples("on advancement done:",
                        "\tsend \"You did an advancement\" to player")
                .since("INSERT VERSION");
    }
}
}
