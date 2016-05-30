/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.util;

/**
 * @author Peter Güttinger
 */
public class NonNullPair<T1, T2> extends Pair<T1, T2> {
	private static final long serialVersionUID = 820250942098905541L;
	
	public NonNullPair(final T1 first, final T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public NonNullPair(final NonNullPair<T1, T2> other) {
		first = other.first;
		second = other.second;
	}
	
	@Override

	public T1 getFirst() {
		return first;
	}
	

	@Override
	public void setFirst(final T1 first) {
		this.first = first;
	}
	
	@Override

	public T2 getSecond() {
		return second;
	}
	

	@Override
	public void setSecond(final T2 second) {
		this.second = second;
	}
	

	@Override
	public T1 getKey() {
		return first;
	}
	

	@Override
	public T2 getValue() {
		return second;
	}
	

	@Override
	public T2 setValue(final T2 value) {
		final T2 old = second;
		second = value;
		return old;
	}
	
	/**
	 * @return a shallow copy of this pair
	 */
	@Override
	public NonNullPair<T1, T2> clone() {
		return new NonNullPair<T1, T2>(this);
	}
	
}
