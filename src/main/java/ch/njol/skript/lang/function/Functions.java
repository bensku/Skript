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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.ScriptLoader;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Functions {

	private static final String INVALID_FUNCTION_DEFINITION =
			"Invalid function definition. Please check for " +
					"typos and make sure that the function's name " +
					"only contains letters and underscores. " +
					"Refer to the documentation for more information.";

	private Functions() {}
	
	final static class FunctionData {
		final Function<?> function;
		
		public FunctionData(final Function<?> function) {
			this.function = function;
		}
	}
	
	@Nullable
	public static ScriptFunction<?> currentFunction = null;
	
	final static Map<String, JavaFunction<?>> javaFunctions = new HashMap<>();
	final static Map<String, FunctionData> functions = new ConcurrentHashMap<>();
	final static Map<String, Signature<?>> javaSignatures = new HashMap<>();
	final static Map<String, Signature<?>> signatures = new ConcurrentHashMap<>();
	
	final static List<FunctionReference<?>> postCheckNeeded = new ArrayList<>();
	
	static boolean callFunctionEvents = false;
	
	/**
	 * Register a function written in Java.
	 * @param function
	 * @return The passed function
	 */
	public static JavaFunction<?> registerFunction(final JavaFunction<?> function) {
		Skript.checkAcceptRegistrations();
		if (!function.name.matches(functionNamePattern))
			throw new SkriptAPIException("Invalid function name '" + function.name + "'");
		if (functions.containsKey(function.name))
			throw new SkriptAPIException("Duplicate function " + function.name);
		functions.put(function.name, new FunctionData(function));
		javaFunctions.put(function.name, function);
		Signature<?> sign = function.getSignature();
		javaSignatures.put(function.name, sign); // This is backup for full reloads (reload all/scripts)
		signatures.put(function.name, sign);
		return function;
	}
	
	static void registerCaller(final FunctionReference<?> r) {
		final Signature<?> sign = signatures.get(r.functionName);
		assert sign != null;
		sign.calls.add(r);
	}
	
	public final static String functionNamePattern = "[\\p{IsAlphabetic}][\\p{IsAlphabetic}\\p{IsDigit}_]*";
	
	@SuppressWarnings("null")
	private final static Pattern functionPattern = Pattern.compile("function (" + functionNamePattern + ")\\((.*)\\)(?: :: (.+))?", Pattern.CASE_INSENSITIVE),
			paramPattern = Pattern.compile("\\s*(.+?)\\s*:(?=[^:]*$)\\s*(.+?)(?:\\s*=\\s*(.+))?\\s*");
	
	/**
	 * Loads a function from given node.
	 * @param node Section node.
	 * @return Script function, or null if something went wrong.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static Function<?> loadFunction(final SectionNode node) {
		SkriptLogger.setNode(node);
		final String key = node.getKey();
		final String definition = ScriptLoader.replaceOptions(key == null ? "" : key);
		assert definition != null;
		final Matcher m = functionPattern.matcher(definition);
		if (!m.matches()) // We have checks when loading the signature, but matches() must be called anyway
			return error(INVALID_FUNCTION_DEFINITION);
		final String name = "" + m.group(1);
		Signature<?> sign = signatures.get(name);
		if (sign == null) // Signature parsing failed, probably: null signature
			return null; // This has been reported before...
		final List<Parameter<?>> params = sign.parameters;
		final ClassInfo<?> c = sign.returnType;
		final NonNullPair<String, Boolean> p = sign.info;
		
		if (Skript.debug() || node.debug())
			Skript.debug("function " + name + "(" + StringUtils.join(params, ", ") + ")" + (c != null && p != null ? " :: " + Utils.toEnglishPlural(c.getCodeName(), p.getSecond()) : "") + ":");
		
		@SuppressWarnings("null")
		final Function<?> f = new ScriptFunction<>(name, params.toArray(new Parameter[params.size()]), node, (ClassInfo<Object>) c, p == null ? false : !p.getSecond());
//		functions.put(name, new FunctionData(f)); // in constructor
		return f;
	}
	
	/**
	 * Loads the signature of function from given node.
	 * @param script Script file name (<b>might</b> be used for some checks).
	 * @param node Section node.
	 * @return Signature of function, or null if something went wrong.
	 */
	@Nullable
	public static Signature<?> loadSignature(String script, final SectionNode node) {
		SkriptLogger.setNode(node);
		final String key = node.getKey();
		final String definition = ScriptLoader.replaceOptions(key == null ? "" : key);
		assert definition != null;
		final Matcher m = functionPattern.matcher(definition);
		if (!m.matches())
			return signError(INVALID_FUNCTION_DEFINITION);
		final String name = "" + m.group(1); // TODO check for name uniqueness (currently functions with same name silently override each other)
		final String args = m.group(2);
		final String returnType = m.group(3);
		final List<Parameter<?>> params = new ArrayList<>();
		int j = 0;
		for (int i = 0; i <= args.length(); i = SkriptParser.next(args, i, ParseContext.DEFAULT)) {
			if (i == -1)
				return signError("Invalid text/variables/parentheses in the arguments of this function");
			if (i == args.length() || args.charAt(i) == ',') {
				final String arg = args.substring(j, i);
				
				if (arg.isEmpty()) // Zero-argument function
					break;
				
				// One ore more arguments for this function
				final Matcher n = paramPattern.matcher(arg);
				if (!n.matches())
					return signError("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
				final String paramName = "" + n.group(1);
				for (final Parameter<?> p : params) {
					if (p.name.toLowerCase(Locale.ENGLISH).equals(paramName.toLowerCase(Locale.ENGLISH)))
						return signError("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
				}
				ClassInfo<?> c;
				c = Classes.getClassInfoFromUserInput("" + n.group(2));
				final NonNullPair<String, Boolean> pl = Utils.getEnglishPlural("" + n.group(2));
				if (c == null)
					c = Classes.getClassInfoFromUserInput(pl.getFirst());
				if (c == null)
					return signError("Cannot recognise the type '" + n.group(2) + "'");
				String rParamName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) +
									(!pl.getSecond() ? "::1" : "") : paramName;
				final Parameter<?> p = Parameter.newInstance(rParamName, c, !pl.getSecond(), n.group(3));
				if (p == null)
					return null;
				params.add(p);
				
				j = i + 1;
			}
			if (i == args.length())
				break;
		}
		ClassInfo<?> c;
		final NonNullPair<String, Boolean> p;
		if (returnType == null) {
			c = null;
			p = null;
		} else {
			c = Classes.getClassInfoFromUserInput(returnType);
			p = Utils.getEnglishPlural(returnType);
			if (c == null)
				c = Classes.getClassInfoFromUserInput(p.getFirst());
			if (c == null) {
				return signError("Cannot recognise the type '" + returnType + "'");
			}
		}
		
		@SuppressWarnings("unchecked")
		Signature<?> sign = new Signature<>(script, name, params, (ClassInfo<Object>) c, p, p == null ? false : !p.getSecond());
		Functions.signatures.put(name, sign);
		Skript.debug("Registered function signature: " + name);
		return sign;
	}
	
	/**
	 * Creates an error and returns Function null.
	 * @param error Error message.
	 * @return Null.
	 */
	@Nullable
	private static Function<?> error(final String error) {
		Skript.error(error);
		return null;
	}
	
	/**
	 * Creates an error and returns Signature null.
	 * @param error Error message.
	 * @return Null.
	 */
	@Nullable
	private static Signature<?> signError(final String error) {
		Skript.error(error);
		return null;
	}
	
	/**
	 * Gets a function, if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existance,
	 * then use {@link #getSignature(String)}.
	 * @param name Name of function.
	 * @return Function, or null if it does not exist.
	 */
	@Nullable
	public static Function<?> getFunction(final String name) {
		final FunctionData d = functions.get(name);
		if (d == null)
			return null;
		return d.function;
	}
	
	/**
	 * Gets a signature of function with given name
	 * @param name Name of function.
	 * @return Signature, or null if function does not exist.
	 */
	@Nullable
	public static Signature<?> getSignature(final String name) {
		return signatures.get(name);
	}
	
	private final static Collection<FunctionReference<?>> toValidate = new ArrayList<>();
	
	/**
	 * Remember to call {@link #validateFunctions()} after calling this
	 * 
	 * @param script
	 * @return How many functions were removed
	 */
	public static int clearFunctions(final File script) {
		int r = 0;
		final Iterator<FunctionData> iter = functions.values().iterator();
		while (iter.hasNext()) {
			final FunctionData d = iter.next();
			if (d != null && d.function instanceof ScriptFunction) {
				Trigger trigger = ((ScriptFunction<?>) d.function).trigger;
				if (trigger == null) // Triggers can be null, make sure this isn't
					continue;
				if (!script.equals(trigger.getScript())) // Is this trigger in correct script?
					continue;
				
				iter.remove();
				r++;
				final Signature<?> sign = signatures.get(d.function.name);
				assert sign != null; // Function must have signature
				
				final Iterator<FunctionReference<?>> it = sign.calls.iterator();
				while (it.hasNext()) {
					final FunctionReference<?> c = it.next();
					if (script.equals(c.script))
						it.remove();
					else
						toValidate.add(c);
				}
			}
		}
		return r;
	}
	
	public static void validateFunctions() {
		for (final FunctionReference<?> c : toValidate)
			c.validateFunction(false);
		toValidate.clear();
	}
	
	/**
	 * Clears all function calls and removes script functions.
	 */
	public static void clearFunctions() {
		final Iterator<FunctionData> iter = functions.values().iterator();
		while (iter.hasNext()) {
			final FunctionData d = iter.next();
			if (d.function instanceof ScriptFunction) {
				iter.remove();
			} else {
				final Signature<?> sign = signatures.get(d.function.name);
				assert sign != null; // Function must have signature
				sign.calls.clear();
			}
		}
		signatures.clear();
		signatures.putAll(javaSignatures);
		assert toValidate.isEmpty() : toValidate;
		toValidate.clear();
	}
	
	@SuppressWarnings("null")
	public static Collection<JavaFunction<?>> getJavaFunctions() {
		return javaFunctions.values();
	}

	/**
	 * Puts a function directly to map. Usually no need to do so.
	 * @param func
	 */
	public static void putFunction(Function<?> func) {
		functions.put(func.name, new FunctionData(func));
	}
	
	/**
	 * Normally, function calls do not cause actual Bukkit events to be
	 * called. If an addon requires such functionality, it should call this
	 * method. After doing so, the events will be called. Calling this method
	 * many times will not cause any additional changes.
	 * <p>
	 * Note that calling events is not free; performance might vary
	 * once you have enabled that.
	 * 
	 * @param addon Addon instance. Nullness is checked runtime.
	 */
	public static void enableFunctionEvents(@Nullable SkriptAddon addon) {
		if (addon == null) {
			throw new SkriptAPIException("enabling function events requires addon instance");
		}
		
		callFunctionEvents = true;
	}
}
