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

import java.util.concurrent.CompletableFuture;

/**
 * Checks for updates
 */
public interface UpdateChecker {
	
	/**
	 * Checks for updates.
	 * @param manifest Manifest for release which is to be updated.
	 * @param releaseChannel Release channel to use.
	 * @return A future that will contain an update manifest, or null if
	 * there are no updates available currently.
	 */
	CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel);
}
