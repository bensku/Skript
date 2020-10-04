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
package ch.njol.skript.tests;

import java.util.Map;
import java.util.Set;

/**
 * Contains test results: successes and failures. Can be serialized e.g.
 * with GSON for transfering it between processes.
 */
public class TestResults {
	
	/**
	 * Succeeded tests.
	 */
	private final Set<String> succeeded;
	
	/**
	 * Failed tests.
	 */
	private final Map<String, String> failed;
	
	public TestResults(Set<String> succeeded, Map<String, String> failed) {
		this.succeeded = succeeded;
		this.failed = failed;
	}
	
	public Set<String> getSucceeded() {
		return succeeded;
	}
	
	public Map<String, String> getFailed() {
		return failed;
	}
	
	@SuppressWarnings("null")
	public String createReport() {
		StringBuilder sb = new StringBuilder("Succeeded:\n");
		for (String test : succeeded) {
			sb.append(test).append('\n');
		}
		sb.append("Failed:\n");
		for (Map.Entry<String, String> entry : failed.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
		}
		return sb.toString();
	}
	
}
