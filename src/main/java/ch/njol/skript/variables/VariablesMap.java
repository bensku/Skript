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

package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.DatabaseStorage.Type;
import ch.njol.skript.variables.SerializedVariable.Value;
import ch.njol.util.Closeable;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.SynchronizedReference;
import ch.njol.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public abstract class Variables {
	private Variables() {}
	
	public final static short YGGDRASIL_VERSION = 1;
	
	public final static Yggdrasil yggdrasil = new Yggdrasil(YGGDRASIL_VERSION);
	
	private final static String configurationSerializablePrefix = "ConfigurationSerializable_";
	static {
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
		yggdrasil.registerClassResolver(new ConfigurationSerializer<ConfigurationSerializable>() {
			{
				init(); // separate method for the annotation
			}
			
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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

final class VariablesMap {
	
	final HashMap<String, Object> hashMap = new HashMap<>();
	final LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * 
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	final Object getVariable(final String name) {
		if (!name.endsWith("*")) {
			return hashMap.get(name);
		} else {
			final String[] split = Variables.splitVariableName(name);
			Map<String, Object> current = linkedHashMap;
			for (int i = 0; i < split.length; i++) {
				final String n = split[i];
				if (n.equals("*")) {
					assert i == split.length - 1;
					return current;
				}
				final Object o = current.get(n);
				if (o == null)
					return null;
				if (o instanceof Map) {
					current = (Map<String, Object>) o;
					assert i != split.length - 1;
				} else {
					return null;
				}
			}
			return null;
		}
	}
	
	/**
	 * Sets a variable.
	 * 
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	@SuppressWarnings("unchecked")
	final void setVariable(final String name, final @Nullable Object value) {
		if (!name.endsWith("*")) {
			if (value == null)
				hashMap.remove(name);
			else
				hashMap.put(name, value);
		}
		final String[] split = Variables.splitVariableName(name);
		LinkedHashMap<String, Object> parent = linkedHashMap;
		for (int i = 0; i < split.length; i++) {
			final String n = split[i];
			Object current = parent.get(n);
			if (current == null) {
				if (i == split.length - 1) {
					if (value != null)
						parent.put(n, value);
					break;
				} else if (value != null) {
					parent.put(n, current = new LinkedHashMap<>());
					parent = (LinkedHashMap<String, Object>) current;
				} else {
					break;
				}
			} else if (current instanceof LinkedHashMap) {
				if (i == split.length - 1) {
					if (value == null)
						((HashMap<String, Object>) current).remove(null);
					else
						((HashMap<String, Object>) current).put(null, value);
					break;
				} else if (i == split.length - 2 && split[i + 1].equals("*")) {
					assert value == null;
					deleteFromHashMap(StringUtils.join(split, Variable.SEPARATOR, 0, i + 1), (HashMap<String, Object>) current);
					final Object v = ((HashMap<String, Object>) current).get(null);
					if (v == null)
						parent.remove(n);
					else
						parent.put(n, v);
					break;
				} else {
					parent = (LinkedHashMap<String, Object>) current;
				}
			} else {
				if (i == split.length - 1) {
					if (value == null)
						parent.remove(n);
					else
						parent.put(n, value);
					break;
				} else if (value != null) {
					final LinkedHashMap<String, Object> c = new LinkedHashMap<>();
					c.put(null, current);
					parent.put(n, c);
					parent = c;
				} else {
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void deleteFromHashMap(final String parent, final LinkedHashMap<String, Object> current) {
		for (final Entry<String, Object> e : current.entrySet()) {
			if (e.getKey() == null)
				continue;
			hashMap.remove(parent + Variable.SEPARATOR + e.getKey());
			final Object val = e.getValue();
			if (val instanceof LinkedHashMap) {
				deleteFromHashMap(parent + Variable.SEPARATOR + e.getKey(), (LinkedHashMap<String, Object>) val);
			}
		}
	}
	
}

							Skript.info("Loaded " + tvs.size() + " variables so far...");
						else
							break;
					}
				}
			}
		};
		loadingLoggerThread.start();
		
		try {
			boolean successful = true;
			for (final Node node : (SectionNode) databases) {
				if (node instanceof SectionNode) {
					final SectionNode n = (SectionNode) node;
					final String type = n.getValue("type");
					if (type == null) {
						Skript.error("Missing entry 'type' in database definition");
						successful = false;
						continue;
					}
					
					final String name = n.getKey();
					assert name != null;
					final VariablesStorage s;
					if (type.equalsIgnoreCase("csv") || type.equalsIgnoreCase("file") || type.equalsIgnoreCase("flatfile")) {
						s = new FlatFileStorage(name);
					} else if (type.equalsIgnoreCase("mysql")) {
						s = new DatabaseStorage(name, Type.MYSQL);
					} else if (type.equalsIgnoreCase("sqlite")) {
						s = new DatabaseStorage(name, Type.SQLITE);
					} else {
						if (!type.equalsIgnoreCase("disabled") && !type.equalsIgnoreCase("none")) {
							Skript.error("Invalid database type '" + type + "'");
							successful = false;
						}
						continue;
					}
					
					final int x;
					synchronized (tempVars) {
						final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
						assert tvs != null;
						x = tvs.size();
					}
					final long start = System.currentTimeMillis();
					if (Skript.logVeryHigh())
						Skript.info("Loading database '" + node.getKey() + "'...");
					
					if (s.load(n))
						storages.add(s);
					else
						successful = false;
					
					final int d;
					synchronized (tempVars) {
						final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
						assert tvs != null;
						d = tvs.size() - x;
					}
					if (Skript.logVeryHigh())
						Skript.info("Loaded " + d + " variables from the database '" + n.getKey() + "' in " + ((System.currentTimeMillis() - start) / 100) / 10.0 + " seconds");
				} else {
					Skript.error("Invalid line in databases: databases must be defined as sections");
					successful = false;
				}
			}
			if (!successful)
				return false;
			
			if (storages.isEmpty()) {
				Skript.error("No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all.");
				return false;
			}
		} finally {
			// make sure to put the loaded variables into the variables map
			final int n = onStoragesLoaded();
			if (n != 0) {
				Skript.warning(n + " variables were possibly discarded due to not belonging to any database (SQL databases keep such variables and will continue to generate this warning, while CSV discards them).");
			}
			
			loadingLoggerThread.interrupt();
			
			saveThread.start();
		}
		return true;
	}
	
	@SuppressWarnings("null")
	private final static Pattern variableNameSplitPattern = Pattern.compile(Pattern.quote(Variable.SEPARATOR));
	
	@SuppressWarnings("null")
	public final static String[] splitVariableName(final String name) {
		return variableNameSplitPattern.split(name);
	}
	
	private final static ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
	/**
	 * must be locked with {@link #variablesLock}.
	 */
	private final static VariablesMap variables = new VariablesMap();
	/**
	 * Not accessed concurrently
	 */
	private final static WeakHashMap<Event, VariablesMap> localVariables = new WeakHashMap<>();
	
	/**
	 * Remember to lock with {@link #getReadLock()} and to not make any changes!
	 */
	static LinkedHashMap<String, Object> getVariables() {
		return variables.linkedHashMap;
	}
	
	/**
	 * Remember to lock with {@link #getReadLock()}!
	 */
	@SuppressWarnings("null")
	static Map<String, Object> getVariablesHashMap() {
		return Collections.unmodifiableMap(variables.hashMap);
	}
	
	@SuppressWarnings("null")
	static Lock getReadLock() {
		return variablesLock.readLock();
	}
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * 
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@Nullable
	public final static Object getVariable(final String name, final @Nullable Event e, final boolean local) {
		if (local) {
			final VariablesMap map = localVariables.get(e);
			if (map == null)
				return null;
			return map.getVariable(name);
		} else {
			try {
				variablesLock.readLock().lock();
				return variables.getVariable(name);
			} finally {
				variablesLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * Sets a variable.
	 * 
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	public final static void setVariable(final String name, @Nullable Object value, final @Nullable Event e, final boolean local) {
		if (value != null) {
			assert !name.endsWith("::*");
			@SuppressWarnings("null")
			final ClassInfo<?> ci = Classes.getSuperClassInfo(value.getClass());
			final Class<?> sas = ci.getSerializeAs();
			if (sas != null) {
				value = Converters.convert(value, sas);
				assert value != null : ci + ", " + sas;
			}
		}
		if (local) {
			assert e != null : name;
			VariablesMap map = localVariables.get(e);
			if (map == null)
				localVariables.put(e, map = new VariablesMap());
			map.setVariable(name, value);
		} else {
			setVariable(name, value);
		}
	}
	
	final static void setVariable(final String name, @Nullable final Object value) {
		try {
			variablesLock.writeLock().lock();
			variables.setVariable(name, value);
		} finally {
			variablesLock.writeLock().unlock();
		}
		saveVariableChange(name, value);
	}
	
	/**
	 * Stores loaded variables while variable storages are loaded.
	 * <p>
	 * Access must be synchronised.
	 */
	final static SynchronizedReference<Map<String, NonNullPair<Object, VariablesStorage>>> tempVars = new SynchronizedReference<>(new HashMap<>());
	
	private static final int MAX_CONFLICT_WARNINGS = 50;
	private static int loadConflicts = 0;
	
	/**
	 * Sets a variable and moves it to the appropriate database if the config was changed. Must only be used while variables are loaded when Skript is starting.
	 * <p>
	 * Must be called on Bukkit's main thread.
	 * <p>
	 * This method directly invokes {@link VariablesStorage#save(String, String, byte[])}, i.e. you should not be holding any database locks or such when calling this!
	 * 
	 * @param name
	 * @param value
	 * @param source
	 * @return Whether the variable was stored somewhere. Not valid while storages are loading.
	 */
	final static boolean variableLoaded(final String name, final @Nullable Object value, final VariablesStorage source) {
		assert Bukkit.isPrimaryThread(); // required by serialisation
		
		synchronized (tempVars) {
			final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
			if (tvs != null) {
				if (value == null)
					return false;
				final NonNullPair<Object, VariablesStorage> v = tvs.get(name);
				if (v != null && v.getSecond() != source) {// variable already loaded from another database
					loadConflicts++;
					if (loadConflicts <= MAX_CONFLICT_WARNINGS)
						Skript.warning("The variable {" + name + "} was loaded twice from different databases (" + v.getSecond().databaseName + " and " + source.databaseName + "), only the one from " + source.databaseName + " will be kept.");
					else if (loadConflicts == MAX_CONFLICT_WARNINGS + 1)
						Skript.warning("[!] More than " + MAX_CONFLICT_WARNINGS + " variables were loaded more than once from different databases, no more warnings will be printed.");
					v.getSecond().save(name, null, null);
				}
				tvs.put(name, new NonNullPair<>(value, source));
				return false;
			}
		}
		
		variablesLock.writeLock().lock();
		try {
			variables.setVariable(name, value);
		} finally {
			variablesLock.writeLock().unlock();
		}
		
		for (final VariablesStorage s : storages) {
			if (s.accept(name)) {
				if (s != source) {
					final Value v = serialize(value);
					s.save(name, v != null ? v.type : null, v != null ? v.data : null);
					if (value != null)
						source.save(name, null, null);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Stores loaded variables into the variables map and the appropriate databases.
	 * 
	 * @return How many variables were not stored anywhere
	 */
	@SuppressWarnings("null")
	private static int onStoragesLoaded() {
		if (loadConflicts > MAX_CONFLICT_WARNINGS)
			Skript.warning("A total of " + loadConflicts + " variables were loaded more than once from different databases");
		Skript.debug("Databases loaded, setting variables...");
		
		synchronized (tempVars) {
			final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
			tempVars.set(null);
			assert tvs != null;
			variablesLock.writeLock().lock();
			try {
				int n = 0;
				for (final Entry<String, NonNullPair<Object, VariablesStorage>> tv : tvs.entrySet()) {
					if (!variableLoaded(tv.getKey(), tv.getValue().getFirst(), tv.getValue().getSecond()))
						n++;
				}
				
				for (final VariablesStorage s : storages)
					s.allLoaded();
				
				Skript.debug("Variables set. Queue size = " + queue.size());
				
				return n;
			} finally {
				variablesLock.writeLock().unlock();
			}
		}
	}
	
	public final static SerializedVariable serialize(final String name, final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		final SerializedVariable.Value var = serialize(value);
		return new SerializedVariable(name, var);
	}
	
	@Nullable
	public final static SerializedVariable.Value serialize(final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		return Classes.serialize(value);
	}
	
	private final static void saveVariableChange(final String name, final @Nullable Object value) {
		queue.add(serialize(name, value));
	}
	
	final static BlockingQueue<SerializedVariable> queue = new LinkedBlockingQueue<>();
	
	static volatile boolean closed = false;
	
	private final static Thread saveThread = Skript.newThread(new Runnable() {
		@Override
		public void run() {
			while (!closed) {
				try {
					final SerializedVariable v = queue.take();
					for (final VariablesStorage s : storages) {
						if (s.accept(v.name)) {
							s.save(v);
							break;
						}
					}
				} catch (final InterruptedException e) {}
			}
		}
	}, "Skript variable save thread");
	
	public static void close() {
		while (queue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {}
		}
		closed = true;
		saveThread.interrupt();
	}
	
	public static int numVariables() {
		try {
			variablesLock.readLock().lock();
			return variables.hashMap.size();
		} finally {
			variablesLock.readLock().unlock();
		}
	}
	
}
