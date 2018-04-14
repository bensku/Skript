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
package ch.njol.skript.events.bukkit;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Config;
import ch.njol.util.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * If {@link ScriptLoader#callPreLoadEvent} is true,
 * this event is called before a script starts
 * loading via {@link ScriptLoader#loadScript(Config)}
 * or one of it's overloads.
 */
public class PreScriptLoadEvent extends Event {

    private Config script;

    public PreScriptLoadEvent(Config script) {
        Validate.notNull(script);
        this.script = script;
    }

    private static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * This is usually, but may not be, the same
     * as {@link ScriptLoader#currentScript}
     * @return The {@link Config} of the loading script
     */
    public Config getScript() {
        return script;
    }

}
