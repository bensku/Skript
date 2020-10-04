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
package ch.njol.skript.lang.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Reference to a Skript function.
 */
public class FunctionReference<T> {
	
	/**
	 * Name of function that is called, for logging purposes.
	 */
	final String functionName;
	
	/**
	 * Signature of referenced function. If {@link #validateFunction(boolean)}
	 * succeeds, this is not null.
	 */
	@Nullable
	private Signature<? extends T> signature;
	
	/**
	 * Actual function reference. Null before the function is called for first
	 * time.
	 */
	@Nullable
	private Function<? extends T> function;
	
	/**
	 * If all function parameters can be condensed to a single list.
	 */
	private boolean singleListParam;
	
	/**
	 * Definitions of function parameters.
	 */
	private final Expression<?>[] parameters;
	
	/**
	 * Indicates if the caller expects this function to return a single value.
	 * Used for verifying correctness of the function signature.
	 */
	private boolean single;
	
	/**
	 * Return types expected from this function. Used for verifying correctness
	 * of the function signature.
	 */
	@Nullable
	private final Class<? extends T>[] returnTypes;
	
	/**
	 * Node for {@link #validateFunction(boolean)} to use for logging.
	 */
	@Nullable
	private final Node node;
	
	/**
	 * Script in which this reference is found. Used for function unload
	 * safety checks.
	 */
	@Nullable
	public final String script;
	
	public FunctionReference(final String functionName, final @Nullable Node node, @Nullable final String script, @Nullable final Class<? extends T>[] returnTypes, final Expression<?>[] params) {
		this.functionName = functionName;
		this.node = node;
		this.script = script;
		this.returnTypes = returnTypes;
		parameters = params;
	}
	
	/**
	 * Validates this function reference. Prints errors if needed.
	 * @param first True if this is called while loading a script. False when
	 * this is called when the function signature changes.
	 * @return True if validation succeeded.
	 */
	@SuppressWarnings("unchecked")
	public boolean validateFunction(boolean first) {
		SkriptLogger.setNode(node);
		Skript.debug("Validating function " + functionName);
		final Signature<?> sign = Functions.getSignature(functionName);
		
		// Check if the requested function exists
		if (sign == null) {
			if (first)
				Skript.error("The function '" + functionName + "' does not exist.");
			else
				Skript.error("The function '" + functionName + "' was deleted or renamed, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
			return false;
		}
		
		// Validate that return types are what caller expects they are
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
		
		// Validate parameter count
		singleListParam = sign.getMaxParameters() == 1 && !sign.getParameter(0).single;
		if (!singleListParam) { // Check that parameter count is within allowed range
			// Too many parameters
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
		// Not enough parameters
		if (parameters.length < sign.getMinParameters()) {
			if (first)
				Skript.error("The function '" + functionName + "' requires at least " + sign.getMinParameters() + " argument" + (sign.getMinParameters() == 1 ? "" : "s") + ","
						+ " but only " + parameters.length + " " + (parameters.length == 1 ? "is" : "are") + " given.");
			else
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
			return false;
		}
		
		// Check parameter types
		for (int i = 0; i < parameters.length; i++) {
			final Parameter<?> p = sign.parameters[singleListParam ? 0 : i];
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
		sign.calls.add(this);
		
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
		// If needed, acquire the function reference
		if (function == null)
			function = (Function<? extends T>) Functions.getFunction(functionName);
		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Invalid function call to a function that does not exist yet. Be careful when using functions in 'script load' events!");
			return null; // Return nothing and hope it works
		}
		
		// Prepare parameter values for calling
		final Object[][] params = new Object[singleListParam ? 1 : parameters.length][];
		if (singleListParam && parameters.length > 1) { // All parameters to one list
			List<Object> l = new ArrayList<>();
			for (int i = 0; i < parameters.length; i++)
				l.addAll(Arrays.asList(parameters[i].getArray(e)));
			params[0] = l.toArray();
			// Don't allow mutating across function boundary; same hack is applied to variables
			for (int i = 0; i < params[0].length; i++) {
				if (params[0][i] instanceof Location)
					params[0][i] = ((Location) params[0][i]).clone();
			}
		} else { // Use parameters in normal way
			for (int i = 0; i < params.length; i++) {
				params[i] = parameters[i].getArray(e);
				// Don't allow mutating across function boundary; same hack is applied to variables
				for (int j = 0; j < params[i].length; j++) {
					if (params[i][j] instanceof Location)
						params[i][j] = ((Location) params[i][j]).clone();
				}
			}
		}
		
		// Execute the function
		assert function != null;
		return function.execute(params);
	}
	
	public boolean isSingle() {
		return single;
	}
	
	@Nullable
	public Class<? extends T> getReturnType() {
		if (signature == null) {
			throw new SkriptAPIException("Signature of function is null when return type is asked!");
		}
		assert signature != null;
		@SuppressWarnings("null")
		ClassInfo<? extends T> ret = signature.returnType;
		return ret == null ? null : ret.getC();
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
