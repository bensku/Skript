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

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public abstract class Function<T> {
	
	final String name;
	
	final Parameter<?>[] parameters;
	
	@Nullable
	final ClassInfo<T> returnType;
	final boolean single;
	
	public Function(final String name, final Parameter<?>[] parameters, final @Nullable ClassInfo<T> returnType, final boolean single) {
		this.name = name;
		this.parameters = parameters;
		this.returnType = returnType;
		this.single = single;
	}
	
	public String getName() {
		return name;
	}
	
	@SuppressWarnings("null")
	public Parameter<?> getParameter(final int index) {
		return parameters[index];
	}
	
	public Parameter<?>[] getParameters() {
		return parameters;
	}
	
	@Nullable
	public ClassInfo<T> getReturnType() {
		return returnType;
	}
	
	public boolean isSingle() {
		return single;
	}
	
	// TODO allow setting parameters by name
	public int getMinParameters() {
		for (int i = parameters.length - 1; i >= 0; i--) {
			if (parameters[i].def == null)
				return i + 1;
		}
		return 0;
	}
	
	public int getMaxParameters() {
		return parameters.length;
	}
	
	// FIXME what happens with a delay in a function?
	
	/**
	 * @param params An array with at least {@link #getMinParameters()} elements and at most {@link #getMaxParameters()} elements.
	 * @return The result of the function
	 */
	@SuppressWarnings("null")
	@Nullable
	public final T[] execute(final Object[][] params) {
		final FunctionEvent e = new FunctionEvent();
		if (params.length > parameters.length) {
			assert false : params.length;
			return null;
		}
		final Object[][] ps = params.length < parameters.length ? Arrays.copyOf(params, parameters.length) : params;
		assert ps != null;
		for (int i = 0; i < parameters.length; i++) {
			final Parameter<?> p = parameters[i];
			final Object[] val = i < params.length ? params[i] : p.def != null ? p.def.getArray(e) : null;
			if (val == null || val.length == 0)
				return null;
			ps[i] = val;
		}
		final T[] r = execute(e, ps);
		assert returnType == null ? r == null : r == null || (r.length <= 1 || !single) && !CollectionUtils.contains(r, null) && returnType.getC().isAssignableFrom(r.getClass().getComponentType()) : this + "; " + Arrays.toString(r);
		return r == null || r.length > 0 ? r : null;
	}
	
	/**
	 * @param e
	 * @param params An array containing as many arrays as this function has parameters. The contained arrays are neither null nor empty, and are of type Object[] (i.e. not of the
	 *            actual parameters' types).
	 * @return Whatever this function is supposed to return. May be null or empty, but must not contain null elements.
	 */
	@Nullable
	public abstract T[] execute(FunctionEvent e, final Object[][] params);
	
	@Override
	public String toString() {
		return "function " + name;
	}
	
	/**
	 * Generates a signature for this function. Should only be used to validate
	 * (Java) function references.
	 * @return Signature.
	 */
	@SuppressWarnings("null")
	public Signature<T> getSignature() {
		return new Signature<T>("unknown", name, Arrays.asList(parameters), returnType, null, single);
	}
	
}
