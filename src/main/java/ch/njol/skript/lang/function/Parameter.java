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
package ch.njol.skript.lang.function;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;

public final class Parameter<T> {
	
	final String name;
	
	final ClassInfo<T> type;
	
	@Nullable
	final Expression<? extends T> def;
	
	final boolean single;
	
	@SuppressWarnings("null")
	public Parameter(final String name, final ClassInfo<T> type, final boolean single, final @Nullable Expression<? extends T> def) {
		this.name = name != null ? name.toLowerCase() : null;
		this.type = type;
		this.def = def;
		this.single = single;
	}
	
	public ClassInfo<T> getType() {
		return type;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Parameter<T> newInstance(final String name, final ClassInfo<T> type, final boolean single, final @Nullable String def) {
		if (!Variable.isValidVariableName(name, true, false)) {
			Skript.error("An argument's name must be a valid variable name.");
			return null;
		}
		Expression<? extends T> d = null;
		if (def != null) {
//			if (def.startsWith("%") && def.endsWith("%")) {
//				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
//				try {
//					d = new SkriptParser("" + def.substring(1, def.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.FUNCTION_DEFAULT).parseExpression(type.getC());
//					if (d == null) {
//						log.printErrors("Can't understand this expression: " + def + "");
//						return null;
//					}
//					log.printLog();
//				} finally {
//					log.stop();
//				}
//			} else {
			final RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				if (type.getC() == String.class) {
					if (def.startsWith("\"") && def.endsWith("\"")) {
						d = (Expression<? extends T>) VariableString.newInstance("" + def.substring(1, def.length() - 1));
					} else {
						if (def.contains(" ")) // Warn about whitespace in unquoted string
							Skript.warning("'" + def + "' contains spaces and is unquoted, which is discouraged");
						d = (Expression<? extends T>) new SimpleLiteral<>(def, false);
					}
				} else {
					d = new SkriptParser(def, SkriptParser.PARSE_LITERALS, ParseContext.DEFAULT).parseExpression(type.getC());
				}
				if (d == null) {
					log.printErrors("'" + def + "' is not " + type.getName().withIndefiniteArticle());
					return null;
				}
				log.printLog();
			} finally {
				log.stop();
			}
//			}
		}
		return new Parameter<>(name, type, single, d);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + ": " + Utils.toEnglishPlural(type.getCodeName(), !single) + (def != null ? " = " + def.toString(null, true) : "");
	}
	
}
