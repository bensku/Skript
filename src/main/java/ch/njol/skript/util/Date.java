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
package ch.njol.skript.util;

import java.time.ZoneId;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Date implements Comparable<Date>, YggdrasilSerializable {
	
	/**
	 * Timestamp. Should always be in computer time/UTC/GMT+0.
	 */
	private long timestamp;
	
	public Date() {
		this(System.currentTimeMillis());
	}
	
	public Date(final long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Date(final long timestamp, final TimeZone zone) {
		final long offset = zone.getOffset(timestamp);
		this.timestamp = timestamp - offset;
	}
	
	public Timespan difference(final Date other) {
		return new Timespan(Math.abs(timestamp - other.timestamp));
	}
	
	@Override
	public int compareTo(final @Nullable Date other) {
		final long d = other == null ? timestamp : timestamp - other.timestamp;
		return d < 0 ? -1 : d > 0 ? 1 : 0;
	}
	
	@Override
	public String toString() {
		return SkriptConfig.formatDate(timestamp);
	}
	
	/**
	 * @return The timestamp in milliseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	public void add(final Timespan span) {
		timestamp += span.getMilliSeconds();
	}
	
	public void subtract(final Timespan span) {
		timestamp -= span.getMilliSeconds();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Date))
			return false;
		final Date other = (Date) obj;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
	
}
