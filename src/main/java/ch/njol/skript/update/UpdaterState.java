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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.update;


/**
 * State of updater.
 */
public enum UpdaterState {
	
	/**
	 * The updater has not been started.
	 */
	NOT_STARTED,
	
	/**
	 * Update check is currently in progress.
	 */
	CHECKING,
	
	/**
	 * An update is currently being downloaded.
	 */
	DOWNLOADING,
	
	/**
	 * The updater has done something, but is currently not doing anything.
	 */
	INACTIVE,
	
	/**
	 * The updater has encountered an error.
	 */
	ERROR
}
