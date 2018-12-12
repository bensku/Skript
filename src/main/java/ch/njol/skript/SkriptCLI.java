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

/**
 * This class does not serve much use (only an easter egg for now), but may be of use in the future. The JVM will refuse to run a class that has a superclass outside of the current classpath, hence the creation of this class.
 */
public class SkriptCLI {
	
	public static void main(String[] args) {
		// Use of Skript's internal logging methods create classpath errors.
		System.out.println("[Skript] Skript is a plugin for Bukkit/Spigot, which allows server owners and other people to modify their servers without learning Java. ");
		System.out.println("[Skript] Skript is *not* a standalone application, and we don't understand why you'd try to see if it was.");
		System.out.println("[Skript] ^-^ Have a nice day! ^-^");
	}
	
}
