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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class StructVariables extends Structure {

	static {
		Skript.registerStructure(StructVariables.class, "variables");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode node) {
		// TODO allow to make these override existing variables
		node.convertToEntries(0, "=");
		for (Node n : node) {
			if (!(n instanceof EntryNode)) {
				Skript.error("Invalid line in variables section");
				continue;
			}
			String name = n.getKey().toLowerCase(Locale.ENGLISH);
			if (name.startsWith("{") && name.endsWith("}"))
				name = "" + name.substring(1, name.length() - 1);
			String var = name;
			name = StringUtils.replaceAll(name, "%(.+)?%", m -> {
				if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
					Skript.error("'" + var + "' is not a valid name for a default variable");
					return null;
				}
				ClassInfo<?> ci = Classes.getClassInfoFromUserInput("" + m.group(1));
				if (ci == null) {
					Skript.error("Can't understand the type '" + m.group(1) + "'");
					return null;
				}
				return "<" + ci.getCodeName() + ">";
			});
			if (name == null) {
				continue;
			} else if (name.contains("%")) {
				Skript.error("Invalid use of percent signs in variable name");
				continue;
			}
			if (Variables.getVariable(name, null, false) != null)
				continue;
			Object o;
			ParseLogHandler log = SkriptLogger.startParseLogHandler();
			try {
				o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
				if (o == null) {
					log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
					continue;
				}
				log.printLog();
			} finally {
				log.stop();
			}
			ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
			if (ci.getSerializer() == null) {
				Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
				continue;
			} else if (ci.getSerializeAs() != null) {
				ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
				if (as == null) {
					assert false : ci;
					continue;
				}
				o = Converters.convert(o, as.getC());
				if (o == null) {
					Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
					continue;
				}
			}
			Variables.setVariable(name, o, null, false);
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "variables";
	}

}
