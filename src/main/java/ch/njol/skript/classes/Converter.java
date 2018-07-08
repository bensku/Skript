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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.registrations.Converters;

/**
 * used to convert data from one type to another.
 * 
 * @param <F> the accepted type of objects to convert <u>f</u>rom
 * @param <T> the type to convert <u>t</u>o
 * @author Peter Güttinger
 * @see Converters#registerConverter(Class, Class, Converter)
 */
public interface Converter<F, T> {
	
	public final static int NO_LEFT_CHAINING = 1;
	public final static int NO_RIGHT_CHAINING = 2;
	public final static int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;
	public final static int NO_COMMAND_ARGUMENTS = 4;
	
	/**
	 * holds information about a converter
	 * 
	 * @author Peter Güttinger
	 * @param <F> same as in {@link Converter}
	 * @param <T> dito
	 */
	@SuppressWarnings("null")
	@NonNullByDefault
	public final static class ConverterInfo<F, T> {
		
		public final Class<F> from;
		public final Class<T> to;
		public final Converter<F, T> converter;
		public final int options;
		
		public ConverterInfo(final Class<F> from, final Class<T> to, final Converter<F, T> converter, final int options) {
			this.from = from;
			this.to = to;
			this.converter = converter;
			this.options = options;
		}
		
	}
	
	/**
	 * Converts an object from the given to the desired type.
	 * 
	 * @param f The object to convert.
	 * @return the converted object
	 */
	@Nullable
	public T convert(F f);
	
	public final static class ConverterUtils {
		
		public final static <F, T> Converter<?, T> createInstanceofConverter(final ConverterInfo<F, T> conv) {
			return createInstanceofConverter(conv.from, conv.converter);
		}
		
		public final static <F, T> Converter<?, T> createInstanceofConverter(final Class<F> from, final Converter<F, T> conv) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					return conv.convert((F) o);
				}
			};
		}
		
		public final static <F, T> Converter<F, T> createInstanceofConverter(final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<F, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final F f) {
					final Object o = conv.convert(f);
					if (to.isInstance(o))
						return (T) o;
					return null;
				}
			};
		}
		
		public final static <F, T> Converter<?, T> createDoubleInstanceofConverter(final ConverterInfo<F, ?> conv, final Class<T> to) {
			return createDoubleInstanceofConverter(conv.from, conv.converter, to);
		}
		
		public final static <F, T> Converter<?, T> createDoubleInstanceofConverter(final Class<F> from, final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					final Object o2 = conv.convert((F) o);
					if (to.isInstance(o2))
						return (T) o2;
					return null;
				}
			};
		}
		
	}
	
}
