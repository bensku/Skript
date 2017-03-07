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
package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 */
public class NumberArithmetic implements Arithmetic<Number, Number> {
	
	@SuppressWarnings("null")
	@Override
	public Number difference(final Number first, final Number second) {
		return Math.abs(first.doubleValue() - second.doubleValue());
	}
	
	@Override
	public Number add(final Number value, final Number difference) {
		return new Double(value.doubleValue() + difference.doubleValue());
	}
	
	@Override
	public Number subtract(final Number value, final Number difference) {
		return new Double(value.doubleValue() - difference.doubleValue());
	}
	
}
