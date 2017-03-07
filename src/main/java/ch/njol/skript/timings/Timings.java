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
package ch.njol.skript.timings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ch.njol.skript.SkriptEventHandler;

/**
 * Static utils for Skript timings.
 */
public class Timings {
	
	protected static Map<Object,Timing> timings = new HashMap<>();
	private static volatile boolean enabled;
	protected static volatile long enableTime;
	protected static volatile long disableTime;
	
	public static Timing of(Object ref) {
		Timing timing;
		synchronized (timings) {
			if (timings.containsKey(ref)) {
				timing = timings.get(ref);
			} else {
				timing = new Timing();
				timings.put(ref, timing);
			}
		}
		
		assert timing != null;
		return timing;
	}
	
	public static boolean enabled() {
		return enabled;
	}
	
	public static void enable() {
		enabled = true;
		enableTime = System.nanoTime();
	}
	
	public static void disable() {
		enabled = false;
		disableTime = System.nanoTime();
		SkriptEventHandler.removeTiming();
	}
	
	public static void clear() {
		timings.clear();
	}
	
}
