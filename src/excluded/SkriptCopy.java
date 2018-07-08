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

package ch.njol.skript.mirre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Metrics.Graph;
import ch.njol.skript.Metrics.Plotter;
import ch.njol.skript.Updater.UpdateState;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.bukkitutil.Workarounds;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.classes.data.DefaultConverters;
import ch.njol.skript.classes.data.DefaultFunctions;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.events.EvtSkript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.BukkitLoggerFilter;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.mirre.FeatureConfig;
import ch.njol.skript.mirre.FilterPrintStream;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Closeable;
import ch.njol.util.Kleenean;
import ch.njol.util.NullableChecker;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.EnumerationIterable;

// TODO meaningful error if someone uses an %expression with percent signs% outside of text or a variable

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code (You'll likely be writing some code though if you're reading this
 * =P)
 * <p>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link SimpleExpression expressions}, etc.
 * <p>
 * If your plugin.yml contains <tt>'depend: [Skript]'</tt> then your plugin will not start at all if Skript is not present. Add <tt>'softdepend: [Skript]'</tt> to your plugin.yml
 * if you want your plugin to work even if Skript isn't present, but want to make sure that Skript gets loaded before your plugin.
 * <p>
 * If you use 'softdepend' you can test whether Skript is loaded with <tt>'Bukkit.getPluginManager().getPlugin(&quot;Skript&quot;) != null'</tt>
 * <p>
 * Once you made sure that Skript is loaded you can use <code>Skript.getInstance()</code> whenever you need a reference to the plugin, but you likely won't need it since all API
 * methods are static.
 * 
 * @author Peter Güttinger
 * @see #registerAddon(JavaPlugin)
 * @see #registerCondition(Class, String...)
 * @see #registerEffect(Class, String...)
 * @see #registerExpression(Class, Class, ExpressionType, String...)
 * @see #registerEvent(String, Class, Class, String...)
 * @see EventValues#registerEventValue(Class, Class, Getter, int)
 * @see Classes#registerClass(ClassInfo)
 * @see Comparators#registerComparator(Class, Class, Comparator)
 * @see Converters#registerConverter(Class, Class, Converter)
 */
public final class SkriptCopy extends JavaPlugin implements Listener {
	
	// ================ PLUGIN ================
	
	public static String MIRRE = "V8";
	
	@Nullable
	private static Skript instance = null;
	
	private static boolean disabled = false;
	
	public static Skript getInstance() {
		final Skript i = instance;
		if (i == null)
			throw new IllegalStateException();
		return i;
	}
	
	public Skript() throws IllegalStateException {
		if (instance != null)
			throw new IllegalStateException("Cannot create multiple instances of Skript!");
		instance = this;
	}
	
	@Nullable
	private static Version version = null;
	
	public static Version getVersion() {
		final Version v = version;
		if (v == null)
			throw new IllegalStateException();
		return v;
	}
	
	public final static Message m_invalid_reload = new Message("skript.invalid reload"),
			m_finished_loading = new Message("skript.finished loading");
	
