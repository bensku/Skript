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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class FunctionReference<T> {
	
	final String functionName;
	
	@Nullable
	private Function<? extends T> function;
	@Nullable
	private Signature<? extends T> signature;
	
	private boolean singleUberParam;
	private final Expression<?>[] parameters;
	
	private boolean single;
	@Nullable
	private final Class<? extends T>[] returnTypes;
	
	@Nullable
	private final Node node;
	@Nullable
	public final File script;
	
	public FunctionReference(final String functionName, final @Nullable Node node, @Nullable final File script, @Nullable final Class<? extends T>[] returnTypes, final Expression<?>[] params) {
		this.functionName = functionName;
		this.node = node;
		this.script = script;
		this.returnTypes = returnTypes;
		parameters = params;
	}
	
	@SuppressWarnings("unchecked")
	public boolean validateFunction(final boolean first) {
		Skript.debug("Validating function " + functionName);
		final Signature<?> sign = Functions.getSignature(functionName);
		final Function<?> newFunc = Functions.getFunction(functionName);
		SkriptLogger.setNode(node);
		if (sign == null) {
			if (first)
				Skript.error("The function '" + functionName + "' does not exist.");
			else
				Skript.error("The function '" + functionName + "' was deleted or renamed, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
			return false;
		}
		
		final Class<? extends T>[] returnTypes = this.returnTypes;
		if (returnTypes != null) {
			final ClassInfo<?> rt = sign.returnType;
			if (rt == null) {
				if (first)
					Skript.error("The function '" + functionName + "' doesn't return any value.");
				else
					Skript.error("The function '" + functionName + "' was redefined with no return value, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
				return false;
			}
			if (!CollectionUtils.containsAnySuperclass(returnTypes, rt.getC())) {
				if (first)
					Skript.error("The returned value of the function '" + functionName + "', " + sign.returnType + ", is " + SkriptParser.notOfType(returnTypes) + ".");
				else
					Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
				return false;
			}
			if (first) {
				single = sign.single;
			} else if (single && !sign.single) {
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
				return false;
			}
		}
		
		// check number of parameters only if the function does not have a single parameter that accepts multiple values
		singleUberParam = sign.getMaxParameters() == 1 && !sign.getParameter(0).single;
		if (!singleUberParam) {
			if (parameters.length > sign.getMaxParameters()) {
				if (first) {
					if (sign.getMaxParameters() == 0)
						Skript.error("The function '" + functionName + "' has no arguments, but " + parameters.length + " are given."
								+ " To call a function without parameters, just write the function name followed by '()', e.g. 'func()'.");
					else
						Skript.error("The function '" + functionName + "' has only " + sign.getMaxParameters() + " argument" + (sign.getMaxParameters() == 1 ? "" : "s") + ","
								+ " but " + parameters.length + " are given."
								+ " If you want to use lists in function calls, you have to use additional parentheses, e.g. 'give(player, (iron ore and gold ore))'");
				} else {
					Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
				}
				return false;
			}
		}
		if (parameters.length < sign.getMinParameters()) {
			if (first)
				Skript.error("The function '" + functionName + "' requires at least " + sign.getMinParameters() + " argument" + (sign.getMinParameters() == 1 ? "" : "s") + ","
						+ " but only " + parameters.length + " " + (parameters.length == 1 ? "is" : "are") + " given.");
			else
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
			return false;
		}
		
		for (int i = 0; i < parameters.length; i++) {
			final Parameter<?> p = sign.parameters.get(singleUberParam ? 0 : i);
			final RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				final Expression<?> e = parameters[i].getConvertedExpression(p.type.getC());
				if (e == null) {
					if (first)
						Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument given to the function '" + functionName + "' is not of the required type " + p.type + "."
								+ " Check the correct order of the arguments and put lists into parentheses if appropriate (e.g. 'give(player, (iron ore and gold ore))')."
								+ " Please note that storing the value in a variable and then using that variable as parameter will suppress this error, but it still won't work.");
					else
						Skript.error("The function '" + functionName + "' was redefined with different, incompatible arguments, but is still used in other script(s)."
								+ " These will continue to use the old version of the function until Skript restarts.");
					return false;
				}
				parameters[i] = e;
			} finally {
				log.printLog();
			}
		}
		
		signature = (Signature<? extends T>) sign;
		function = (Function<? extends T>) newFunc; // Try this, in case it exists
		Functions.registerCaller(this);
		
		return true;
	}

	@Nullable
	public Function<? extends T> getFunction() {
		return function;
	}

	public boolean resetReturnValue() {
		if (function != null)
			return function.resetReturnValue();
		return false;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected T[] execute(final Event e) {
		if (function == null)
			function = (Function<? extends T>) Functions.getFunction(functionName);
		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Invalid function call to function that does not exist yet. Be careful when using functions in 'script load' events!");
			return null; // Return nothing and hope it works
		}
		
		final Object[][] params = new Object[singleUberParam ? 1 : parameters.length][];
		if (singleUberParam && parameters.length > 1) {
			final ArrayList<Object> l = new ArrayList<>();
			for (int i = 0; i < parameters.length; i++)
				l.addAll(Arrays.asList(parameters[i].getArray(e))); // TODO what if an argument is not available? pass null or abort?
			params[0] = l.toArray();
			// Don't allow mutating across function boundary; same hack is applied to variables
			for (int i = 0; i < params[0].length; i++) {
				if (params[0][i] instanceof Location)
					params[0][i] = ((Location) params[0][i]).clone();
			}
		} else {
			for (int i = 0; i < params.length; i++) {
				params[i] = parameters[i].getArray(e); // TODO what if an argument is not available? pass null or abort?
				// Don't allow mutating across function boundary; same hack is applied to variables
				for (int j = 0; j < params[i].length; j++) {
					if (params[i][j] instanceof Location)
						params[i][j] = ((Location) params[i][j]).clone();
				}
			}
		}
		
		assert function != null;
		return function.execute(params);
	}
	
	public boolean isSingle() {
		return single;
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends T> getReturnType() {
		if (signature == null) {
			throw new SkriptAPIException("Signature of function is null when return type is asked!");
		}
		assert signature != null;
		@SuppressWarnings("null") // Wait what, Eclipse? Already asserted this...
		ClassInfo<? extends T> ret = signature.returnType;
		return (Class<? extends T>) (ret == null ? Unknown.class : ret.getC());
	}
	
	public String toString(@Nullable final Event e, final boolean debug) {
		final StringBuilder b = new StringBuilder(functionName + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(parameters[i].toString(e, debug));
		}
		return "" + b.append(")");
	}
	
}
