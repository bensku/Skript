/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.lang.function;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains a set of functions.
 */
public class Namespace {

	/**
	 * Origin of functions in namespace.
	 */
	public enum Origin {
		/**
		 * Functions implemented in Java.
		 */
		JAVA,

		/**
		 * Script functions.
		 */
		SCRIPT
	}

	/**
	 * Key to a namespace.
	 */
	public static class Key {

		private final Origin origin;

		private final String name;

		public Key(Origin origin, String name) {
			super();
			this.origin = origin;
			this.name = name;
		}

		public Origin getOrigin() {
			return origin;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + origin.hashCode();
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!name.equals(other.name))
				return false;
			return origin == other.origin;
		}
	}

	/**
	 * Signatures of known functions.
	 */
	private final Map<String, Signature<?>> signatures;

	/**
	 * Known functions. Populated as function bodies are loaded.
	 */
	private final Map<String, Function<?>> functions;

	public Namespace() {
		this.signatures = new HashMap<>();
		this.functions = new HashMap<>();
	}

	@Nullable
	public Signature<?> getSignature(String name) {
		return signatures.get(name);
	}

	public void addSignature(Signature<?> sign) {
		if (signatures.containsKey(sign.getName())) {
			throw new IllegalArgumentException("function name already used");
		}
		signatures.put(sign.getName(), sign);
	}

	@SuppressWarnings("null")
	public Collection<Signature<?>> getSignatures() {
		return signatures.values();
	}

	@Nullable
	public Function<?> getFunction(String name) {
		return functions.get(name);
	}

	public void addFunction(Function<?> func) {
		assert signatures.containsKey(func.getName()) : "missing signature for function";
		functions.put(func.getName(), func);
	}

	@SuppressWarnings("null")
	public Collection<Function<?>> getFunctions() {
		return functions.values();
	}
}
