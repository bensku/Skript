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
package ch.njol.skript.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Mirreducki
 * 
 */
@SuppressWarnings("null")
public class ScriptOptions {

	private HashMap<File, List<String>> localWarningSuppression = new HashMap<>();

	private HashMap<File, Boolean> usesNewLoops = new HashMap<>();

	private static ScriptOptions instance = null;
	
	private ScriptOptions(){
		ScriptOptions.instance = this;
	}

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

	public boolean suppressesWarning(File scriptFile, String warning) {
		if (localWarningSuppression.containsKey(scriptFile))
			return localWarningSuppression.get(scriptFile).contains(warning);
		return false;
	}

	public void setSuppressWarning(File scriptFile, String warning) {
		if (!localWarningSuppression.containsKey(scriptFile))
			localWarningSuppression.put(scriptFile, new ArrayList<>());
		localWarningSuppression.get(scriptFile).add(warning);
	}

}