	@Override
	public void onEnable() {
		if (disabled) {
			Skript.error(m_invalid_reload.toString());
			setEnabled(false);
			return;
		}
		
		System.setOut(new FilterPrintStream(System.out));
		
		Language.loadDefault(getAddonInstance());
		
		Workarounds.init();
		
		version = new Version("" + getDescription().getVersion());
		runningCraftBukkit = Bukkit.getServer().getClass().getName().equals("org.bukkit.craftbukkit.CraftServer");
		final String bukkitV = Bukkit.getBukkitVersion();
		final Matcher m = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?").matcher(bukkitV);
		if (!m.find()) {
			Skript.error("The Bukkit version '" + bukkitV + "' does not contain a version number which is required for Skript to enable or disable certain features. " +
					"Skript will still work, but you might get random errors if you use features that are not available in your version of Bukkit.");
			minecraftVersion = new Version(666, 0, 0);
		} else {
			minecraftVersion = new Version("" + m.group());
		}
		
		if (!getDataFolder().isDirectory())
			getDataFolder().mkdirs();
		
		final File scripts = new File(getDataFolder(), SCRIPTSFOLDER);
		if (!scripts.isDirectory()) {
			ZipFile f = null;
			try {
				if (!scripts.mkdirs())
					throw new IOException("Could not create the directory " + scripts);
				f = new ZipFile(getFile());
				for (final ZipEntry e : new EnumerationIterable<ZipEntry>(f.entries())) {
					if (e.isDirectory())
						continue;
					File saveTo = null;
					if (e.getName().startsWith(SCRIPTSFOLDER + "/")) {
						final String fileName = e.getName().substring(e.getName().lastIndexOf('/') + 1);
						saveTo = new File(scripts, (fileName.startsWith("-") ? "" : "-") + fileName);
					} else if (e.getName().equals("config.sk")) {
						final File cf = new File(getDataFolder(), e.getName());
						if (!cf.exists())
							saveTo = cf;
					} else if (e.getName().startsWith("aliases-") && e.getName().endsWith(".sk") && !e.getName().contains("/")) {
						final File af = new File(getDataFolder(), e.getName());
						if (!af.exists())
							saveTo = af;
					} else if (e.getName().startsWith("features.sk")) {
						final File af = new File(getDataFolder(), e.getName());
						if (!af.exists())
							saveTo = af;
					}
					if (saveTo != null) {
						final InputStream in = f.getInputStream(e);
						try {
							assert in != null;
							FileUtils.save(in, saveTo);
						} finally {
							in.close();
						}
					}
				}
				info("Successfully generated the config, the example scripts and the aliases files.");
			} catch (final ZipException e) {} catch (final IOException e) {
				error("Error generating the default files: " + ExceptionUtils.toString(e));
			} finally {
				if (f != null) {
					try {
						f.close();
					} catch (final IOException e) {}
				}
			}
		}
		FeatureConfig.load(getFile());
		
		
		getCommand("skript").setExecutor(new SkriptCommand());
		
		new JavaClasses();
		new BukkitClasses();
		new BukkitEventValues();
		new SkriptClasses();
		
		new DefaultComparators();
		new DefaultConverters();
		new DefaultFunctions();
		
		
		try {
			getAddonInstance().loadClasses("ch.njol.skript", "conditions", "effects", "events", "expressions", "entity");
		} catch (final Exception e) {
			exception(e, "Could not load required .class files: " + e.getLocalizedMessage());
			setEnabled(false);
			return;
		}
		
		SkriptConfig.load();
		Language.setUseLocal(true);
		
		Updater.start();
		
		Aliases.load();
		
		Commands.registerListeners();
		
		if (logNormal())
			info(" " + Language.get("skript.copyright"));
		
		final long tick = testing() ? Bukkit.getWorlds().get(0).getFullTime() : 0;
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				assert Bukkit.getWorlds().get(0).getFullTime() == tick;
				
				// load hooks
				try {
					final JarFile jar = new JarFile(getFile());
					try {
						for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
							if (e.getName().startsWith("ch/njol/skript/hooks/") && e.getName().endsWith("Hook.class") && StringUtils.count("" + e.getName(), '/') <= 5) {
								final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
								try {
									final Class<?> hook = Class.forName(c, true, getClassLoader());
									if (hook != null && Hook.class.isAssignableFrom(hook) && !hook.isInterface() && Hook.class != hook) {
										hook.getDeclaredConstructor().setAccessible(true);
										hook.getDeclaredConstructor().newInstance();
									}
								} catch (final ClassNotFoundException ex) {
									Skript.exception(ex, "Cannot load class " + c);
								} catch (final ExceptionInInitializerError err) {
									Skript.exception(err.getCause(), "Class " + c + " generated an exception while loading");
								}
								continue;
							}
						}
					} finally {
						try {
							jar.close();
						} catch (final IOException e) {}
					}
				} catch (final Exception e) {
					error("Error while loading plugin hooks" + (e.getLocalizedMessage() == null ? "" : ": " + e.getLocalizedMessage()));
					if (testing())
						e.printStackTrace();
				}
				
				Language.setUseLocal(false);
				
				stopAcceptingRegistrations();
				
				FeatureConfig.discard();
				
				Documentation.generate(); // TODO move to test classes?
				
				if (logNormal())
					info("Loading variables...");
				final long vls = System.currentTimeMillis();
				
