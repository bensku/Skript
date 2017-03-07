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
package ch.njol.skript.registrations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;

/**
 * @author Peter Güttinger
 */
public class EventValues {
	
	private EventValues() {}
	
	private final static class EventValueInfo<E extends Event, T> {
		
		public final Class<E> event;
		public final Class<T> c;
		public final Getter<T, E> getter;
		@Nullable
		public final Class<? extends E>[] exculdes;
		@Nullable
		public final String excludeErrorMessage;
		
		public EventValueInfo(final Class<E> event, final Class<T> c, final Getter<T, E> getter, final @Nullable String excludeErrorMessage, final @Nullable Class<? extends E>[] exculdes) {
			assert event != null;
			assert c != null;
			assert getter != null;
			this.event = event;
			this.c = c;
			this.getter = getter;
			this.exculdes = exculdes;
			this.excludeErrorMessage = excludeErrorMessage;
		}
	}
	
	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<EventValueInfo<?, ?>>(30);
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<EventValueInfo<?, ?>>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<EventValueInfo<?, ?>>();
	
	private final static List<EventValueInfo<?, ?>> getEventValuesList(final int time) {
		if (time == -1)
			return pastEventValues;
		if (time == 0)
			return defaultEventValues;
		if (time == 1)
			return futureEventValues;
		throw new IllegalArgumentException("time must be -1, 0, or 1");
	}
	
	/**
	 * Registers an event value.
	 * 
	 * @param e the event type
	 * @param c the type of the default value
	 * @param g the getter to get the value
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final Getter<T, E> g, final int time) {
		registerEventValue(e, c, g, time, null, (Class<? extends E>[]) null);
	}
	
	@Deprecated
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final ch.njol.skript.classes.SerializableGetter<T, E> g, final int time) {
		registerEventValue(e, c, (Getter<T, E>) g, time);
	}
	
	/**
	 * Same as {@link #registerEventValue(Class, Class, Getter, int)}
	 * 
	 * @param e
	 * @param c
	 * @param g
	 * @param time
	 * @param excludes Subclasses of the event for which this event value should not be registered for
	 */
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final Getter<T, E> g, final int time, final @Nullable String excludeErrorMessage, final @Nullable Class<? extends E>... excludes) {
		Skript.checkAcceptRegistrations();
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (int i = 0; i < eventValues.size(); i++) {
			final EventValueInfo<?, ?> info = eventValues.get(i);
			if (info.event != e ? info.event.isAssignableFrom(e) : info.c.isAssignableFrom(c)) {
				eventValues.add(i, new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
				return;
			}
		}
		eventValues.add(new EventValueInfo<E, T>(e, c, g, excludeErrorMessage, excludes));
	}
	
	@Deprecated
	public static <T, E extends Event> void registerEventValue(final Class<E> e, final Class<T> c, final ch.njol.skript.classes.SerializableGetter<T, E> g, final int time, final @Nullable String excludeErrorMessage, final @Nullable Class<? extends E>... excludes) {
		registerEventValue(e, c, (Getter<T, E>) g, time, excludeErrorMessage, excludes);
	}
	
	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return The event's value
	 * @see #registerEventValue(Class, Class, Getter, int)
	 */
	@Nullable
	public static <T, E extends Event> T getEventValue(final E e, final Class<T> c, final int time) {
		@SuppressWarnings({"null", "unchecked"})
		final Getter<? extends T, ? super E> g = EventValues.getEventValueGetter((Class<E>) e.getClass(), c, time);
		if (g == null)
			return null;
		return g.get(e);
	}
	
	/**
	 * Returns a getter to get a value from an event.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param e
	 * @param c
	 * @param time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Getter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public final static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time) {
		return EventValues.getEventValueGetter(e, c, time, true);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private final static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(final Class<E> e, final Class<T> c, final int time, final boolean allowDefault) {
		final List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		boolean b;
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (((b = ev.event.isAssignableFrom(e)) || e.isAssignableFrom(ev.event)) && c.isAssignableFrom(ev.c)) {
				if (!EventValues.checkExcludes(ev, e))
					return null;
				if (b)
					return (Getter<? extends T, ? super E>) ev.getter;
				return new Getter<T, E>() {
					@Override
					@Nullable
					public T get(final E event) {
						if (!ev.event.isInstance(event))
							return null;
						return ((Getter<? extends T, E>) ev.getter).get(event);
					}
				};
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if (((b = ev.event.isAssignableFrom(e)) || e.isAssignableFrom(ev.event)) && ev.c.isAssignableFrom(c)) {
				if (!EventValues.checkExcludes(ev, e))
					return null;
				final boolean checkInstanceOf = !b;
				return new Getter<T, E>() {
					@Override
					@Nullable
					public T get(final E event) {
						if (checkInstanceOf && !e.isInstance(event))
							return null;
						final Object o = ((Getter<? super T, ? super E>) ev.getter).get(event);
						if (c.isInstance(o))
							return (T) o;
						return null;
					}
				};
			}
		}
		for (final EventValueInfo<?, ?> ev : eventValues) {
			if ((b = ev.event.isAssignableFrom(e)) || e.isAssignableFrom(ev.event)) {
				if (!EventValues.checkExcludes(ev, e))
					return null;
				final Getter<? extends T, ? super E> g = (Getter<? extends T, ? super E>) getConvertedGetter(ev, c, !b);
				if (g != null)
					return g;
			}
		}
		if (allowDefault && time != 0)
			return getEventValueGetter(e, c, 0, false);
		return null;
	}
	
	private final static boolean checkExcludes(final EventValueInfo<?, ?> ev, final Class<? extends Event> e) {
		final Class<? extends Event>[] excl = ev.exculdes;
		if (excl == null)
			return true;
		for (final Class<? extends Event> ex : excl) {
			if (ex.isAssignableFrom(e)) {
				Skript.error(ev.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}
	
	@Nullable
	private final static <E extends Event, F, T> Getter<? extends T, ? super E> getConvertedGetter(final EventValueInfo<E, F> i, final Class<T> to, final boolean checkInstanceOf) {
		final Converter<? super F, ? extends T> c = Converters.getConverter(i.c, to);
		if (c == null)
			return null;
		return new Getter<T, E>() {
			@Override
			@Nullable
			public T get(final E e) {
				if (checkInstanceOf && !i.event.isInstance(e))
					return null;
				final F f = i.getter.get(e);
				if (f == null)
					return null;
				return c.convert(f);
			}
		};
	}
	
	public final static boolean doesEventValueHaveTimeStates(final Class<? extends Event> e, final Class<?> c) {
		return getEventValueGetter(e, c, -1, false) != null || getEventValueGetter(e, c, 1, false) != null;
	}
	
}
