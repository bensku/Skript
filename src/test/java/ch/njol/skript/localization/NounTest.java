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
package ch.njol.skript.localization;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.njol.util.NonNullPair;

/**
 * @author Peter Güttinger
 */
public class NounTest {
	
	@Test
	public void testGetPlural() {
		final String[][] tests = {
				{"a", "a", "a"},
				{"a¦b", "a", "ab"},
				{"a¦b¦c", "ab", "ac"},
				{"a¦b¦c¦d", "abd", "acd"},
				{"a¦b¦c¦d¦e", "abd", "acde"},
				{"a¦b¦c¦d¦e¦f", "abde", "acdf"},
				{"a¦b¦c¦d¦e¦f¦g", "abdeg", "acdfg"},
		};
		for (final String[] test : tests) {
			@SuppressWarnings("null")
			final NonNullPair<String, String> p = Noun.getPlural(test[0]);
			assertEquals(test[1], p.getFirst());
			assertEquals(test[2], p.getSecond());
		}
	}
	
	@SuppressWarnings("null")
	@Test
	public void testNormalizePluralMarkers() {
		final String[][] tests = {
				{"a", "a"},
				{"a¦b", "a¦¦b¦"},
				{"a¦b¦c", "a¦b¦c¦"},
				{"a¦b¦c¦d", "a¦b¦c¦d"},
				{"a¦b¦c¦d¦e", "a¦b¦c¦d¦¦e¦"},
				{"a¦b¦c¦d¦e¦f", "a¦b¦c¦d¦e¦f¦"},
				{"a¦b¦c¦d¦e¦f¦g", "a¦b¦c¦d¦e¦f¦g"},
		};
		for (final String[] test : tests) {
			assertEquals(test[1], Noun.normalizePluralMarkers(test[0]));
			assertEquals(test[1] + "@x", Noun.normalizePluralMarkers(test[0] + "@x"));
		}
	}
	
}
