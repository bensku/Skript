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
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import net.kyori.adventure.text.Component;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import org.eclipse.jdt.annotation.Nullable;


@Name("Advancement message")
@Description("Sets the advancement message for on advancement: event")
@Examples("set the advancement message to \"%event-player% did an advancement!\"")
@Since("INSERT VERSION")
public class ExprAdvancementMessage extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprAdvancementMessage.class, String.class, ExpressionType.SIMPLE, "[the] advancement message");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (ScriptLoader.isCurrentEvent(PlayerAdvancementDoneEvent.class)) {
            return true;
        }
        Skript.error("Cannot use the advancement message outside an on advancement done event", ErrorQuality.SEMANTIC_ERROR);
        return false;
    }
    @Override
    @Nullable
    protected String[] get(Event e) {
        return new String[] {((PlayerAdvancementDoneEvent) e).message().toString()};
    }

    @Override
    public Class<?>[] acceptChange(final ChangeMode mode) {
        if (mode == ChangeMode.SET) {
            return CollectionUtils.array(String.class);
        }
        return null;
    }
    @Override
    public void change(final Event e, @Nullable Object[] delta, final ChangeMode mode) {
        if (mode == ChangeMode.SET) {
            String msg = delta[0].toString();
            Component message = Component.text(msg);
            ((PlayerAdvancementDoneEvent) e).message(message);
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "the advancement message";
    }
}
