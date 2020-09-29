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
package ch.njol.skript.events.bukkit;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Getter;

/**
 * @author Peter Güttinger
 */
public class ExperienceSpawnEvent extends Event implements Cancellable {
	static {
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Location.class, new Getter<Location, ExperienceSpawnEvent>() {
			@Override
			public Location get(final ExperienceSpawnEvent e) {
				return e.getLocation();
			}
		}, 0);
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Experience.class, new Getter<Experience, ExperienceSpawnEvent>() {
			@Override
			public Experience get(final ExperienceSpawnEvent e) {
				return new Experience(e.getSpawnedXP());
			}
		}, 0);
	}
	
	private int xp;
	
	private final Location l;
	
	public ExperienceSpawnEvent(final int xp, final Location l) {
		this.xp = xp;
		this.l = l;
	}
	
	public int getSpawnedXP() {
		return xp;
	}
	
	public void setSpawnedXP(final int xp) {
		this.xp = Math.max(0, xp);
	}
	
	public Location getLocation() {
		return l;
	}
	
	private boolean cancelled = false;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(final boolean cancel) {
		cancelled = cancel;
	}
	
	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
