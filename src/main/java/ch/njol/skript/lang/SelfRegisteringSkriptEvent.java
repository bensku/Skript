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

import org.bukkit.event.Event;

/**
 * @author Peter Güttinger
 */
public abstract class SelfRegisteringSkriptEvent extends SkriptEvent {
	
	/**
	 * This method is called after the whole trigger is loaded for events that fire themselves. This is also called when the script is deserialised.
	 * 
	 * @param t the trigger to register to this event
	 */
	public abstract void register(final Trigger t);
	
	/**
	 * This method is called to unregister this event registered through {@link #register(Trigger)}.
	 * 
	 * @param t the same trigger which was registered for this event
	 */
	public abstract void unregister(final Trigger t);
	
	/**
	 * This method is called to unregister all events registered through {@link #register(Trigger)}. This is called on all registered events, thus it can also only unregister the
	 * event it is called on.
	 */
	public abstract void unregisterAll();
	
	@Override
	public final boolean check(final Event e) {
		throw new UnsupportedOperationException();
	}
	
}
