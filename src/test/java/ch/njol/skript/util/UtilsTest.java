/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.util;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Güttinger
 */
public class UtilsTest {

	@Test
	@SuppressWarnings("null")
	public void testPlural() {

		final String[][] strings = {
			{"house", "houses"},
			{"cookie", "cookies"},
			{"creeper", "creepers"},
			{"cactus", "cacti"},
			{"rose", "roses"},
			{"dye", "dyes"},
			{"name", "names"},
			{"ingot", "ingots"},
			{"derp", "derps"},
			{"sheep", "sheep"},
			{"choir", "choirs"},
			{"man", "men"},
			{"child", "children"},
			{"hoe", "hoes"},
			{"toe", "toes"},
			{"hero", "heroes"},
			{"kidney", "kidneys"},
			{"anatomy", "anatomies"},
			{"axe", "axes"},
			{"elf", "elfs"},
			{"knife", "knives"},
			{"shelf", "shelfs"},
		};

		for (final String[] s : strings) {
			assertEquals(s[1], Utils.toEnglishPlural(s[0]));
			assertEquals(s[0], Utils.getEnglishPlural(s[1]).getFirst());
		}

	}

	@Test
	@SuppressWarnings("null")
	public void testSuperClass() {

		final Class<?>[][] classes = {
			{Object.class, Object.class},
			{String.class, String.class},
			{String.class, Object.class, Object.class},
			{Object.class, String.class, Object.class},
			{String.class, String.class, String.class},
			{Object.class, String.class, Object.class, String.class, Object.class},
			{Double.class, Integer.class, Number.class},
			{UnknownHostException.class, FileNotFoundException.class, IOException.class},
			{SortedMap.class, TreeMap.class, SortedMap.class},
			{LinkedList.class, ArrayList.class, AbstractList.class},
			{List.class, Set.class, Collection.class},
			{ArrayList.class, Set.class, Collection.class},
		};

		for (final Class<?>[] cs : classes) {
			assertEquals(cs[cs.length - 1], Utils.getSuperType(Arrays.copyOf(cs, cs.length - 1)));
		}

	}

}
