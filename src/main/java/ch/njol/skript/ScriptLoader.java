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
package ch.njol.skript;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Conditional;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.While;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Callback;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
final public class ScriptLoader {
	private ScriptLoader() {}
	
	private final static Message m_no_errors = new Message("skript.no errors"),
			m_no_scripts = new Message("skript.no scripts");
	private final static PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
	
	@Nullable
	public static Config currentScript = null;
	
	/**
	 * use {@link #setCurrentEvent(String, Class...)}
	 */
	@Nullable
	private static String currentEventName = null;
	
	@Nullable
	public static String getCurrentEventName() {
		return currentEventName;
	}
	
	/**
	 * use {@link #setCurrentEvent(String, Class...)}
	 */
	@Nullable
	private static Class<? extends Event>[] currentEvents = null;
	
	/**
	 * Call {@link #deleteCurrentEvent()} after parsing
	 * 
	 * @param name
	 * @param events
	 */
	public static void setCurrentEvent(final String name, final @Nullable Class<? extends Event>... events) {
		currentEventName = name;
		currentEvents = events;
		hasDelayBefore = Kleenean.FALSE;
	}
	
	public static void deleteCurrentEvent() {
		currentEventName = null;
		currentEvents = null;
		hasDelayBefore = Kleenean.FALSE;
	}
	
	public static List<TriggerSection> currentSections = new ArrayList<TriggerSection>();
	public static List<Loop> currentLoops = new ArrayList<Loop>();
	private final static Map<String, ItemType> currentAliases = new HashMap<String, ItemType>();
	final static HashMap<String, String> currentOptions = new HashMap<String, String>();
	
	public static Map<String, ItemType> getScriptAliases() {
		return currentAliases;
	}
	
	/**
	 * must be synchronized
	 */
	private final static ScriptInfo loadedScripts = new ScriptInfo();
	
	public static Kleenean hasDelayBefore = Kleenean.FALSE;
	
	public static class ScriptInfo {
		public int files, triggers, commands, functions;
		
		public ScriptInfo() {}
		
		public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands, final int numFunctions) {
			files = numFiles;
			triggers = numTriggers;
			commands = numCommands;
			functions = numFunctions;
		}
		
		public void add(final ScriptInfo other) {
			files += other.files;
			triggers += other.triggers;
			commands += other.commands;
			functions += other.functions;
		}
		
		public void subtract(final ScriptInfo other) {
			files -= other.files;
			triggers -= other.triggers;
			commands -= other.commands;
			functions -= other.functions;
		}
	}
	
