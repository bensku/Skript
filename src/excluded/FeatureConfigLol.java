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

package ch.njol.skript.mirre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Pair;
import ch.njol.util.coll.iterator.EnumerationIterable;
import ch.njol.util.coll.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 *
 */
public class FeatureConfigLol {
	
	private static boolean loaded = false;
	private static boolean debug = false;
	
	private static List<String> disabledClassNames = new ArrayList<String>();
	private static List<String> disabledPatterns = new ArrayList<String>();
	
	private static Map<String, String> alteredPatterns = new HashMap<String, String>();
	
	public final static OptionSection features = new OptionSection("features");
	public final static OptionSection alterSyntax = new OptionSection("alterSyntax");
	
	public static Pair<Boolean, String[]> contains(String className, String... patterns){
		
		if(disabledClassNames.contains(className)){
			if(debug)
				Skript.info("Disabling feature " + className + " through the Features.sk config.");
			return new Pair<Boolean, String[]>(true, patterns);
		}
		if(patterns != null){
			for(int i = 0 ; i < patterns.length ; i++){
				if(alteredPatterns.containsKey(patterns[i])){
					if(debug)
						Skript.info("Altering the pattern" + patterns[i] + " to " + alteredPatterns.get(patterns[i]) + ".");
					patterns[i] = alteredPatterns.get(patterns[i]);
					return new Pair<Boolean, String[]>(false, patterns);
				}
				if(disabledPatterns.contains(patterns[i])){
					if(debug)
						Skript.info("Disabling the feature " + className + " which had the exact pattern: " + patterns[i] + ".");
					return new Pair<Boolean, String[]>(true, patterns);
				}
			}
		}
		return new Pair<Boolean, String[]>(false, patterns);
	}
	

	
	@SuppressWarnings("null")
	public static void load(File f){
		if(loaded)
			return;
		loaded = true;
		File featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
		Config mc = null;
		if(featureFile.exists()){
			try {
				mc = new Config(featureFile, false, false, ":");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			ZipFile zip = null;
			try{
				zip = new ZipFile(f);
				File saveTo = null;
				ZipEntry entry = zip.getEntry("features.sk");
				if(entry != null){
					final File af = new File(Skript.getInstance().getDataFolder(), entry.getName());
					if (!af.exists())
						saveTo = af;
				}if (saveTo != null) {
					final InputStream in = zip.getInputStream(entry);
					try {
						assert in != null;
						FileUtils.save(in, saveTo);
					} finally {
						in.close();
					}
				}
			}catch (final ZipException e1) {} catch (final IOException e2) {}
			finally {
				if (zip != null) {
					try {
						zip.close();
					} catch (final IOException e3) {}
				}
				featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
				try {
					mc = new Config(featureFile, false, false, ":");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		if(mc != null){
			mc.load(FeatureConfigLol.class);
			
			
			if(mc.get("Version") == null || !mc.get("Version").equalsIgnoreCase(Skript.MIRRE)){
				Skript.warning("Your features.sk config file might by outdated. Delete your features.sk in the Skript folder to renew the config file.");
			}
			if(mc.get("DEBUG").equalsIgnoreCase("true"))
				debug = true;
			
			SectionNode node = (SectionNode) mc.getMainNode().get("features");
			if(node != null && !node.isEmpty()){
				for(Node n : new IteratorIterable<Node>(node.iterator())){		
					if(n.getKey().startsWith("Feature")){
						disabledPatterns.add(mc.get("features", n.getKey()));
					}else{
						disabledClassNames.add(n.getKey());
						String s = mc.get("features", n.getKey());
						if(!s.equalsIgnoreCase("null"))
							disabledPatterns.add(s);
					}
				}
			}
			SectionNode alterNode = (SectionNode) mc.getMainNode().get("alterSyntax");
			if(alterNode != null && !alterNode.isEmpty()){
				for(Node n : new IteratorIterable<Node>(alterNode.iterator())){	
					alteredPatterns.put(n.getKey(), mc.get("alterSyntax", n.getKey()));
				}
			}
		}
	}
	
	public static void discard(){
		if(loaded){
			disabledPatterns.clear();
			disabledClassNames.clear();
		}
	}
}
