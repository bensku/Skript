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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.Setter;

/**
 * FIXME rename
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class Aliases {
	
	private final static boolean newPotions = Skript.isRunningMinecraft(1, 9);
	
	/**
	 * Note to self: never use this, use {@link #getAlias_i(String)} instead.
	 */
	private final static HashMap<String, ItemType> aliases_english = new HashMap<String, ItemType>(10000);
	private final static HashMap<String, ItemType> aliases_localised = new HashMap<String, ItemType>(1000);
	
	private static HashMap<String, ItemType> getAliases() {
		return Language.isUsingLocal() ? aliases_localised : aliases_english;
	}
	
	@Nullable
	private static ItemType getAlias_i(final String s) {
		final ItemType t = ScriptLoader.getScriptAliases().get(s);
		if (t != null)
			return t;
		return getAliases().get(s);
	}
	
	private final static HashMap<Integer, MaterialName> materialNames_english = new HashMap<Integer, MaterialName>(Material.values().length);
	private final static HashMap<Integer, MaterialName> materialNames_localised = new HashMap<Integer, MaterialName>(Material.values().length);
	
	private static HashMap<Integer, MaterialName> getMaterialNames() {
		return Language.isUsingLocal() ? materialNames_localised : materialNames_english;
	}
	
	static String itemSingular = "item";
	static String itemPlural = "items";
	@Nullable
	static String itemGender = null;
	static String blockSingular = "block";
	static String blockPlural = "blocks";
	@Nullable
	static String blockGender = null;
	
	// this is not an alias!
	private final static ItemType everything = new ItemType();
	static {
		everything.setAll(true);
		everything.add(new ItemData());
	}
	
	private final static Message m_brackets_error = new Message("aliases.brackets error");
	private final static ArgsMessage m_invalid_brackets = new ArgsMessage("aliases.invalid brackets");
	private final static ArgsMessage m_empty_alias = new ArgsMessage("aliases.empty alias");
	private final static ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
	private final static Message m_starting_with_number = new Message("aliases.starting with number");
	private final static Message m_missing_aliases = new Message("aliases.missing aliases");
	private final static Message m_empty_string = new Message("aliases.empty string");
	private final static ArgsMessage m_invalid_item_data = new ArgsMessage("aliases.invalid item data");
	private final static ArgsMessage m_invalid_id = new ArgsMessage("aliases.invalid id");
	private final static Message m_invalid_block_data = new Message("aliases.invalid block data");
	private final static ArgsMessage m_invalid_item_type = new ArgsMessage("aliases.invalid item type");
	private final static ArgsMessage m_out_of_data_range = new ArgsMessage("aliases.out of data range");
	private final static Message m_invalid_range = new Message("aliases.invalid range");
	private final static ArgsMessage m_invalid_section = new ArgsMessage("aliases.invalid section");
	private final static ArgsMessage m_section_not_found = new ArgsMessage("aliases.section not found");
	private final static ArgsMessage m_not_a_section = new ArgsMessage("aliases.not a section");
	private final static Message m_unexpected_non_variation_section = new Message("aliases.unexpected non-variation section");
	private final static Message m_unexpected_section = new Message("aliases.unexpected section");
	private final static ArgsMessage m_loaded_x_aliases_from = new ArgsMessage("aliases.loaded x aliases from");
	private final static ArgsMessage m_loaded_x_aliases = new ArgsMessage("aliases.loaded x aliases");
	
	final static class Variations extends HashMap<String, HashMap<String, ItemType>> {
		private final static long serialVersionUID = -139481665727386819L;
	}
	
	private static int nextBracket(final String s, final char closingBracket, final char openingBracket, final int start) {
		int n = 0;
		assert s.charAt(start) == openingBracket;
		for (int i = start + 1; i < s.length(); i++) {
			if (s.charAt(i) == '\\') {
				i++;
				continue;
			} else if (s.charAt(i) == closingBracket) {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == openingBracket) {
				n++;
			}
		}
		Skript.error(m_invalid_brackets.toString(openingBracket + "" + closingBracket));
		return -1;
	}
	
	/**
	 * Concatenates parts of an alias's name. This currently 'lowercases' the first character of any part if there's no space in front of it. It also replaces double spaces with a
	 * single one and trims the resulting string.
	 * 
	 * @param parts
	 */
	private static String concatenate(final String... parts) {
		assert parts.length >= 2;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isEmpty())
				continue;
			if (b.length() == 0) {
				b.append(parts[i]);
				continue;
			}
			final char c = parts[i].charAt(0);
			if (Character.isUpperCase(c) && b.charAt(b.length() - 1) != ' ') {
				b.append(Character.toLowerCase(c) + parts[i].substring(1));
			} else {
				b.append(parts[i]);
			}
		}
		return "" + b.toString().replace("  ", " ").trim();
	}
	
	/**
	 * @param name Mixedcase string with no whitespace besides spaces and no double spaces.
	 * @param value The alias's value, used for {variations}
	 * @param variations
	 * @return A map containing all parsed aliases
	 */
	static LinkedHashMap<String, ItemType> getAliases(final String name, final ItemType value, final Variations variations) {
		final LinkedHashMap<String, ItemType> r = new LinkedHashMap<String, ItemType>(); // LinkedHashMap to preserve order for item names
		
		if ((name.contains("potion") || name.contains("water bottle") || name.contains("bottle of water")) && newPotions) { // 1.9 new potions hack
			return r;
		}
		
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if ("[({".indexOf(c) != -1) {
				final int end = nextBracket(name, "])}".charAt("[({".indexOf(c)), c, i);
				if (end == -1)
					return r;
				if (c == '[') {
					r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(i + 1, end), name.substring(end + 1)), value, variations));
					r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(end + 1)), value, variations));
				} else if (c == '(') {
					int n = 0;
					int last = i;
					boolean hasParts = false;
					for (int j = i + 1; j < end; j++) {
						final char x = name.charAt(j);
						if (x == '(') {
							n++;
						} else if (x == ')') {
							n--;
						} else if (x == '|') {
							if (n > 0)
								continue;
							hasParts = true;
							r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(last + 1, j), name.substring(end + 1)), value, variations));
							last = j;
						}
					}
					if (!hasParts) {
						Skript.error(m_brackets_error.toString());
						return r;
					}
					r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(last + 1, end), name.substring(end + 1)), value, variations));
				} else {
					assert c == '{';
					continue;
				}
				return r;
			}
		}
		
		// variations and <any>/<block>/etc. are replaced last because they replace genders
		
		int i = name.indexOf('{');
		if (i != -1) {
			final int end = name.indexOf('}', i + 1);
			if (end == -1) {// checked above
				assert false;
				return r;
			}
			final String var = name.substring(i + 1, end);
			if (variations.containsKey(var)) {
				boolean hasDefault = false;
				for (final Entry<String, ItemType> v : variations.get(var).entrySet()) {
					final String n;
					if (v.getKey().equalsIgnoreCase("{default}")) {
						hasDefault = true;
						if (v.getValue() == null)
							continue;
						n = concatenate(name.substring(0, i), name.substring(end + 1));
					} else {
						final int g = v.getKey().lastIndexOf('@');
						if (g == -1) {
							n = concatenate(name.substring(0, i), v.getKey(), name.substring(end + 1));
						} else {
							final String n0 = concatenate(name.substring(0, i), v.getKey().substring(0, g), name.substring(end + 1));
							final int c0 = n0.lastIndexOf('@');
							n = (c0 == -1 ? n0 : n0.substring(0, c0).trim() + v.getKey().substring(g));
						}
					}
					final ItemType t = v.getValue().intersection(value);
					if (t != null)
						r.putAll(getAliases(n, t, variations));
					else
						Skript.warning(m_empty_alias.toString(n));
				}
				if (!hasDefault)
					r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(end + 1)), value, variations));
			} else {
				Skript.error(m_unknown_variation.toString(var));
			}
			return r;
		}
		
		i = name.indexOf('<');
		if (i != -1) {
			final int end = name.indexOf('>', i + 1);
			if (end != -1) {
				final String x = name.substring(i + 1, end);
				if (x.equalsIgnoreCase("any")) {
					String s = name.substring(0, i) + m_any.toString() + name.substring(end + 1);
					final int g = s.lastIndexOf('@');
					if (g != -1)
						s = s.substring(0, g + 1) + "-";
					r.putAll(getAliases(s, value, variations));
					return r;
				} else {
					final String[][] os = {
							{"item", itemSingular, itemPlural, itemGender},
							{"block", blockSingular, blockPlural, blockGender},
							{"item/block", itemSingular, itemPlural, itemGender, blockSingular, blockPlural, blockGender},
							{"block/item", blockSingular, blockPlural, blockGender, itemSingular, itemPlural, itemGender},
					};
					for (final String[] o : os) {
						if (x.equalsIgnoreCase(o[0])) {
							for (int j = 1; j < o.length; j += 3) {
								String s = name.substring(0, i) + "¦" + o[j] + "¦" + o[j + 1] + "¦" + name.substring(end + 1);
								if (o[j + 2] != null) {
									final NonNullPair<String, Integer> p = Noun.stripGender(s, s);
									s = p.getFirst() + "@" + o[j + 2];
								}
								r.put(s, value);
							}
							return r;
						}
					}
				}
			}
		}
		
		r.put(name, value);
		r.remove("");
		return r;
	}
	
	@SuppressWarnings("null")
	private final static Pattern numberWordPattern = Pattern.compile("\\d+\\s+.+");
	
	/**
	 * Parses & adds new aliases
	 * 
	 * @param name mixedcase string
	 * @param value
	 * @param variations
	 * @return amount of added aliases
	 */
	static int addAliases(final String name, final String value, final Variations variations) {
		final ItemType t = parseAlias(value);
		if (t == null) {
			return 0;
		}
		final HashMap<String, ItemType> aliases = getAliases();
		final HashMap<String, ItemType> as = getAliases("" + name.replaceAll("\\s+", " "), t, variations);
		boolean printedStartingWithNumberError = false;
//		boolean printedSyntaxError = false;
		for (final Entry<String, ItemType> e : as.entrySet()) {
			final String s = "" + e.getKey().trim().replaceAll("\\s+", " ");
			final NonNullPair<String, Integer> g = Noun.stripGender(s, "" + e.getKey());
			final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
			final String lcs = p.getFirst().toLowerCase();
			final String lcp = p.getSecond().toLowerCase();
			if (numberWordPattern.matcher(lcs).matches() || numberWordPattern.matcher(lcp).matches()) {
				if (!printedStartingWithNumberError) {
					Skript.error(m_starting_with_number.toString());
					printedStartingWithNumberError = true;
				}
				continue;
			}
//			if (lc.contains(",") || lc.contains(" and ") || lc.contains(" or ")) {
//				if (!printedSyntaxError) {
//					Skript.error("aliases must not contain syntax elements (comma, 'and', 'or')");
//					printedSyntaxError = true;
//				}
//				continue;
//			}
			boolean b;
			final ItemType alias;
			if ((b = lcs.endsWith(itemSingular)) || lcp.endsWith(itemPlural)) {
				String m = b ? lcs.substring(0, lcs.length() - itemSingular.length()) : lcp.substring(0, lcp.length() - itemPlural.length());
				if (m.endsWith(" ") || m.endsWith("-"))
					m = m.substring(0, m.length() - 1);
				final ItemType si = aliases.get(m);
				if (si != null)
					si.setItem(e.getValue());
				alias = e.getValue();
			} else if ((b = lcs.endsWith(blockSingular)) || lcp.endsWith(blockPlural)) {
				String m = b ? lcs.substring(0, lcs.length() - blockSingular.length()) : lcp.substring(0, lcp.length() - blockPlural.length());
				if (m.endsWith(" ") || m.endsWith("-"))
					m = m.substring(0, m.length() - 1);
				final ItemType si = aliases.get(m);
				if (si != null)
					si.setBlock(e.getValue());
				alias = e.getValue();
			} else {
				final ItemType[] ib = new ItemType[2];
				final String[] ibs = {itemSingular, blockSingular};
				final String[] seps = {" ", "", "-"};
				for (int i = 0; i < ib.length; i++) {
					for (final String sep : seps) {
						ib[i] = aliases.get(lcs + sep + ibs[i]);
						if (ib[i] != null)
							break;
					}
				}
				if (ib[0] == null && ib[1] == null) {
					alias = e.getValue();
				} else {
					alias = e.getValue().clone();
					alias.setItem(ib[0]);
					alias.setBlock(ib[1]);
				}
			}
			aliases.put(lcs, alias);
			aliases.put(lcp, alias);
			
			//if (logSpam()) // over 10k aliases in the default aliases.sk as of MC 1.5
			//	info("added alias " + s + " for " + e.getValue());
			
			final HashMap<Integer, MaterialName> materialNames = getMaterialNames();
			
			if (alias.getTypes().size() == 1) {
				final ItemData d = alias.getTypes().get(0);
				MaterialName n = materialNames.get(Integer.valueOf(d.getId()));
				if (d.dataMin == -1 && d.dataMax == -1) {
					if (n != null) {
						if (n.singular.equals("" + d.getId()) && n.singular.equals(n.plural)) {
							n.singular = p.getFirst();
							n.plural = p.getSecond();
						}
					} else {
						materialNames.put(Integer.valueOf(d.getId()), new MaterialName(d.getId(), p.getFirst(), p.getSecond(), g.getSecond()));
					}
				} else {
					if (n == null)
						materialNames.put(Integer.valueOf(d.getId()), n = new MaterialName(d.getId(), "" + d.getId(), "" + d.getId(), g.getSecond()));
					@SuppressWarnings("null")
					final NonNullPair<Short, Short> data = new NonNullPair<Short, Short>(Short.valueOf(d.dataMin), Short.valueOf(d.dataMax));
					n.names.put(data, p);
				}
			}
		}
		return as.size();
	}
	
	/**
	 * Gets the custom name of of a material, or the default if none is set.
	 * 
	 * @param id
	 * @param data
	 * @return The material's name
	 */
	public static String getMaterialName(final int id, final short data, final boolean plural) {
		return getMaterialName(id, data, data, plural);
	}
	
	public static String getDebugMaterialName(final int id, final short data, final boolean plural) {
		return getDebugMaterialName(id, data, data, plural);
	}
	
	public static String getMaterialName(final int id, final short dataMin, final short dataMax, final boolean plural) {
		final MaterialName n = getMaterialNames().get(Integer.valueOf(id));
		if (n == null) {
			return "" + id;
		}
		return n.toString(dataMin, dataMax, plural);
	}
	
	public static String getDebugMaterialName(final int id, final short dataMin, final short dataMax, final boolean plural) {
		final MaterialName n = getMaterialNames().get(Integer.valueOf(id));
		if (n == null) {
			return "" + id + ":" + dataMin + (dataMax == dataMin ? "" : "-" + dataMax);
		}
		return n.getDebugName(dataMin, dataMax, plural);
	}
	
	/**
	 * @return The ietm's gender or -1 if no name is found
	 */
	public static int getGender(final int id, final short dataMin, final short dataMax) {
		final MaterialName n = getMaterialNames().get(Integer.valueOf(id));
		if (n != null)
			return n.gender;
		return -1;
	}
	
	/**
	 * @return how many ids are missing an alias, including the 'any id' (-1)
	 */
	static int addMissingMaterialNames() {
		final HashMap<Integer, MaterialName> materialNames = getMaterialNames();
		int r = 0;
		final StringBuilder missing = new StringBuilder(m_missing_aliases + " ");
		for (final Material m : Material.values()) {
			if (materialNames.get(Integer.valueOf(m.getId())) == null) {
				materialNames.put(Integer.valueOf(m.getId()), new MaterialName(m.getId(), "" + m.toString().toLowerCase().replace('_', ' '), "" + m.toString().toLowerCase().replace('_', ' '), 0));
				missing.append(m.getId() + ", ");
				r++;
			}
		}
		final MaterialName m = materialNames.get(Integer.valueOf(-1));
		if (m == null) {
			materialNames.put(Integer.valueOf(-1), new MaterialName(-1, Language.get("aliases.anything"), Language.get("aliases.anything"), 0));
			missing.append("<any>, ");
			r++;
		}
		if (r > 0)
			Skript.warning("" + missing.substring(0, missing.length() - 2));
		return r;
	}
	
	/**
	 * Parses an ItemType to be used as an alias, i.e. it doesn't parse 'all'/'every' and the amount.
	 * 
	 * @param s mixed case string
	 * @return A new ItemType representing the given value
	 */
	@Nullable
	public static ItemType parseAlias(final String s) {
		if (s.isEmpty()) {
			Skript.error(m_empty_string.toString());
			return null;
		}
		if (s.equals("*"))
			return everything;
		
		final ItemType t = new ItemType();
		
		final String[] types = s.split("\\s*,\\s*");
		for (final String type : types) {
			if (type == null || parseType(type, t, true) == null)
				return null;
		}
		
		return t;
	}
	
	private final static RegexMessage p_any = new RegexMessage("aliases.any", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static Message m_any = new Message("aliases.any-skp");
	private final static RegexMessage p_every = new RegexMessage("aliases.every", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of_every = new RegexMessage("aliases.of every", "(\\d+) ", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of = new RegexMessage("aliases.of", "(\\d+) (?:", " )?(.+)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Parses an ItemType.
	 * <p>
	 * Prints errors.
	 * 
	 * @param s
	 * @return The parsed ItemType or null if the input is invalid.
	 */
	@Nullable
	public static ItemType parseItemType(String s) {
		if (s.isEmpty())
			return null;
		s = "" + s.trim();
		
		final ItemType t = new ItemType();
		
		Matcher m;
		if ((m = p_of_every.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else if ((m = p_of.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			s = "" + m.group(m.groupCount());
		} else if ((m = p_every.matcher(s)).matches()) {
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else {
			final int l = s.length();
			s = Noun.stripIndefiniteArticle(s);
			if (s.length() != l) // had indefinite article
				t.setAmount(1);
		}
		
		final String lc = s.toLowerCase();
		final String of = Language.getSpaced("enchantments.of").toLowerCase();
		int c = -1;
		outer: while ((c = lc.indexOf(of, c + 1)) != -1) {
			final ItemType t2 = t.clone();
			final BlockingLogHandler log = SkriptLogger.startLogHandler(new BlockingLogHandler());
			try {
				if (parseType("" + s.substring(0, c), t2, false) == null)
					continue;
			} finally {
				log.stop();
			}
			if (t2.numTypes() == 0)
				continue;
			final Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			final String[] enchs = lc.substring(c + of.length(), lc.length()).split("\\s*(,|" + Pattern.quote(Language.get("and")) + ")\\s*");
			for (final String ench : enchs) {
				final EnchantmentType e = EnchantmentType.parse("" + ench);
				if (e == null)
					continue outer;
				enchantments.put(e.getType(), e.getLevel());
			}
			t2.addEnchantments(enchantments);
			return t2;
		}
		
		if (parseType(s, t, false) == null)
			return null;
		
		if (t.numTypes() == 0)
			return null;
		
		return t;
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s The string holding the type, can be either a number or an alias, plus an optional data part. Case does not matter.
	 * @param t The ItemType to add the parsed ItemData(s) to (i.e. this ItemType will be modified)
	 * @param isAlias Whether this type is parsed for an alias.
	 * @return The given item type or null if the input couldn't be parsed.
	 */
	@Nullable
	private static ItemType parseType(final String s, final ItemType t, final boolean isAlias) {
		ItemType i;
		int c = s.indexOf(':');
		if (c == -1)
			c = s.length();
		final String type = s.substring(0, c);
		ItemData data = null;
		if (c != s.length()) {
			data = parseData("" + s.substring(c + 1));
			if (data == null) {
				Skript.error(m_invalid_item_data.toString(s.substring(c)));
				return null;
			}
		}
		if (type.isEmpty()) {
			t.add(data == null ? new ItemData() : data);
			return t;
		} else if (type.matches("\\d+")) {
			ItemData d = new ItemData(Utils.parseInt(type));
			if (Material.getMaterial(d.getId()) == null) {
				Skript.error(m_invalid_id.toString(d.getId()));
				return null;
			}
			if (data != null) {
				if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
					Skript.error(m_invalid_block_data.toString());
					return null;
				}
				d = d.intersection(data);
			}
			if (!isAlias && d != null) {
				Skript.warning("Using an ID instead of an alias is discouraged and will likely not be supported in future versions of Skript anymore. " +
						(d.toString().equals(type) ?
								"Please create an alias for '" + type + (type.equals(s) ? "" : " or '" + s + "'") + "' (" + Material.getMaterial(d.getId()).name() + ") in aliases-english.sk or the script's aliases section and use that instead." :
								"Please replace '" + s + "' with e.g. '" + d.toString(true, false) + "'."));
			}
			t.add(d);
			return t;
		} else if ((i = getAlias(type)) != null) {
			for (ItemData d : i) {
				if (data != null) {
					if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
						Skript.error(m_invalid_block_data.toString());
						return null;
					}
					d = d.intersection(data);
				} else {
					d = d.clone();
				}
				t.add(d);
			}
			if (data == null) {
				if (i.getItem() != i)
					t.setItem(i.getItem().clone());
				if (i.getBlock() != i)
					t.setBlock(i.getBlock().clone());
			}
			return t;
		}
		if (isAlias)
			Skript.error(m_invalid_item_type.toString(s));
		return null;
	}
	
	/**
	 * Gets an alias from the aliases defined in the config.
	 * 
	 * @param s The alias to get, case does not matter
	 * @return A copy of the ItemType represented by the given alias or null if no such alias exists.
	 */
	@Nullable
	private static ItemType getAlias(final String s) {
		ItemType i;
		String lc = "" + s.toLowerCase();
		final Matcher m = p_any.matcher(lc);
		if (m.matches()) {
			lc = "" + m.group(m.groupCount());
		}
		if ((i = getAlias_i(lc)) != null)
			return i.clone();
		boolean b;
		if ((b = lc.endsWith(" " + blockSingular)) || lc.endsWith(" " + blockPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? blockSingular.length() : blockPlural.length()) - 1))) != null) {
				i = i.clone();
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getId() > Skript.MAXBLOCKID) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		} else if ((b = lc.endsWith(" " + itemSingular)) || lc.endsWith(" " + itemPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? itemSingular.length() : itemPlural.length()) - 1))) != null) {
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getId() != -1 && d.getId() <= Skript.MAXBLOCKID) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Gets the data part of an item data
	 * 
	 * @param s Everything after ':'
	 * @return ItemData with only the dataMin and dataMax set
	 */
	@Nullable
	private static ItemData parseData(final String s) {
		if (s.isEmpty())
			return new ItemData();
		if (!s.matches("\\d+(-\\d+)?"))
			return null;
		final ItemData t = new ItemData();
		int i = s.indexOf('-');
		if (i == -1)
			i = s.length();
		try {
			t.dataMin = Short.parseShort(s.substring(0, i));
			t.dataMax = (i == s.length() ? t.dataMin : Short.parseShort(s.substring(i + 1, s.length())));
		} catch (final NumberFormatException e) { // overflow
			Skript.error(m_out_of_data_range.toString(Short.MAX_VALUE));
			return null;
		}
		if (t.dataMin > t.dataMax) {
			Skript.error(m_invalid_range.toString());
			return null;
		}
		return t;
	}
	
	public static void clear() {
		aliases_english.clear();
		aliases_localised.clear();
		materialNames_english.clear();
		materialNames_localised.clear();
	}
	
	public static void load() {
		
		final boolean wasLocal = Language.isUsingLocal();
		try {
			for (int l = 0; l < 2; l++) {
				Language.setUseLocal(l == 1);
				if (l == 1 && !Language.isUsingLocal())
					break;
				
				final Config aliasConfig;
				try {
					final File file = new File(Skript.getInstance().getDataFolder(), "aliases-" + Language.getName() + ".sk");
					if (!file.exists()) {
						Skript.error("Could not find the " + Language.getName() + " aliases file " + file.getName());
					}
					aliasConfig = new Config(file, false, true, "=");
				} catch (final IOException e) {
					Skript.error("Could not load the " + Language.getName() + " aliases config: " + e.getLocalizedMessage());
					return;
				}
				
				final ArrayList<String> aliasNodes = new ArrayList<String>();
				
				aliasConfig.validate(
						new SectionValidator()
								.addEntry("aliases", new Setter<String>() {
									@Override
									public void set(final String s) {
										for (final String n : s.split(","))
											aliasNodes.add(n.trim());
									}
								}, false)
								.addEntry("item", new Setter<String>() {
									@Override
									public void set(final String s) {
										final NonNullPair<String, Integer> g = Noun.stripGender(s, "item");
										itemGender = Noun.getGenderID(g.getSecond());
										final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
										itemSingular = "" + p.getFirst().toLowerCase();
										itemPlural = "" + p.getSecond().toLowerCase();
									}
								}, false)
								.addEntry("block", new Setter<String>() {
									@Override
									public void set(final String s) {
										final NonNullPair<String, Integer> g = Noun.stripGender(s, "block");
										blockGender = Noun.getGenderID(g.getSecond());
										final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
										blockSingular = "" + p.getFirst().toLowerCase();
										blockPlural = "" + p.getSecond().toLowerCase();
									}
								}, false)
								.setAllowUndefinedSections(true));
				
				for (final Node node : aliasConfig.getMainNode()) {
					if (node instanceof SectionNode) {
						if (!aliasNodes.contains(node.getKey())) {
							Skript.error(m_invalid_section.toString(node.getKey()));
						}
					}
				}
				
				final Variations variations = new Variations();
				int num = 0;
				for (final String an : aliasNodes) {
					final Node node = aliasConfig.getMainNode().get(an);
					SkriptLogger.setNode(node);
					if (node == null) {
						Skript.error(m_section_not_found.toString(an));
						continue;
					}
					if (!(node instanceof SectionNode)) {
						Skript.error(m_not_a_section.toString(an));
						continue;
					}
					int i = 0;
					for (final Node n : (SectionNode) node) {
						if (n instanceof EntryNode) {
							i += addAliases(((EntryNode) n).getKey(), ((EntryNode) n).getValue(), variations);
						} else if (n instanceof SectionNode) {
							final String key = n.getKey();
							if (key == null) {
								assert false;
								continue;
							}
							if (!(key.startsWith("{") && key.endsWith("}"))) {
								Skript.error(m_unexpected_non_variation_section.toString());
								continue;
							}
							final HashMap<String, ItemType> vs = new HashMap<String, ItemType>();
							for (final Node a : (SectionNode) n) {
								if (a instanceof SectionNode) {
									Skript.error(m_unexpected_section.toString());
									continue;
								} else if (!(a instanceof EntryNode)) {
									continue;
								}
								final boolean noDefault = ((EntryNode) a).getValue().isEmpty() && ((EntryNode) a).getKey().equalsIgnoreCase("{default}");
								final ItemType t = noDefault ? null : parseAlias(((EntryNode) a).getValue());
								if (t != null || noDefault)
									vs.put(Noun.normalizePluralMarkers(((EntryNode) a).getKey()), t);
							}
							variations.put(key.substring(1, key.length() - 1), vs);
						}
					}
					if (Skript.logVeryHigh())
						Skript.info(m_loaded_x_aliases_from.toString(i, node.getKey()));
					num += i;
				}
				SkriptLogger.setNode(null);
				
				if (Skript.logNormal())
					Skript.info(m_loaded_x_aliases.toString(num));
				
				addMissingMaterialNames();
				
//			if (!SkriptConfig.keepConfigsLoaded.value())
//				aliasConfig = null;
				
			}
		} finally {
			Language.setUseLocal(wasLocal);
		}
		
	}
	
}
