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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Repeat Effect")
@Description("Runs an effect x number of times.")
@Examples({"repeat effect send \"<red>Server will restart in 1 minute!\" to all players 3 times"})
@Since("INSERT VERSION")
public class EffRepeat extends Effect  {

	static {
		Skript.registerEffect(EffRepeat.class, "(repeat|run) [effect] <.+> %number% times");
	}

	@SuppressWarnings("null")
	private Effect effect;

	@Nullable
	private Expression<Number> amount;

	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String eff = parseResult.regexes.get(0).group();
		effect = Effect.parse(eff, "Can't understand this effect: " + eff);
		amount = (Expression<Number>) exprs[0];
		return effect != null;
	}

	@Override
	protected void execute(Event e) {
		int max = 1;

		if (amount != null) {
			Number n = amount.getSingle(e);
			if (n == null)
				return;
			max = n.intValue();
		}
		for (int i=1; i<=max; i++) {
			effect.run(e);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "repeat " + effect.toString(e, debug) + " " + (amount != null ? amount.toString(e, debug) : "") + " times";
	}
}
