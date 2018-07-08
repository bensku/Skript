/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.config;

/**
 * Not used anymore
 * 
 * @author Peter Güttinger
 */
public class ParseOptionNode extends Node {
	
	ParseOptionNode(final String name, final SectionNode parent, final ConfigReader r) {
		super(name, parent, r);
	}
	
	@Override
	String save() {
		final String option = orig.substring(getOrig().indexOf('[') + 1, orig.indexOf(']'));
		if (orig.startsWith("!separator[")) {
			config.separator = option;
		}
		return orig.trim();
	}
	
}
