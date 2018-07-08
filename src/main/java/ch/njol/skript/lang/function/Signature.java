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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.NonNullPair;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> {
	
	final String script;
	final String name; // Stored for hashCode
	final List<Parameter<?>> parameters;
	@Nullable
	final ClassInfo<T> returnType;
	@Nullable
	final NonNullPair<String, Boolean> info;
	final boolean single;
	
	final Collection<FunctionReference<?>> calls;
	
	@SuppressWarnings("null")
	public Signature(String script, String name, List<Parameter<?>> parameters, @Nullable final ClassInfo<T> returnType, @Nullable final NonNullPair<String, Boolean> info, boolean single) {
		this.script = script;
		this.name = name;
		this.parameters = Collections.unmodifiableList(parameters);
		this.returnType = returnType;
		this.info = info;
		this.single = single;
		
		calls = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	@SuppressWarnings("null")
	public Parameter<?> getParameter(final int index) {
		return parameters.get(index);
	}
	
	public List<Parameter<?>> getParameters() {
		return parameters;
	}
	
	@Nullable
	public ClassInfo<T> getReturnType() {
		return returnType;
	}
	
	public boolean isSingle() {
		return single;
	}
	
	public int getMaxParameters() {
		return parameters.size();
	}
	
	public int getMinParameters() {
		for (int i = parameters.size() - 1; i >= 0; i--) {
			if (parameters.get(i).def == null)
				return i + 1;
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
