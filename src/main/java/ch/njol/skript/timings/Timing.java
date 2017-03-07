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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;

import com.google.common.collect.ImmutableMap;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Trigger;

/**
 * Timing for certain action.
 */
public class Timing {
	
	private Map<Trigger,Long> triggerTimes;
	private long eventTime;
	
	/**
	 * Creates a new timing. Only used by {@link Timings}
	 */
	protected Timing() {
		triggerTimes = new LinkedHashMap<>();
		eventTime = 0L;
	}
	
	public void addTrigger(Trigger t, long time) {
		triggerTimes.put(t, time);
	}
	
	public void setEventTime(long time) {
		eventTime = time;
	}
	
	@SuppressWarnings("null")
	public Map<Trigger,Long> getTriggerTimes() {
		return ImmutableMap.copyOf(triggerTimes);
	}
	
	public long getEventTime() {
		return eventTime;
	}
}
