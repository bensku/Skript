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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class StructFunction extends PreloadingStructure {

	static {
		Skript.registerStructure(StructFunction.class, "function <.+>");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode node) {
		getParser().setCurrentEvent("function", FunctionEvent.class);

		Functions.loadSignature(getParser().getCurrentScript().getFileName(), node);

		getParser().deleteCurrentEvent();
		return true;
	}

	@Override
	public void init(SectionNode node) {
		getParser().setCurrentEvent("function", FunctionEvent.class);

		Function<?> function = Functions.loadFunction(node);
		if (function != null)
			getParser().getScriptInfo().functions++;

		getParser().deleteCurrentEvent();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "function";
	}

}
