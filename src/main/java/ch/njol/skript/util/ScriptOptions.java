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

/**
 * @author Mirreducki
 * 
 */
public class ScriptOptions {
	
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
}
