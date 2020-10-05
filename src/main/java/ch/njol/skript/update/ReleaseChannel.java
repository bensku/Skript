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

import java.util.function.Function;

/**
 * Allows checking whether releases are in this channel or not.
 */
public class ReleaseChannel {
	
	/**
	 * Used to check whether a release is in this channel.
	 */
	private final Function<String, Boolean> checker;
	
	/**
	 * Release channel name.
	 */
	private final String name;
	
	public ReleaseChannel(Function<String, Boolean> checker, String name) {
		this.checker = checker;
		this.name = name;
	}
	
	/**
	 * Checks whether the release with given name belongs to this channel.
	 * @param release Channel name.
	 * @return Whether the release belongs to channel or not.
	 */
	public boolean check(String release) {
		return checker.apply(release);
	}
	
	/**
	 * Gets release channel name. For example, 'beta'.
	 * @return Channel name.
	 */
	public String getName() {
		return name;
	}
}