//	private final static class SerializedScript {
//		public SerializedScript() {}
//
//		public final List<Trigger> triggers = new ArrayList<Trigger>();
//		public final List<ScriptCommand> commands = new ArrayList<ScriptCommand>();
//	}
	
	private static String indentation = "";
	
	static ScriptInfo loadScripts() {
		final File scriptsFolder = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator);
		if (!scriptsFolder.isDirectory())
			scriptsFolder.mkdirs();
		
		final Date start = new Date();
		
		final ScriptInfo i;
		
		final ErrorDescLogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler(null, null, m_no_errors.toString()));
		try {
			Language.setUseLocal(false);
			
			loadStructures(scriptsFolder);
			i = loadScripts(scriptsFolder);
			
			synchronized (loadedScripts) {
				loadedScripts.add(i);
			}
		} finally {
			Language.setUseLocal(true);
			h.stop();
		}
		
		if (i.files == 0)
			Skript.warning(m_no_scripts.toString());
		if (Skript.logNormal() && i.files > 0)
			Skript.info(m_scripts_loaded.toString(i.files, i.triggers, i.commands, start.difference(new Date())));
		
		SkriptEventHandler.registerBukkitEvents();
		
		return i;
	}
	
	/**
	 * Filter for enabled scripts & folders.
	 */
	private final static FileFilter scriptFilter = new FileFilter() {
		@Override
		public boolean accept(final @Nullable File f) {
			return f != null && (f.isDirectory() || StringUtils.endsWithIgnoreCase("" + f.getName(), ".sk")) && !f.getName().startsWith("-");
		}
	};
	
	/**
	 * Loads enabled scripts from the specified directory and it's subdirectories.
	 * 
	 * @param directory
	 * @return Info on the loaded scripts
	 */
	public final static ScriptInfo loadScripts(final File directory) {
		final ScriptInfo i = new ScriptInfo();
		final boolean wasLocal = Language.setUseLocal(false);
		try {
			final File[] files = directory.listFiles(scriptFilter);
			Arrays.sort(files);
			for (final File f : files) {
				if (f.isDirectory()) {
					i.add(loadScripts(f));
				} else {
					i.add(loadScript(f));
				}
			}
		} finally {
			if (wasLocal)
				Language.setUseLocal(true);
		}
		
		return i;
	}
	
	/**
	 * Loads the specified scripts.
	 * 
	 * @param files
	 * @return Info on the loaded scripts
	 */
	public final static ScriptInfo loadScripts(final File[] files) {
		Arrays.sort(files);
		final ScriptInfo i = new ScriptInfo();
		final boolean wasLocal = Language.setUseLocal(false);
		try {
			for (final File f : files) {
				assert f != null : Arrays.toString(files);
				i.add(loadScript(f));
			}
		} finally {
			if (wasLocal)
				Language.setUseLocal(true);
		}
		
		synchronized (loadedScripts) {
			loadedScripts.add(i);
		}
		
		SkriptEventHandler.registerBukkitEvents();
		
		return i;
	}
	
	@SuppressWarnings("unchecked")
	private final static ScriptInfo loadScript(final File f) {
//		File cache = null;
//		if (SkriptConfig.enableScriptCaching.value()) {
//			cache = new File(f.getParentFile(), "cache" + File.separator + f.getName() + "c");
//			if (cache.exists()) {
//				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
//				ObjectInputStream in = null;
//				try {
//					in = new ObjectInputStream(new FileInputStream(cache));
//					final long lastModified = in.readLong();
//					if (lastModified == f.lastModified()) {
//						final SerializedScript script = (SerializedScript) in.readObject();
//						triggersLoop: for (final Trigger t : script.triggers) {
//							if (t.getEvent() instanceof SelfRegisteringSkriptEvent) {
//								((SelfRegisteringSkriptEvent) t.getEvent()).register(t);
//								SkriptEventHandler.addSelfRegisteringTrigger(t);
//							} else {
//								for (final SkriptEventInfo<?> e : Skript.getEvents()) {
//									if (e.c == t.getEvent().getClass()) {
//										SkriptEventHandler.addTrigger(e.events, t);
//										continue triggersLoop;
//									}
//								}
//								throw new EmptyStackException();
//							}
//						}
//						for (final ScriptCommand c : script.commands) {
//							Commands.registerCommand(c);
//						}
//						log.printLog();
//						return new ScriptInfo(1, script.triggers.size(), script.commands.size());
//					} else {
//						cache.delete();
//					}
//				} catch (final Exception e) {
//					if (Skript.testing()) {
//						System.err.println("[debug] Error loading cached script '" + f.getName() + "':");
//						e.printStackTrace();
//					}
//					unloadScript(f);
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e1) {}
//					}
//					cache.delete();
//				} finally {
//					log.stop();
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e) {}
//					}
//				}
//			}
//		}
		try {
			final Config config = new Config(f, true, false, ":");
			if (SkriptConfig.keepConfigsLoaded.value())
				SkriptConfig.configs.add(config);
			int numTriggers = 0;
			int numCommands = 0;
			int numFunctions = 0;
			
			currentAliases.clear();
			currentOptions.clear();
			currentScript = config;
			
//			final SerializedScript script = new SerializedScript();
			
			final CountingLogHandler numErrors = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));
			
			try {
				for (final Node cnode : config.getMainNode()) {
					if (!(cnode instanceof SectionNode)) {
						Skript.error("invalid line - all code has to be put into triggers");
						continue;
					}
					
					final SectionNode node = ((SectionNode) cnode);
					String event = node.getKey();
					if (event == null)
						continue;
					
					if (event.equalsIgnoreCase("aliases")) {
						node.convertToEntries(0, "=");
						for (final Node n : node) {
							if (!(n instanceof EntryNode)) {
								Skript.error("invalid line in aliases section");
								continue;
							}
							final ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
							if (t == null)
								continue;
							currentAliases.put(((EntryNode) n).getKey().toLowerCase(), t);
						}
						continue;
					} else if (event.equalsIgnoreCase("options")) {
						node.convertToEntries(0);
						for (final Node n : node) {
							if (!(n instanceof EntryNode)) {
								Skript.error("invalid line in options");
								continue;
							}
							currentOptions.put(((EntryNode) n).getKey(), ((EntryNode) n).getValue());
						}
						continue;
					} else if (event.equalsIgnoreCase("variables")) {
						// TODO allow to make these override existing variables
						node.convertToEntries(0, "=");
						for (final Node n : node) {
							if (!(n instanceof EntryNode)) {
								Skript.error("Invalid line in variables section");
								continue;
							}
							String name = ((EntryNode) n).getKey().toLowerCase(Locale.ENGLISH);
							if (name.startsWith("{") && name.endsWith("}"))
								name = "" + name.substring(1, name.length() - 1);
							final String var = name;
							name = StringUtils.replaceAll(name, "%(.+)?%", new Callback<String, Matcher>() {
								@Override
								@Nullable
								public String run(final Matcher m) {
									if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
										Skript.error("'" + var + "' is not a valid name for a default variable");
										return null;
									}
									final ClassInfo<?> ci = Classes.getClassInfoFromUserInput("" + m.group(1));
									if (ci == null) {
										Skript.error("Can't understand the type '" + m.group(1) + "'");
										return null;
									}
									return "<" + ci.getCodeName() + ">";
								}
							});
							if (name == null) {
								continue;
							} else if (name.contains("%")) {
								Skript.error("Invalid use of percent signs in variable name");
								continue;
							}
							if (Variables.getVariable(name, null, false) != null)
								continue;
							Object o;
							final ParseLogHandler log = SkriptLogger.startParseLogHandler();
							try {
								o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
								if (o == null) {
									log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
									continue;
								}
								log.printLog();
							} finally {
								log.stop();
							}
							final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
							if (ci.getSerializer() == null) {
								Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
								continue;
							} else if (ci.getSerializeAs() != null) {
								final ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
								if (as == null) {
									assert false : ci;
									continue;
								}
								o = Converters.convert(o, as.getC());
								if (o == null) {
									Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
									continue;
								}
							}
							Variables.setVariable(name, o, null, false);
						}
						continue;
					}
					
					if (!SkriptParser.validateLine(event))
						continue;
					
					if (event.toLowerCase().startsWith("command ")) {
						
						setCurrentEvent("command", CommandEvent.class);
						
						final ScriptCommand c = Commands.loadCommand(node);
						if (c != null) {
							numCommands++;
//							script.commands.add(c);
						}
						
						deleteCurrentEvent();
						
						continue;
					} else if (event.toLowerCase().startsWith("function ")) {
						
						setCurrentEvent("function", FunctionEvent.class);
						
						final Function<?> func = Functions.loadFunction(node);
						if (func != null) {
							numFunctions++;
						}
						
						deleteCurrentEvent();
						
						continue;
					}
					
					if (Skript.logVeryHigh() && !Skript.debug())
						Skript.info("loading trigger '" + event + "'");
					
					if (StringUtils.startsWithIgnoreCase(event, "on "))
						event = "" + event.substring("on ".length());
					
					event = replaceOptions(event);
					
					final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + "'");
					if (parsedEvent == null)
						continue;
					
					if (Skript.debug() || node.debug())
						Skript.debug(event + " (" + parsedEvent.getSecond().toString(null, true) + "):");
					
					setCurrentEvent("" + parsedEvent.getFirst().getName().toLowerCase(Locale.ENGLISH), parsedEvent.getFirst().events);
					final Trigger trigger;
					try {
						trigger = new Trigger(config.getFile(), event, parsedEvent.getSecond(), loadItems(node));
						trigger.setLineNumber(node.getLine()); // Set line number for debugging
						trigger.setDebugLabel(config.getFileName() + ": line " + node.getLine());
					} finally {
						deleteCurrentEvent();
					}
					
					if (parsedEvent.getSecond() instanceof SelfRegisteringSkriptEvent) {
						((SelfRegisteringSkriptEvent) parsedEvent.getSecond()).register(trigger);
						SkriptEventHandler.addSelfRegisteringTrigger(trigger);
					} else {
						SkriptEventHandler.addTrigger(parsedEvent.getFirst().events, trigger);
					}
					
