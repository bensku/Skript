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
package ch.njol.skript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.timings.Timing;
import ch.njol.skript.timings.Timings;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptEventHandler {
	private SkriptEventHandler() {}
	
	final static Map<Class<? extends Event>, List<Trigger>> triggers = new HashMap<>();
	
	private final static List<Trigger> selfRegisteredTriggers = new ArrayList<>();
	
	private final static Iterator<Trigger> getTriggers(final Class<? extends Event> event) {
		return new Iterator<Trigger>() {
			@Nullable
			private Class<?> e = event;
			@Nullable
			private Iterator<Trigger> current = null;
			
			@Override
			public boolean hasNext() {
				Iterator<Trigger> current = this.current;
				Class<?> e = this.e;
				while (current == null || !current.hasNext()) {
					if (e == null || !Event.class.isAssignableFrom(e))
						return false;
					final List<Trigger> l = triggers.get(e);
					this.current = current = l == null ? null : l.iterator();
					this.e = e = e.getSuperclass();
				}
				return true;
			}
			
			@Override
			public Trigger next() {
				final Iterator<Trigger> current = this.current;
				if (current == null || !hasNext())
					throw new NoSuchElementException();
				final Trigger next = current.next();
				assert next != null;
				return next;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Nullable
	static Event last = null;
	
	final static EventExecutor ee = new EventExecutor() {
		@Override
		public void execute(final @Nullable Listener l, final @Nullable Event e) {
			if (e == null)
				return;
			if (last == e) // an event is received multiple times if multiple superclasses of it are registered
				return;
			last = e;
			check(e);
		}
	};
	
	static void check(final Event e) {
		@SuppressWarnings("null")
		Iterator<Trigger> ts = getTriggers(e.getClass());
		if (!ts.hasNext())
			return;
		
		if (Skript.logVeryHigh()) {
			boolean hasTrigger = false;
			while (ts.hasNext()) {
				if (ts.next().getEvent().check(e)) {
					hasTrigger = true;
					break;
				}
			}
			if (!hasTrigger)
				return;
			final Class<? extends Event> c = e.getClass();
			assert c != null;
			ts = getTriggers(c);
			
			logEventStart(e);
		}
		
		if (e instanceof Cancellable && ((Cancellable) e).isCancelled() &&
				!(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY)
				|| e instanceof ServerCommandEvent && (((ServerCommandEvent) e).getCommand() == null || ((ServerCommandEvent) e).getCommand().isEmpty())) {
			if (Skript.logVeryHigh())
				Skript.info(" -x- was cancelled");
			return;
		}
		
		while (ts.hasNext()) {
			final Trigger t = ts.next();
			if (!t.getEvent().check(e))
				continue;
			logTriggerStart(t);
			t.execute(e);
			logTriggerEnd(t);
		}
		
		logEventEnd();
	}
	
	private static long startEvent;
	@Nullable
	private static Timing timing;
	
	@SuppressWarnings("null")
	public static void logEventStart(final Event e) {
		if (Timings.enabled())
			timing = Timings.of(e.getEventName());
		
		startEvent = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("");
		Skript.info("== " + e.getClass().getName() + " ==");
	}
	
	public static void logEventEnd() {
		if (timing != null)
			timing.setEventTime(System.nanoTime() - startEvent);
		
		if (!Skript.logVeryHigh())
			return;
		Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}
	
	static long startTrigger;
	
	public static void logTriggerStart(final Trigger t) {
		startTrigger = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName());
	}
	
	public static void logTriggerEnd(final Trigger t) {
		if (timing != null)
			timing.addTrigger(t, System.nanoTime() - startTrigger);
		
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
	}
	
	public static void removeTiming() {
		timing = null;
	}
	
	public static void addTrigger(final Class<? extends Event>[] events, final Trigger trigger) {
		for (final Class<? extends Event> e : events) {
			List<Trigger> ts = triggers.get(e);
			if (ts == null)
				triggers.put(e, ts = new ArrayList<>());
			ts.add(trigger);
		}
	}
	
	/**
	 * Stores a self registered trigger to allow for it to be unloaded later on.
	 * 
	 * @param t Trigger that has already been registered to its event
	 */
	public static void addSelfRegisteringTrigger(final Trigger t) {
		assert t.getEvent() instanceof SelfRegisteringSkriptEvent;
		selfRegisteredTriggers.add(t);
	}
	
	static ScriptInfo removeTriggers(final File script) {
		final ScriptInfo info = new ScriptInfo();
		info.files = 1;
		
		final Iterator<List<Trigger>> triggersIter = SkriptEventHandler.triggers.values().iterator();
		while (triggersIter.hasNext()) {
			final List<Trigger> ts = triggersIter.next();
			for (int i = 0; i < ts.size(); i++) {
				if (script.equals(ts.get(i).getScript())) {
					info.triggers++;
					ts.remove(i);
					i--;
					if (ts.isEmpty())
						triggersIter.remove();
				}
			}
		}
		
		for (int i = 0; i < selfRegisteredTriggers.size(); i++) {
			final Trigger t = selfRegisteredTriggers.get(i);
			if (script.equals(t.getScript())) {
				info.triggers++;
				((SelfRegisteringSkriptEvent) t.getEvent()).unregister(t);
				selfRegisteredTriggers.remove(i);
				i--;
			}
		}
		
		info.commands = Commands.unregisterCommands(script);
		
		info.functions = Functions.clearFunctions(script);
		
		return info;
	}
	
	static void removeAllTriggers() {
		triggers.clear();
		for (final Trigger t : selfRegisteredTriggers)
			((SelfRegisteringSkriptEvent) t.getEvent()).unregisterAll();
		selfRegisteredTriggers.clear();
//		unregisterEvents();
	}
	
	/**
	 * Stores which events are currently registered with Bukkit
	 */
	private final static Set<Class<? extends Event>> registeredEvents = new HashSet<>();
	private final static Listener listener = new Listener() {};
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	final static void registerBukkitEvents() {
		for (final Class<? extends Event> e : triggers.keySet()) {
			assert e != null;
			if (!containsSuperclass((Set) registeredEvents, e)) { // I just love Java's generics
				Bukkit.getPluginManager().registerEvent(e, listener, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance());
				registeredEvents.add(e);
//				for (final Iterator<Class<? extends Event>> i = registeredEvents.iterator(); i.hasNext();) {
//					final Class<? extends Event> ev = i.next();
//					if (e.isAssignableFrom(ev)) {
//						if (unregisterEvent(ev))
//							i.remove();
//					}
//				}
			}
		}
	}
	
	public final static boolean containsSuperclass(final Set<Class<?>> classes, final Class<?> c) {
		if (classes.contains(c))
			return true;
		for (final Class<?> cl : classes) {
			if (cl.isAssignableFrom(c))
				return true;
		}
		return false;
	}
	
//	private final static void unregisterEvents() {
//		for (final Iterator<Class<? extends Event>> i = registeredEvents.iterator(); i.hasNext();) {
//			if (unregisterEvent(i.next()))
//				i.remove();
//		}
//	}
//
//	private final static boolean unregisterEvent(Class<? extends Event> event) {
//		try {
//			Method m = null;
//			while (m == null) {
//				try {
//					m = event.getDeclaredMethod("getHandlerList");
//				} catch (NoSuchMethodException e) {
//					event = (Class<? extends Event>) event.getSuperclass();
//					if (event == Event.class) {
//						assert false;
//						return false;
//					}
//				}
//			}
//			m.setAccessible(true);
//			final HandlerList l = (HandlerList) m.invoke(null);
//			l.unregister(listener);
//			return true;
//		} catch (final Exception e) {
//			if (Skript.testing())
//				e.printStackTrace();
//		}
//		return false;
//	}
	
}
