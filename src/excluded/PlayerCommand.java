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

package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.InvalidNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.VoidNode;
import ch.njol.util.Pair;

public class PlayerCommand implements CommandExecutor {
	
	public Config config = null;
	public Node node = null;
	
	public boolean answered = false;
	public boolean answer = false;
	
	private Thread waiting = null;
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		
		if (!answered && args.length == 1 && (args[0].equalsIgnoreCase("y") || args[0].equalsIgnoreCase("n"))) {
			answer = args[0].equalsIgnoreCase("y");
			answered = true;
			waiting.notify();
			return true;
		}
		
		if (waiting != null && waiting.isAlive())
			waiting.interrupt();
		
		waiting = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				final ArrayList<Pair<String, String>> co = parseCommandOptions(args);
				
				final String action = args[0].toLowerCase();
				final StringBuilder b = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					if (args[i].startsWith("--"))
						break;
					b.append(b.length() == 0 ? "" : " ");
					b.append(args[i]);
				}
				final String actionParams = b.toString();
				
				Verbosity v = Verbosity.LOW;
				
				answered = false;
				answer = false;
				
				boolean ok = false;
				
				boolean save = false;
				
				// --- FLAGS ---
				
				for (final Pair<String, String> p : co) {
					ok = false;
					
					if (p.first.equals("c") || p.first.equals("config")) {
						if (p.second.isEmpty() || p.second.equals("main")) {
							config = Skript.mainConfig;
							node = Skript.mainConfig.getMainNode();
							sender.sendMessage("selectend main config");
							ok = true;
						}
						if (!ok) {
							for (final Config c : Skript.configs) {
								if (c.getFileName().substring(0, c.getFileName().lastIndexOf(".cfg")).equalsIgnoreCase(p.second)) {
									config = c;
									ok = true;
									break;
								}
							}
						}
						if (!ok) {
							final File f = new File(Skript.skript.getDataFolder(), Skript.TRIGGERFILEFOLDER + File.pathSeparator + "-" + p.second + ".cfg");
							if (f.exists()) {
								if (!answered) {
									sender.sendMessage(p.second + " is currently disabled and is therefore not loaded.");
									sender.sendMessage("Do you want to load it (this will not enable it)?");
								}
								if (waitForAnswer()) {
									try {
										sender.sendMessage("loading...");
										Skript.configs.add(config = new Config(f, true, ":"));
										node = config.getMainNode();
										sender.sendMessage("loaded & selected " + f.getName());
									} catch (final IOException e) {
										sender.sendMessage("unable to load " + f.getName());
									}
								}
								ok = true;
							}
						}
						if (!ok) {
							if (!answered)
								sender.sendMessage("file not found. Do you want to create it?");
							if (waitForAnswer()) {
								try {
									final File f = new File(Skript.skript.getDataFolder(), Skript.TRIGGERFILEFOLDER + File.pathSeparator + p.second);
									Skript.configs.add(config = new Config(f, true, ":"));
									node = config.getMainNode();
									sender.sendMessage("created & selected " + f.getName());
								} catch (final IOException e) {
									sender.sendMessage("file creation failed. bad filename?");
								}
							}
						}
					} else if (p.first.equals("s") || p.first.equals("select")) {
						final Node n = node.getNode(p.second);
						if (n == null)
							sender.sendMessage("invalid node in '" + p.second + "'");
						else
							node = n;
					} else if (p.first.equals("save")) {
						save = true;
					} else if (p.first.equals("a") || p.first.equals("accept")) {
						answered = true;
						answer = true;
						continue;
					} else if (p.first.matches("v+")) {
						v = Verbosity.values()[Math.max(p.first.length(), Verbosity.values().length - 1)];
					}
					answered = false;// --a will only skip the question(s) of the next option or the main action if it's the last option.
					answer = false;
				}
				
				// --- ACTIONS ---
				
				if (action != null) {
					if (action.equals("e") || action.equals("enable")
							|| action.equals("d") || action.equals("disable")) {
						final boolean enable = action.startsWith("e");
						final String prefix = (enable ? "en" : "dis");
						if (node.getParent() == null) {
							if (enable ^ !config.isEnabled()) {
								sender.sendMessage("file is already " + prefix + "abled");
								return;
							}
							if (!answered)
								sender.sendMessage(prefix + "abling the file will rename the file on the disk. Do you want to continue?");
							if (waitForAnswer()) {
								if (config.setEnabled(enable)) {
									sender.sendMessage("file " + prefix + "abled");
								} else {
									if (new File(config.getFile(), enable ? config.getFileName().substring(1) : "-" + config.getFileName()).exists()) {
										sender.sendMessage("could not " + prefix + "able the file because a file with that name already exists");
									} else {
										sender.sendMessage("could not " + prefix + "able the file.");
									}
								}
							}
						} else {
							if (node instanceof EntryNode) {
								node.rename(node.getName().startsWith("-") ? node.getName().substring(1) : "-" + node.getName());
								sender.sendMessage("node " + prefix + "abled");
							}
						}
					} else if (action.equals("s") || action.equals("save")) {
						save = true;
					} else if (action.equals("a") || action.equals("add")
							|| action.equals("n") || action.equals("new")) {
						final String[] params = actionParams.split(":", 2);
						if (params.length < 2) {
							sender.sendMessage("usage: /s n key:value|group:");
							return;
						}
						if (!(node instanceof SectionNode)) {
							sender.sendMessage("adding node to parent of selected node.");
							node = node.getParent();
						}
						if (params[1].isEmpty()) {
							((SectionNode) node).getNodeList().add(node = new SectionNode(params[0], (SectionNode) node));
						} else {
							((SectionNode) node).getNodeList().add(node = new EntryNode(params[0], params[1], (SectionNode) node));
						}
						sender.sendMessage("created & selected " + params[0]);
					} else if (action.equals("r") || action.equals("rename")) {
						if (actionParams.isEmpty()) {
							sender.sendMessage("usage: /s r new name");
							return;
						}
						final String oldname = node.getName();
						node.rename(actionParams);
						sender.sendMessage("renamed " + oldname + " to " + node.getName());
					} else if (action.equals("d") || action.equals("delete")) {
						if (!answered)
							sender.sendMessage("do you really want to delete this node?");
						if (waitForAnswer()) {
							node.delete();
						}
					} else if (action.equals("m") || action.equals("move")) {
						if (actionParams.isEmpty()) {
							sender.sendMessage("usage: /s m +move down|-move up|index to insert after|parent node");
						}
						if (actionParams.matches("-[0-9]+")) {
							node.getParent().moveDelta(Integer.parseInt(actionParams));
						} else if (actionParams.matches("\\+[0-9]+")) {
							node.getParent().moveDelta(Integer.parseInt(actionParams.substring(1)));
						} else if (actionParams.matches("[0-9]+")) {
							node.getParent().move(Integer.parseInt(actionParams));
						} else {
							final Node n = node.getNode(actionParams, true);
							if (n == null) {
								sender.sendMessage("invalid path");
								return;
							}
							if (!(n instanceof SectionNode)) {
								node.move((SectionNode) n);
							} else {
								node.move(n.getParent());
								node.move(n.getParent().getNodeList().indexOf(n) + 1);
							}
						}
					} else if (action.equals("l") || action.equals("list")) {
						if (!(node instanceof SectionNode)) {
							sender.sendMessage("selected node is not a section, switching to parent node");
							node = node.getParent();
						}
						int page = 1;
						try {
							page = Math.min((int) Math.ceil(((SectionNode) node).getNodeList().size() / CommandHandler.linesPerPage), Math.max(1, Integer.parseInt(actionParams)));
						} catch (final NumberFormatException e) {}
						sender.sendMessage("&8== subnodes of "
								+ node.getName()
								+ " (page "
								+ page
								+ " of "
								+ Math.ceil((((SectionNode) node).getNodeList().size() - (v.compareTo(Verbosity.HIGH) >= 0 ? ((SectionNode) node).getNumVoidNodes() : 0))
										/ CommandHandler.linesPerPage) + ")");
						int j = 0;
						for (int i = (page - 1) * CommandHandler.linesPerPage; j < CommandHandler.linesPerPage; i++) {
							final Node n = ((SectionNode) node).getNodeList().get(i);
							if (v.compareTo(Verbosity.HIGH) >= 0 && n instanceof VoidNode) {
								continue;
							}
							j++;
							String s;
							if (n.isVoid()) {
								s = n.getOrig();
							} else {
								s = n.getName();
								if (v.compareTo(Verbosity.NORMAL) >= 0) {
									if (node instanceof EntryNode)
										s += ": " + ((EntryNode) n).getValue();
									else if (node instanceof SectionNode)
										s += " [" + ((SectionNode) n).getNodeList().size() + " subnodes]";
								}
							}
							sender.sendMessage("&7" + (i + 1 < 10 ? "0" : "") + (i + 1) + ":" + (n instanceof InvalidNode ? "&4" : "&0") + " " + s);
						}
					}
				}
				
				if (save) {
					try {
						config.save();
						sender.sendMessage(config.getFileName() + " saved sucessfully");
					} catch (final IOException e) {
						sender.sendMessage("error saving " + config.getFileName() + " (see server log)");
						Skript.error("error saving " + config.getFileName() + ":");
						e.printStackTrace();
					}
				}
				
				answered = false;
				answer = false;
				waiting = null;
			}
			
		});
		
		waiting.start();
		
		return true;
	}
	
	private boolean waitForAnswer() {
		while (!answered) {// if --a this is already false
			try {
				Thread.currentThread().wait();
			} catch (final InterruptedException e) {
				answered = false;
				answer = false;
				break;
			}
		}
		return answer;
	}
	
	private static ArrayList<Pair<String, String>> parseCommandOptions(final String[] args) {
		final ArrayList<Pair<String, String>> c = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--")) {
				String o = "";
				final String s = args[i].substring(1).toLowerCase();
				i++;
				while (i < args.length && !args[i].startsWith("--")) {
					o += (o.isEmpty() ? "" : " ") + args[i];
					i++;
				}
				if (i == args.length)
					break;
				i--;
				c.add(new Pair<String, String>(s, o));
			}
		}
		return c;
	}
	
}