//					script.triggers.add(trigger);
					
					numTriggers++;
				}
				
				if (Skript.logHigh())
					Skript.info("loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + " and " + numCommands + " command" + (numCommands == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
				
				currentScript = null;
			} finally {
				numErrors.stop();
			}
			
//			if (SkriptConfig.enableScriptCaching.value() && cache != null) {
//				if (numErrors.getCount() > 0) {
//					ObjectOutputStream out = null;
//					try {
//						cache.getParentFile().mkdirs();
//						out = new ObjectOutputStream(new FileOutputStream(cache));
//						out.writeLong(f.lastModified());
//						out.writeObject(script);
//					} catch (final NotSerializableException e) {
//						Skript.exception(e, "Cannot cache " + f.getName());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} catch (final IOException e) {
//						Skript.warning("Cannot cache " + f.getName() + ": " + e.getLocalizedMessage());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} finally {
//						if (out != null)
//							out.close();
//					}
//				}
//			}
			
			return new ScriptInfo(1, numTriggers, numCommands, numFunctions);
		} catch (final IOException e) {
			Skript.error("Could not load " + f.getName() + ": " + ExceptionUtils.toString(e));
		} catch (final Exception e) {
			Skript.exception(e, "Could not load " + f.getName());
		} finally {
			SkriptLogger.setNode(null);
		}
		return new ScriptInfo();
	}
	
	/**
	 * Loads the specified scripts.
	 * 
	 * @param files
	 */
	public final static void loadStructures(final File[] files) {
		Arrays.sort(files);
		for (final File f : files) {
			assert f != null : Arrays.toString(files);
			loadStructure(f);
		}
	
		
		SkriptEventHandler.registerBukkitEvents();
	}
	
	/**
	 * Loads structures of all scripts in given directory.
	 * 
	 * @param directory
	 */
	public final static void loadStructures(final File directory) {
		final File[] files = directory.listFiles(scriptFilter);
		Arrays.sort(files);
		for (final File f : files) {
			if (f.isDirectory()) {
				loadStructures(f);
			} else {
				loadStructure(f);
			}
		}
	}
	
	/**
	 * Loads structure of given script, currently only for functions. Must be called before
	 * actually loading that script.
	 * @param f Script
	 */
	@SuppressWarnings("unchecked")
	private final static void loadStructure(final File f) {
		try {
			final Config config = new Config(f, true, false, ":");
			if (SkriptConfig.keepConfigsLoaded.value())
				SkriptConfig.configs.add(config);
			int numTriggers = 0;
			int numCommands = 0;
			int numFunctions = 0;
			
			currentAliases.clear();
			currentOptions.clear();
			currentScript = config;
			
//			final SerializedScript script = new SerializedScript();
			
			final CountingLogHandler numErrors = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));
			
			try {
				for (final Node cnode : config.getMainNode()) {
					if (!(cnode instanceof SectionNode)) {
						Skript.error("invalid line - all code has to be put into triggers");
						continue;
					}
					
					final SectionNode node = ((SectionNode) cnode);
					String event = node.getKey();
					if (event == null)
						continue;
					
					
					if (!SkriptParser.validateLine(event))
						continue;
					
					if (event.toLowerCase().startsWith("function ")) {
						
						setCurrentEvent("function", FunctionEvent.class);
						
						final Signature<?> func = Functions.loadSignature(config.getFileName(), node);
						if (func != null) {
							numFunctions++;
						}
						
						deleteCurrentEvent();
						
						continue;
					}
				}
				
				currentScript = null;
			} finally {
				numErrors.stop();
			}
		} catch (final IOException e) {
			Skript.error("Could not load " + f.getName() + ": " + ExceptionUtils.toString(e));
		} catch (final Exception e) {
			Skript.exception(e, "Could not load " + f.getName());
		} finally {
			SkriptLogger.setNode(null);
		}
	}
	
	/**
	 * Unloads enabled scripts from the specified directory and its subdirectories.
	 * 
	 * @param folder
	 * @return Info on the unloaded scripts
	 */
	final static ScriptInfo unloadScripts(final File folder) {
		final ScriptInfo r = unloadScripts_(folder);
		Functions.validateFunctions();
		return r;
	}
	
	private final static ScriptInfo unloadScripts_(final File folder) {
		final ScriptInfo info = new ScriptInfo();
		final File[] files = folder.listFiles(scriptFilter);
		for (final File f : files) {
			if (f.isDirectory()) {
				info.add(unloadScripts_(f));
			} else if (f.getName().endsWith(".sk")) {
				info.add(unloadScript_(f));
			}
		}
		return info;
	}
	
	/**
	 * Unloads the specified script.
	 * 
	 * @param script
	 * @return Info on the unloaded script
	 */
	final static ScriptInfo unloadScript(final File script) {
		final ScriptInfo r = unloadScript_(script);
		Functions.clearFunctions(script);
		//Functions.validateFunctions();
		return r;
	}
	
	private final static ScriptInfo unloadScript_(final File script) {
		final ScriptInfo info = SkriptEventHandler.removeTriggers(script);
		synchronized (loadedScripts) {
			loadedScripts.subtract(info);
		}
		return info;
	}
	
	public final static String replaceOptions(final String s) {
		final String r = StringUtils.replaceAll(s, "\\{@(.+?)\\}", new Callback<String, Matcher>() {
			@Override
			@Nullable
			public String run(final Matcher m) {
				final String option = currentOptions.get(m.group(1));
				if (option == null) {
					Skript.error("undefined option " + m.group());
					return m.group();
				}
				return option;
			}
		});
		assert r != null;
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<TriggerItem> loadItems(final SectionNode node) {
		
		if (Skript.debug())
			indentation += "    ";
		
		final ArrayList<TriggerItem> items = new ArrayList<TriggerItem>();
		
		Kleenean hadDelayBeforeLastIf = Kleenean.FALSE;
		
		for (final Node n : node) {
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				final SimpleNode e = (SimpleNode) n;
				final String s = replaceOptions("" + e.getKey());
				if (!SkriptParser.validateLine(s))
					continue;
				final Statement stmt = Statement.parse(s, "Can't understand this condition/effect: " + s);
				if (stmt == null)
					continue;
				if (Skript.debug() || n.debug())
					Skript.debug(indentation + stmt.toString(null, true));
				items.add(stmt);
				if (stmt instanceof Delay)
					hasDelayBefore = Kleenean.TRUE;
			} else if (n instanceof SectionNode) {
				String name = replaceOptions("" + n.getKey());
				if (!SkriptParser.validateLine(name))
					continue;
				
				if (StringUtils.startsWithIgnoreCase(name, "loop ")) {
					final String l = "" + name.substring("loop ".length());
					final RetainingLogHandler h = SkriptLogger.startRetainingLog();
					Expression<?> loopedExpr;
					try {
						loopedExpr = new SkriptParser(l).parseExpression(Object.class);
						if (loopedExpr != null)
							loopedExpr = loopedExpr.getConvertedExpression(Object.class);
						if (loopedExpr == null) {
							h.printErrors("Can't understand this loop: '" + name + "'");
							continue;
						}
						h.printLog();
					} finally {
						h.stop();
					}
					if (loopedExpr.isSingle()) {
						Skript.error("Can't loop " + loopedExpr + " because it's only a single value");
						continue;
					}
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "loop " + loopedExpr.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					items.add(new Loop(loopedExpr, (SectionNode) n));
					if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
						hasDelayBefore = Kleenean.UNKNOWN;
				} else if (StringUtils.startsWithIgnoreCase(name, "while ")) {
					final String l = "" + name.substring("while ".length());
					final Condition c = Condition.parse(l, "Can't understand this condition: " + l);
					if (c == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "while " + c.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					items.add(new While(c, (SectionNode) n));
					if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
						hasDelayBefore = Kleenean.UNKNOWN;
				} else if (name.equalsIgnoreCase("else")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
						Skript.error("'else' has to be placed just after an 'if' or 'else if' section");
						continue;
					}
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "else:");
					final Kleenean hadDelayAfterLastIf = hasDelayBefore;
					hasDelayBefore = hadDelayBeforeLastIf;
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
					hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore));
				} else if (StringUtils.startsWithIgnoreCase(name, "else if ")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
						Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
						continue;
					}
					name = "" + name.substring("else if ".length());
					final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + "'");
					if (cond == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "else if " + cond.toString(null, true));
					final Kleenean hadDelayAfterLastIf = hasDelayBefore;
					hasDelayBefore = hadDelayBeforeLastIf;
					((Conditional) items.get(items.size() - 1)).loadElseIf(cond, (SectionNode) n);
					hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore.and(Kleenean.UNKNOWN)));
				} else {
					if (StringUtils.startsWithIgnoreCase(name, "if "))
						name = "" + name.substring(3);
					final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + "'");
					if (cond == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + cond.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					hadDelayBeforeLastIf = hadDelayBefore;
					items.add(new Conditional(cond, (SectionNode) n));
					hasDelayBefore = hadDelayBefore.or(hasDelayBefore.and(Kleenean.UNKNOWN));
				}
			}
		}
		
		for (int i = 0; i < items.size() - 1; i++)
			items.get(i).setNext(items.get(i + 1));
		
		SkriptLogger.setNode(node);
		
		if (Skript.debug())
			indentation = "" + indentation.substring(0, indentation.length() - 4);
		
		return items;
	}
	
	/**
	 * For unit testing
	 * 
	 * @param node
	 * @return The loaded Trigger
	 */
	@Nullable
	static Trigger loadTrigger(final SectionNode node) {
		String event = node.getKey();
		if (event == null) {
			assert false : node;
			return null;
		}
		if (event.toLowerCase().startsWith("on "))
			event = "" + event.substring("on ".length());
		
		final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + "'");
		if (parsedEvent == null) {
			assert false;
			return null;
		}
		
		setCurrentEvent("unit test", parsedEvent.getFirst().events);
		try {
			return new Trigger(null, event, parsedEvent.getSecond(), loadItems(node));
		} finally {
			deleteCurrentEvent();
		}
	}
	
	public final static int loadedScripts() {
		synchronized (loadedScripts) {
			return loadedScripts.files;
		}
	}
	
	public final static int loadedCommands() {
		synchronized (loadedScripts) {
			return loadedScripts.commands;
		}
	}
	
	public final static int loadedFunctions() {
		synchronized (loadedScripts) {
			return loadedScripts.functions;
		}
	}
	
	public final static int loadedTriggers() {
		synchronized (loadedScripts) {
			return loadedScripts.triggers;
		}
	}
	
	public final static boolean isCurrentEvent(final @Nullable Class<? extends Event> event) {
		return CollectionUtils.containsSuperclass(currentEvents, event);
	}
	
	@SafeVarargs
	public final static boolean isCurrentEvent(final Class<? extends Event>... events) {
		return CollectionUtils.containsAnySuperclass(currentEvents, events);
	}
	
	/**
	 * Use this sparingly; {@link #isCurrentEvent(Class)} or {@link #isCurrentEvent(Class...)} should be used in most cases.
	 */
	@Nullable
	public static Class<? extends Event>[] getCurrentEvents() {
		return currentEvents;
	}
	
}
