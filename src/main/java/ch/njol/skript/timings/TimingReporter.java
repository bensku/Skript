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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.localization.Language;

/**
 * Creates timing reports.
 */
public class TimingReporter {
	
	@SuppressWarnings("null")
	public static String getReport() {
		Map<Object,Timing> timings = Timings.timings;
		Map<String,Long> triggers = new HashMap<>();
		Map<Object,Long> events = new HashMap<>();
		
		for (Entry<Object,Timing> entry : timings.entrySet()) {
			Object key = entry.getKey();
			Timing val = entry.getValue();
			
			for (Entry<Trigger,Long> trigger : val.getTriggerTimes().entrySet()) {
				String name = trigger.getKey().getName();
				long tt = 0L;
				if (triggers.containsKey(name))
					tt = triggers.get(name);
				tt += trigger.getValue();
				triggers.put(name, tt);
			}
			
			long evtTime = 0L;
			if (events.containsKey(key))
				evtTime = events.get(key);
			evtTime += val.getEventTime();
			events.put(key, evtTime);
		}
		
		long length = Timings.disableTime - Timings.enableTime;
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Language.get("timings.start"), length / (float) 1000000000) + "\n");
		
		sb.append(Language.get("timings.triggers") + "\n");
		for (Entry<String,Long> trigger : triggers.entrySet()) {
			float percent = trigger.getValue() / (float) length * 100;
			sb.append(trigger.getKey() + ": " + (trigger.getValue() / (float) 1000000) + "ms (" + percent + "%)\n");
		}
		
		sb.append(Language.get("timings.events") + "\n");
		for (Entry<Object,Long> event : events.entrySet()) {
			float percent = event.getValue() / (float) length * 100;
			sb.append(event.getKey() + ": " + (event.getValue() / (float) 1000000) + "ms (" + percent + "%)\n");
		}
		
		return sb.toString();
	}
	
	public static void saveToFile(String str) {
		File folder = Skript.getInstance().getDataFolder();
		String timestamp = DateFormat.getTimeInstance().format(System.currentTimeMillis()).replaceAll(":", "_");
		File file = new File(folder + "/timings-" + timestamp + ".log");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		try {
			PrintWriter out = new PrintWriter(file);
			out.write(str);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace(); // Can't happen...
		}
	}
}
