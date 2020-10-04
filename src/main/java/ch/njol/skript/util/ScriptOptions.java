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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Mirreducki
 * 
 */
public final class ScriptOptions {
	
	private HashMap<File, Set<String>> localWarningSuppression = new HashMap<>();
	
	private HashMap<File, Boolean> usesNewLoops = new HashMap<>();
	
	@SuppressWarnings("null")
	private static ScriptOptions instance = null;
	
	private ScriptOptions(){
		ScriptOptions.instance = this;
	}
	
	@SuppressWarnings("null")
	public static ScriptOptions getInstance(){
		return instance != null ? instance : new ScriptOptions();
	}
	
	public boolean usesNewLoops(File file){
		if(usesNewLoops.containsKey(file))
			return usesNewLoops.get(file);
		return true;
	}
	
	public void setUsesNewLoops(File file, boolean b){
		usesNewLoops.put(file, b);
	}
	
	public boolean suppressesWarning(@Nullable File scriptFile, String warning) {
		Set<String> suppressed = localWarningSuppression.get(scriptFile);
		return suppressed != null && suppressed.contains(warning);
	}
	
 	public void setSuppressWarning(@Nullable File scriptFile, String warning) {
 		localWarningSuppression.computeIfAbsent(scriptFile, k -> new HashSet<>()).add(warning);
	}
}
