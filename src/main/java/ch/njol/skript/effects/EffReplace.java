/*
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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Replace")
@Description("Replaces all occurrences of a given text with another text. Please note that you can only change variables and a few expressions, e.g. a <a href='../expressions/#ExprMessage'>message</a> or a line of a sign.")
@Examples({"replace \"<item>\" in {textvar} with \"%item%\"",
        "replace every \"&\" with \"§\" in line 1",
        "# The following acts as a simple chat censor, but it will e.g. censor mass, hassle, assassin, etc. as well:",
        "on chat:",
        "	replace all \"fuck\", \"bitch\" and \"ass\" with \"****\" in the message"})
@Since("2.0")
// TODO add 'replace all <items> in <inventories> with <item>'
public class EffReplace extends Effect {
    static {
        Skript.registerEffect(EffReplace.class,
                "replace (all|every|) %strings% in %string% with %string%",
                "replace (all|every|) %strings% with %string% in %string%");
    }

    @SuppressWarnings("null")
    private Expression<String> haystack, needles, replacement;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        haystack = (Expression<String>) exprs[1 + matchedPattern];
        if (!ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
            Skript.error(haystack + " cannot be changed and can thus not have parts replaced.");
            return false;
        }
        needles = (Expression<String>) exprs[0];
        replacement = (Expression<String>) exprs[2 - matchedPattern];
        return true;
    }

    @Override
    protected void execute(final Event e) {
        String h = haystack.getSingle(e);
        final String[] ns = needles.getAll(e);
        final String r = replacement.getSingle(e);
        if (h == null || ns.length == 0 || r == null)
            return;
        for (final String n : ns) {
            assert n != null;
            h = StringUtils.replace(h, n, r, SkriptConfig.caseSensitive.value());
        }
        haystack.change(e, new String[]{h}, ChangeMode.SET);
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return "replace " + needles.toString(e, debug) + " in " + haystack.toString(e, debug) + " with " + replacement.toString(e, debug);
    }

}
