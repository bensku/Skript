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
package ch.njol.skript.lang;

/**
 * @author Peter Güttinger
 * @param <E> the syntax element this info is for
 */
public class SyntaxElementInfo<E extends SyntaxElement> {
	
	public final Class<E> c;
	public final String[] patterns;
	
	public SyntaxElementInfo(final String[] patterns, final Class<E> c) throws IllegalArgumentException {
		this.patterns = patterns;
		this.c = c;
		try {
			c.getConstructor();
//			if (!c.getDeclaredConstructor().isAccessible())
//				throw new IllegalArgumentException("The nullary constructor of class "+c.getName()+" is not public");
		} catch (final NoSuchMethodException e) {
			// throwing an Exception throws an (empty) ExceptionInInitializerError instead, thus an Error is used
			throw new Error(c + " does not have a public nullary constructor", e);
		} catch (final SecurityException e) {
			throw new IllegalStateException("Skript cannot run properly because a security manager is blocking it!");
		}
	}
	
}
