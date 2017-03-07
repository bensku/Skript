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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.effects.EffReturn;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.function.Functions.FunctionData;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.Variables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Peter Güttinger
 */
public class ScriptFunction<T> extends Function<T> {
	
	final Trigger trigger;
	
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public ScriptFunction(final String name, final Parameter<?>[] parameters, final SectionNode node, @Nullable final ClassInfo<T> returnType, final boolean single) {
		super(name, parameters, returnType, single);
		
		// here to allow recursion
		Functions.functions.put(name, new FunctionData(this));
		
		Functions.currentFunction = this;
		try {
			trigger = new Trigger(node.getConfig().getFile(), "function " + name, new SimpleEvent(), ScriptLoader.loadItems(node));
		} finally {
			Functions.currentFunction = null;
		}
	}
	
	private boolean returnValueSet = false;
	@Nullable
	private T[] returnValue = null;
	
	/**
	 * Should only be called by {@link EffReturn}.
	 * 
	 * @param e
	 * @param value
	 */
	public final void setReturnValue(final FunctionEvent e, final @Nullable T[] value) {
		assert !returnValueSet;
		returnValueSet = true;
		returnValue = value;
	}
	
	// REMIND track possible types of local variables (including undefined variables) (consider functions, commands, and EffChange) - maybe make a general interface for this purpose
	// REM: use patterns, e.g. {_a%b%} is like "a.*", and thus subsequent {_axyz} may be set and of that type.
	@Override
	@Nullable
	public T[] execute(final FunctionEvent e, final Object[][] params) {
		for (int i = 0; i < parameters.length; i++) {
			final Parameter<?> p = parameters[i];
			final Object[] val = params[i];
			if (p.single) {
				Variables.setVariable(p.name, val[0], e, true);
			} else {
				for (int j = 0; j < val.length; j++) {
					Variables.setVariable(p.name + "::" + (j + 1), val[j], e, true);
				}
			}
		}
		trigger.execute(e);
		returnValueSet = false;
		return returnValue;
	}
	
}
