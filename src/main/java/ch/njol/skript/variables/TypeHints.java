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
package ch.njol.skript.variables;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This is used to manage local variable type hints.
 * 
 * <ul>
 * <li>EffChange adds then when local variables are set
 * <li>Variable checks them when parser tries to create it
 * <li>ScriptLoader clears hints after each section has been parsed
 * </ul>
 */
public class TypeHints {
	
	private static final Map<String, Class<?>> typeHints = new HashMap<>();
	
	public static void add(String variable, Class<?> hint) {
		if (!hint.equals(Object.class))
			typeHints.put(variable, hint);
	}
	
	@Nullable
	public static Class<?> get(String variable) {
		return typeHints.get(variable);
	}
	
	public static void clear() {
		typeHints.clear();
	}
}
