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
package ch.njol.skript.aliases;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.log.BukkitLoggerFilter;

/**
 * @author Peter Güttinger
 */
public class AliasesTest {
	static {
		BukkitLoggerFilter.addFilter(new Filter() {
			@Override
			public boolean isLoggable(final @Nullable LogRecord record) {
				if (record == null)
					return false;
				return record.getMessage() == null || !record.getMessage().startsWith("[Skript] Missing entry");
			}
		});
	}
	
	// TODO re-enable this test once aliases format is a bit more clear
//	@Test
//	public void testNames() {
//		final ItemType t = new ItemType();
//		t.add(new ItemData(0));
//		
//		final Aliases.Variations v = new Aliases.Variations();
//		final LinkedHashMap<String, ItemType> var1 = new LinkedHashMap<>();
//		var1.put("{default}", t);
//		var1.put("v1.1", t);
//		var1.put("v1.2", t);
//		v.put("var1", var1);
//		final LinkedHashMap<String, ItemType> var2 = new LinkedHashMap<>();
//		var2.put("v2.1 @a", t);
//		var2.put("v2.2", t);
//		v.put("var2", var2);
//		final LinkedHashMap<String, ItemType> var3 = new LinkedHashMap<>();
//		var3.put("v3.1¦¦s¦", t);
//		var3.put("v3.2¦a¦b¦", t);
//		v.put("var3", var3);
//		final LinkedHashMap<String, ItemType> varL = new LinkedHashMap<>();
//		varL.put("{default}", t);
//		varL.put("normales ", t);
//		varL.put("Birken", t);
//		v.put("varL", varL);
//		
//		final String[][] tests = {
//				{"a", "a"},
//				{"a[b]c", "abc", "ac"},
//				{"a [b] c", "a b c", "a c"},
//				{"a(b|c)d", "abd", "acd"},
//				{"a(b|)c", "abc", "ac"},
//				{"a {var1}", "a", "a v1.1", "a v1.2"},
//				{"a {var2} @an", "a v2.1@a", "a v2.2 @an", "a @an"},
//				{"a {var3}", "a v3.1¦¦s¦", "a v3.2¦a¦b¦", "a"},
//				{"<any> a @an", "aliases.any-skp a @-"},
//				{"a <item>", "a ¦item¦items¦"},
//				{"[Holz]Block", "Holzblock", "Block"},
//				{"{varL}Holz", "Holz", "normales Holz", "Birkenholz"}
//		};
//		
//		for (final String[] test : tests) {
//			@SuppressWarnings("null")
//			final Set<String> names = Aliases.getAliases(test[0], t, v).keySet();
//			assertEquals(test[0], test.length - 1, names.size());
//			int i = 1;
//			for (final String name : names)
//				assertEquals(test[0], test[i++], name);
//		}
//	}
	
}