				final LogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler() {
//					private final List<LogEntry> log = new ArrayList<LogEntry>();
					
					@Override
					public LogResult log(final LogEntry entry) {
						super.log(entry);
						if (entry.level.intValue() >= Level.SEVERE.intValue()) {
							logEx(entry.message); // no [Skript] prefix
							return LogResult.DO_NOT_LOG;
						} else {
//							log.add(entry);
//							return LogResult.CACHED;
							return LogResult.LOG;
						}
					}
					
					@Override
					protected void beforeErrors() {
						logEx();
						logEx("===!!!=== Skript variable load error ===!!!===");
						logEx("Unable to load (all) variables:");
					}
					
					@Override
					protected void afterErrors() {
						logEx();
						logEx("Skript will work properly, but old variables might not be available at all and new ones may or may not be saved until Skript is able to create a backup of the old file and/or is able to connect to the database (which requires a restart of Skript)!");
						logEx();
					}
					
					@Override
					protected void onStop() {
						super.onStop();
//						SkriptLogger.logAll(log);
					}
				});
				final CountingLogHandler c = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));
				try {
					if (!Variables.load())
						if (c.getCount() == 0)
							error("(no information available)");
				} finally {
					c.stop();
					h.stop();
				}
				
				final long vld = System.currentTimeMillis() - vls;
				if (logNormal())
					info("Loaded " + Variables.numVariables() + " variables in " + ((vld / 100) / 10.) + " seconds");
				
				ScriptLoader.loadScripts();
				
				Skript.info(m_finished_loading.toString());
				
				EvtSkript.onSkriptStart();
				
				final Metrics metrics = new Metrics(Skript.this);
				final Graph scriptData = metrics.createGraph("data");
				scriptData.addPlotter(new Plotter("scripts") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedScripts();
					}
				});
				scriptData.addPlotter(new Plotter("triggers") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedTriggers();
					}
				});
				scriptData.addPlotter(new Plotter("commands") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedCommands();
					}
				});
				scriptData.addPlotter(new Plotter("functions") {
					@Override
					public int getValue() {
						return ScriptLoader.loadedFunctions();
					}
				});
				scriptData.addPlotter(new Plotter("variables") {
					@Override
					public int getValue() {
						return Variables.numVariables();
					}
				});
				final Graph language = metrics.createGraph("language");
				language.addPlotter(new Plotter() {
					@Override
					public int getValue() {
						return 1;
					}
					
					@Override
					public String getColumnName() {
						return Language.getName();
					}
				});
				final Graph similarPlugins = metrics.createGraph("similar plugins");
				for (final String plugin : new String[] {"VariableTriggers", "CommandHelper", "Denizen", "rTriggers", "kTriggers", "TriggerCmds", "BlockScripts", "ScriptBlock", "buscript", "BukkitScript"}) {
					assert plugin != null;
					similarPlugins.addPlotter(new Plotter(plugin) {
						@Override
						public int getValue() {
							return Bukkit.getPluginManager().getPlugin(plugin) != null ? 1 : 0;
						}
					});
				}
				metrics.start();
				Skript.metrics = metrics;
				
				// suppresses the "can't keep up" warning after loading all scripts
				final Filter f = new Filter() {
					@Override
					public boolean isLoggable(final @Nullable LogRecord record) {
						if (record == null)
							return false;
						if (record.getMessage() != null && record.getMessage().toLowerCase().startsWith("can't keep up!"))
							return false;
						return true;
					}
				};
				BukkitLoggerFilter.addFilter(f);
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.this, new Runnable() {
					@Override
					public void run() {
						BukkitLoggerFilter.removeFilter(f);
					}
				}, 1);
			}
		});
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(final PlayerJoinEvent e) {
				if (e.getPlayer().hasPermission("skript.admin")) {
					new Task(Skript.this, 0) {
						@Override
						public void run() {
							Updater.stateLock.readLock().lock();
							try {
								final Player p = e.getPlayer();
								assert p != null;
								if ((Updater.state == UpdateState.CHECKED_FOR_UPDATE || Updater.state == UpdateState.DOWNLOAD_ERROR) && Updater.latest.get() != null)
									info(p, "" + Updater.m_update_available);
							} finally {
								Updater.stateLock.readLock().unlock();
							}
						}
					};
				}
			}
		}, this);
		
		
		Bukkit.getScheduler().runTaskAsynchronously(getInstance(), new Runnable(){

			@Override
			public void run() {
				String s = getMirreVersion();
				if(!s.equalsIgnoreCase(MIRRE)){
					Skript.warning("A new version of Skript Fixes has been found. Skript 2.2 Fixes " + s + " has been released. It's recommended to try the latest version.");
				}
			}
			
		});
	}
	
	@SuppressWarnings("null")
	static String getMirreVersion(){
		try {
	      URL url = new URL("http://mirre.eu.pn/version/");
	      Scanner scanner = new Scanner(url.openStream());
	      String str = "";
	      while (scanner.hasNext()) {
	          str = str + scanner.next();
	      }
	      scanner.close();
	      return str;
	    }
	    catch (IOException ex) {
	    	
	    }
	    return null;
	}
	
	private static Version minecraftVersion = new Version(666);
	private static boolean runningCraftBukkit = false;
	
	public static Version getMinecraftVersion() {
		return minecraftVersion;
	}
	
	/**
	 * @return Whether this server is running CraftBukkit
	 */
	public static boolean isRunningCraftBukkit() {
		return runningCraftBukkit;
	}
	
	/**
	 * @return Whether this server is running Minecraft <tt>major.minor</tt> or higher
	 */
	public static boolean isRunningMinecraft(final int major, final int minor) {
		return minecraftVersion.compareTo(major, minor) >= 0;
	}
	
	public static boolean isRunningMinecraft(final int major, final int minor, final int revision) {
		return minecraftVersion.compareTo(major, minor, revision) >= 0;
	}
	
	public static boolean isRunningMinecraft(final Version v) {
		return minecraftVersion.compareTo(v) >= 0;
	}
	
	/**
	 * Used to test whether certain Bukkit features are supported.
	 * 
	 * @param className
	 * @return Whether the given class exists.
	 * @deprecated use {@link #classExists(String)}
	 */
	@Deprecated
	public final static boolean supports(final String className) {
		return classExists(className);
	}
	
	/**
	 * Tests whether a given class exists in the classpath.
	 * 
	 * @param className The {@link Class#getCanonicalName() canonical name} of the class
	 * @return Whether the given class exists.
	 */
	public final static boolean classExists(final String className) {
		try {
			Class.forName(className);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Tests whether a method exists in the given class.
	 * 
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @return Whether the given method exists.
	 */
	public final static boolean methodExists(final Class<?> c, final String methodName, final Class<?>... parameterTypes) {
		try {
			c.getDeclaredMethod(methodName, parameterTypes);
			return true;
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}
	
	/**
	 * Tests whether a method exists in the given class, and whether the return type matches the expected one.
	 * <p>
	 * Note that this method doesn't work properly if multiple methods with the same name and parameters exist but have different return types.
	 * 
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @param returnType The expected return type
	 * @return Whether the given method exists.
	 */
	public final static boolean methodExists(final Class<?> c, final String methodName, final Class<?>[] parameterTypes, final Class<?> returnType) {
		try {
			final Method m = c.getDeclaredMethod(methodName, parameterTypes);
			return m.getReturnType() == returnType;
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}
	
	/**
	 * Tests whether a field exists in the given class.
	 * 
	 * @param c The class
	 * @param fieldName The name of the field
	 * @return Whether the given field exists.
	 */
	public final static boolean fieldExists(final Class<?> c, final String fieldName) {
		try {
			c.getDeclaredField(fieldName);
			return true;
		} catch (final NoSuchFieldException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}
	
	@Nullable
	static Metrics metrics;
	
	@Nullable
	public static Metrics getMetrics() {
		return metrics;
	}
	
	/**
	 * Clears triggers, commands, functions and variable names
	 */
	final static void disableScripts() {
		VariableString.variableNames.clear();
		SkriptEventHandler.removeAllTriggers();
		Commands.clearCommands();
		Functions.clearFunctions();
	}
	
	/**
	 * Prints errors from reloading the config & scripts
	 */
	final static void reload() {
		disableScripts();
		reloadMainConfig();
		reloadAliases();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadScripts() {
		disableScripts();
		ScriptLoader.loadScripts();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadMainConfig() {
		SkriptConfig.load();
	}
	
	/**
	 * Prints errors
	 */
	final static void reloadAliases() {
		Aliases.clear();
		Aliases.load();
	}
	
	@SuppressWarnings("null")
	private final static Collection<Closeable> closeOnDisable = Collections.synchronizedCollection(new ArrayList<Closeable>());
	
	/**
	 * Registers a Closeable that should be closed when this plugin is disabled.
	 * <p>
	 * All registered Closeables will be closed after all scripts have been stopped.
	 * 
	 * @param closeable
	 */
	public static void closeOnDisable(final Closeable closeable) {
		closeOnDisable.add(closeable);
	}
	
	@Override
	public void onDisable() {
		if (disabled)
			return;
		disabled = true;
		
		EvtSkript.onSkriptStop(); // TODO [code style] warn user about delays in Skript stop events
		
		disableScripts();
		
		Bukkit.getScheduler().cancelTasks(this);
		
		for (final Closeable c : closeOnDisable) {
			try {
				c.close();
			} catch (final Exception e) {
				Skript.exception(e, "An error occurred while shutting down.", "This might or might not cause any issues.");
			}
		}
		
		// unset static fields to prevent memory leaks as Bukkit reloads the classes with a different classloader on reload
		// async to not slow down server reload, delayed to not slow down server shutdown
		final Thread t = newThread(new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (final InterruptedException e) {}
				try {
					final Field modifiers = Field.class.getDeclaredField("modifiers");
					modifiers.setAccessible(true);
					final JarFile jar = new JarFile(getFile());
					try {
						for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
							if (e.getName().endsWith(".class")) {
								try {
									final Class<?> c = Class.forName(e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length()), false, getClassLoader());
									for (final Field f : c.getDeclaredFields()) {
										if (Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
											if (Modifier.isFinal(f.getModifiers())) {
												modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
											}
											f.setAccessible(true);
											f.set(null, null);
										}
									}
								} catch (final Throwable ex) {
									if (testing())
										ex.printStackTrace();
								}
							}
						}
					} finally {
						jar.close();
					}
				} catch (final Throwable ex) {
					if (testing())
						ex.printStackTrace();
				}
			}
		}, "Skript cleanup thread");
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		t.start();
	}
	
	// ================ CONSTANTS, OPTIONS & OTHER ================
	
	public final static String SCRIPTSFOLDER = "scripts";
	
	public static void outdatedError() {
		error("Skript v" + getInstance().getDescription().getVersion() + " is not fully compatible with Bukkit " + Bukkit.getVersion() + ". Some feature(s) will be broken until you update Skript.");
	}
	
	public static void outdatedError(final Exception e) {
		outdatedError();
		if (testing())
			e.printStackTrace();
	}
	
	/**
	 * A small value, useful for comparing doubles or floats.
	 * <p>
	 * E.g. to test whether two floating-point numbers are equal:
	 * 
	 * <pre>
	 * Math.abs(a - b) &lt; Skript.EPSILON
	 * </pre>
	 * 
	 * or whether a location is within a specific radius of another location:
	 * 
	 * <pre>
	 * location.distanceSquared(center) - radius * radius &lt; Skript.EPSILON
	 * </pre>
	 * 
	 * @see #EPSILON_MULT
	 */
	public final static double EPSILON = 1e-10;
	/**
	 * A value a bit larger than 1
	 * 
	 * @see #EPSILON
	 */
	public final static double EPSILON_MULT = 1.00001;
	
	/**
	 * The maximum ID a block can have in Minecraft.
	 */
	public final static int MAXBLOCKID = 255;
	/**
	 * The maximum data value of Minecraft, i.e. Short.MAX_VALUE - Short.MIN_VALUE.
	 */
	public final static int MAXDATAVALUE = Short.MAX_VALUE - Short.MIN_VALUE;
	
	// TODO localise Infinity, -Infinity, NaN (and decimal point?)
	public final static String toString(final double n) {
		return StringUtils.toString(n, SkriptConfig.numberAccuracy.value());
	}
	
	public final static UncaughtExceptionHandler UEH = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(final @Nullable Thread t, final @Nullable Throwable e) {
			Skript.exception(e, "Exception in thread " + (t == null ? null : t.getName()));
		}
	};
	
	/**
	 * Creates a new Thread and sets its UncaughtExceptionHandler. The Thread is not started automatically.
	 */
	public final static Thread newThread(final Runnable r, final String name) {
		final Thread t = new Thread(r, name);
		t.setUncaughtExceptionHandler(UEH);
		return t;
	}
	
	// ================ REGISTRATIONS ================
	
	private static boolean acceptRegistrations = true;
	
	public static boolean isAcceptRegistrations() {
		return acceptRegistrations;
	}
	
	public static void checkAcceptRegistrations() {
		if (!acceptRegistrations)
			throw new SkriptAPIException("Registering is disabled after initialisation!");
	}
	
	private static void stopAcceptingRegistrations() {
		acceptRegistrations = false;
		
		Converters.createMissingConverters();
		
		Classes.onRegistrationsStop();
	}
	
	// ================ ADDONS ================
	
	private final static HashMap<String, SkriptAddon> addons = new HashMap<String, SkriptAddon>();
	
	/**
	 * Registers an addon to Skript. This is currently not required for addons to work, but the returned {@link SkriptAddon} provides useful methods for registering syntax elements
	 * and adding new strings to Skript's localization system (e.g. the required "types.[type]" strings for registered classes).
	 * 
	 * @param p The plugin
	 */
	public static SkriptAddon registerAddon(final JavaPlugin p) {
		checkAcceptRegistrations();
		if (addons.containsKey(p.getName()))
			throw new IllegalArgumentException("The plugin " + p.getName() + " is already registered");
		final SkriptAddon addon = new SkriptAddon(p);
		addons.put(p.getName(), addon);
		return addon;
	}
	
	@Nullable
	public static SkriptAddon getAddon(final JavaPlugin p) {
		return addons.get(p.getName());
	}
	
	@Nullable
	public static SkriptAddon getAddon(final String name) {
		return addons.get(name);
	}
	
	@SuppressWarnings("null")
	public static Collection<SkriptAddon> getAddons() {
		return Collections.unmodifiableCollection(addons.values());
	}
	
	@Nullable
	private static SkriptAddon addon;
	
	/**
	 * @return A {@link SkriptAddon} representing Skript.
	 */
	public static SkriptAddon getAddonInstance() {
		final SkriptAddon a = addon;
		if (a == null)
			return addon = new SkriptAddon(Skript.getInstance())
					.setLanguageFileDirectory("lang");
		else
			return a;
	}
	
	// ================ CONDITIONS & EFFECTS ================
	
	private final static Collection<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<SyntaxElementInfo<? extends Condition>>(50);
	private final static Collection<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<SyntaxElementInfo<? extends Effect>>(50);
	private final static Collection<SyntaxElementInfo<? extends Statement>> statements = new ArrayList<SyntaxElementInfo<? extends Statement>>(100);
	
	/**
	 * registers a {@link Condition}.
	 * 
	 * @param condition The condition's class
	 * @param patterns Skript patterns to match this condition
	 */
	@SuppressWarnings("null")
	public static <E extends Condition> void registerCondition(final Class<E> condition, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		
		Pair<Boolean, String[]> pair = FeatureConfig.contains(condition.getSimpleName(), patterns);
		if(pair.getFirst())
			return;
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<E>(pair.getSecond(), condition);
		conditions.add(info);
		statements.add(info);
	}
	
	/**
	 * Registers an {@link Effect}.
	 * 
	 * @param effect The effect's class
	 * @param patterns Skript patterns to match this effect
	 */
	@SuppressWarnings("null")
	public static <E extends Effect> void registerEffect(final Class<E> effect, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		Pair<Boolean, String[]> pair = FeatureConfig.contains(effect.getSimpleName(), patterns);
		if(pair.getFirst())
			return;
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<E>(pair.getSecond(), effect);
		effects.add(info);
		statements.add(info);
	}
	
	public static Collection<SyntaxElementInfo<? extends Statement>> getStatements() {
		return statements;
	}
	
	public static Collection<SyntaxElementInfo<? extends Condition>> getConditions() {
		return conditions;
	}
	
	public static Collection<SyntaxElementInfo<? extends Effect>> getEffects() {
		return effects;
	}
	
	// ================ EXPRESSIONS ================
	
	private final static List<ExpressionInfo<?, ?>> expressions = new ArrayList<ExpressionInfo<?, ?>>(100);
	
	private final static int[] expressionTypesStartIndices = new int[ExpressionType.values().length];
	
	/**
	 * Registers an expression.
	 * 
	 * @param c The expression's class
	 * @param returnType The superclass of all values returned by the expression
	 * @param type The expression's {@link ExpressionType type}. This is used to determine in which order to try to parse expressions.
	 * @param patterns Skript patterns that match this expression
	 * @throws IllegalArgumentException if returnType is not a normal class
	 */
	@SuppressWarnings("null")
	public static <E extends Expression<T>, T> void registerExpression(final Class<E> c, final Class<T> returnType, final ExpressionType type, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		Pair<Boolean, String[]> pair = FeatureConfig.contains(c.getSimpleName(), patterns);
		if(pair.getFirst())
			return;
		if (returnType.isAnnotation() || returnType.isArray() || returnType.isPrimitive())
			throw new IllegalArgumentException("returnType must be a normal type");
		final ExpressionInfo<E, T> info = new ExpressionInfo<E, T>(pair.getSecond(), returnType, c);
		for (int i = type.ordinal() + 1; i < ExpressionType.values().length; i++) {
			expressionTypesStartIndices[i]++;
		}
		expressions.add(expressionTypesStartIndices[type.ordinal()], info);
	}
	
	@SuppressWarnings("null")
	public static Iterator<ExpressionInfo<?, ?>> getExpressions() {
		return expressions.iterator();
	}
	
	public static Iterator<ExpressionInfo<?, ?>> getExpressions(final Class<?>... returnTypes) {
		return new CheckedIterator<ExpressionInfo<?, ?>>(getExpressions(), new NullableChecker<ExpressionInfo<?, ?>>() {
			@Override
			public boolean check(final @Nullable ExpressionInfo<?, ?> i) {
				if (i == null || i.returnType == Object.class)
					return true;
				for (final Class<?> returnType : returnTypes) {
					assert returnType != null;
					if (Converters.converterExists(i.returnType, returnType))
						return true;
				}
				return false;
			}
		});
	}
	
	// ================ EVENTS ================
	
	private final static Collection<SkriptEventInfo<?>> events = new ArrayList<SkriptEventInfo<?>>(50);
	
	/**
	 * Registers an event.
	 * 
	 * @param name Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this). Used for error messages and
	 *            the documentation.
	 * @param c The event's class
	 * @param event The Bukkit event this event applies to
	 * @param patterns Skript patterns to match this event
	 * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event> event, final String... patterns) {
		checkAcceptRegistrations();
		Pair<Boolean, String[]> pair = FeatureConfig.contains(c.getSimpleName(), patterns);
		if(pair.getFirst())
			return new SkriptEventInfo<E>(name, patterns, c, CollectionUtils.array(event));
		final SkriptEventInfo<E> r = new SkriptEventInfo<E>(name, pair.getSecond(), c, CollectionUtils.array(event));
		events.add(r);
		return r;
	}
	
	/**
	 * Registers an event.
	 * 
	 * @param name The name of the event, used for error messages
	 * @param c The event's class
	 * @param events The Bukkit events this event applies to
	 * @param patterns Skript patterns to match this event
	 * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
	 */
	@SuppressWarnings("null")
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event>[] events, final String... patterns) {
		checkAcceptRegistrations();
		Pair<Boolean, String[]> pair = FeatureConfig.contains(c.getSimpleName(), patterns);
		if(pair.getFirst())
			return new SkriptEventInfo<E>(name, patterns, c, events);
		final SkriptEventInfo<E> r = new SkriptEventInfo<E>(name, pair.getSecond(), c, events);
		Skript.events.add(r);
		return r;
	}
	
	public final static Collection<SkriptEventInfo<?>> getEvents() {
		return events;
	}
	
	// ================ COMMANDS ================
	
	/**
	 * Dispatches a command with calling command events
	 * 
	 * @param sender
	 * @param command
	 * @return Whether the command was run
	 */
	public final static boolean dispatchCommand(final CommandSender sender, final String command) {
		try {
			if (sender instanceof Player) {
				final PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
				Bukkit.getPluginManager().callEvent(e);
				if (e.isCancelled() || e.getMessage() == null || !e.getMessage().startsWith("/"))
					return false;
				return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
			} else {
				final ServerCommandEvent e = new ServerCommandEvent(sender, command);
				Bukkit.getPluginManager().callEvent(e);
				if (e.getCommand() == null || e.getCommand().isEmpty())
					return false;
				return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
			}
		} catch (final Exception ex) {
			ex.printStackTrace(); // just like Bukkit
			return false;
		}
	}
	
	// ================ LOGGING ================
	
	public final static boolean logNormal() {
		return SkriptLogger.log(Verbosity.NORMAL);
	}
	
	public final static boolean logHigh() {
		return SkriptLogger.log(Verbosity.HIGH);
	}
	
	public final static boolean logVeryHigh() {
		return SkriptLogger.log(Verbosity.VERY_HIGH);
	}
	
	public final static boolean debug() {
		return SkriptLogger.debug();
	}
	
	public final static boolean testing() {
		return debug() || Skript.class.desiredAssertionStatus();
	}
	
	public final static boolean log(final Verbosity minVerb) {
		return SkriptLogger.log(minVerb);
	}
	
	public static void debug(final String info) {
		if (!debug())
			return;
		SkriptLogger.log(SkriptLogger.DEBUG, info);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void info(final String info) {
		SkriptLogger.log(Level.INFO, info);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void warning(final String warning) {
		SkriptLogger.log(Level.WARNING, warning);
	}
	
	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void error(final @Nullable String error) {
		if (error != null)
			SkriptLogger.log(Level.SEVERE, error);
	}
	
	/**
	 * Use this in {@link Expression#init(Expression[], int, Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)} (and other methods that are called during the parsing) to log
	 * errors with a specific {@link ErrorQuality}.
	 * 
	 * @param error
	 * @param quality
	 */
	public static void error(final String error, final ErrorQuality quality) {
		SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, error));
	}
	
	private final static String EXCEPTION_PREFIX = "#!#! ";
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param info Description of the error and additional information
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public final static EmptyStacktraceException exception(final String... info) {
		return exception(null, info);
	}
	
	public final static EmptyStacktraceException exception(final @Nullable Throwable cause, final String... info) {
		return exception(cause, null, null, info);
	}
	
	public final static EmptyStacktraceException exception(final @Nullable Throwable cause, final @Nullable Thread thread, final String... info) {
		return exception(cause, thread, null, info);
	}
	
	public final static EmptyStacktraceException exception(final @Nullable Throwable cause, final @Nullable TriggerItem item, final String... info) {
		return exception(cause, null, item, info);
	}
	
	/**
	 * Used if something happens that shouldn't happen
	 * 
	 * @param cause exception that shouldn't occur
	 * @param info Description of the error and additional information
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public final static EmptyStacktraceException exception(@Nullable Throwable cause, final @Nullable Thread thread, final @Nullable TriggerItem item, final String... info) {
		
		logEx();
		logEx("[Skript] Severe Error:");
		logEx(info);
		logEx();
		logEx("If you're developing an add-on for Skript this likely means that you have done something wrong.");
		logEx("If you're a server admin however please go to http://dev.bukkit.org/server-mods/skript/tickets/");
		logEx("and check whether this error has already been reported.");
		logEx("If not please create a new ticket with a meaningful title, copy & paste this whole error into it,");
		logEx("and describe what you did before it happened and/or what you think caused the error.");
		logEx("If you think that it's a trigger that's causing the error please post the trigger as well.");
		logEx("By following this guide fixing the error should be easy and done fast.");
		
		logEx();
		logEx("Stack trace:");
		if (cause == null || cause.getStackTrace().length == 0) {
			logEx("  warning: no/empty exception given, dumping current stack trace instead");
			cause = new Exception(cause);
		}
		boolean first = true;
		while (cause != null) {
			logEx((first ? "" : "Caused by: ") + cause.toString());
			for (final StackTraceElement e : cause.getStackTrace())
				logEx("    at " + e.toString());
			cause = cause.getCause();
			first = false;
		}
		
		logEx();
		logEx("Version Information:");
		logEx("  Skript: " + getVersion());
		logEx("  Bukkit: " + Bukkit.getBukkitVersion());
		logEx("  Minecraft: " + getMinecraftVersion());
		logEx("  Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
		logEx("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
		logEx();
		logEx("Running CraftBukkit: " + runningCraftBukkit);
		logEx();
		logEx("Current node: " + SkriptLogger.getNode());
		logEx("Current item: " + (item == null ? "null" : item.toString(null, true)));
		logEx();
		logEx("Thread: " + (thread == null ? Thread.currentThread() : thread).getName());
		logEx();
		logEx("End of Error.");
		logEx();
		
		return new EmptyStacktraceException();
	}
	
	final static void logEx() {
		SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX);
	}
	
	final static void logEx(final String... lines) {
		for (final String line : lines)
			SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX + line);
	}
	
	public static String SKRIPT_PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Skript" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
	
//	static {
//		Language.addListener(new LanguageChangeListener() {
//			@Override
//			public void onLanguageChange() {
//				final String s = Language.get_("skript.prefix");
//				if (s != null)
//					SKRIPT_PREFIX = Utils.replaceEnglishChatStyles(s) + ChatColor.RESET + " ";
//			}
//		});
//	}
	
	public static void info(final CommandSender sender, final String info) {
		sender.sendMessage(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(info));
	}
	
	/**
	 * @param message
	 * @param permission
	 * @see #adminBroadcast(String)
	 */
	public static void broadcast(final String message, final String permission) {
		Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), permission);
	}
	
	public static void adminBroadcast(final String message) {
		Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), "skript.admin");
	}
	
	/**
	 * Similar to {@link #info(CommandSender, String)} but no [Skript] prefix is added.
	 * 
	 * @param sender
	 * @param info
	 */
	public static void message(final CommandSender sender, final String info) {
		sender.sendMessage(Utils.replaceEnglishChatStyles(info));
	}
	
	public static void error(final CommandSender sender, final String error) {
		sender.sendMessage(SKRIPT_PREFIX + ChatColor.DARK_RED + Utils.replaceEnglishChatStyles(error));
	}
	
}
