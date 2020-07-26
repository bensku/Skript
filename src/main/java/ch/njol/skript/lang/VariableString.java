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
package ch.njol.skript.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.Config;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ScriptOptions;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;

/**
 * Represents a string that may contain expressions, and is thus "variable".
 * 
 * @author Peter Güttinger
 */
public class VariableString implements Expression<String> {
	
	private final static class ExpressionInfo {
		ExpressionInfo(final Expression<?> expr) {
			this.expr = expr;
		}
		
		final Expression<?> expr;
		int flags = 0;
		boolean toChatStyle = false;
	}
	
	private final String orig;
	
	@Nullable
	private final Object[] string;
	@Nullable
	private Object[] stringUnformatted;
	private final boolean isSimple;
	@Nullable
	private final String simple;
	@Nullable
	private final String simpleUnformatted;
	private final StringMode mode;
	
	/**
	 * Message components that this string consists of. Only simple parts have
	 * been evaluated here.
	 */
	private final MessageComponent[] components;

	public static boolean disableVariableStartingWithExpressionWarnings = false;
	
	/**
	 * Creates a new VariableString which does not contain variables.
	 * @param s Content for string.
	 */
	private VariableString(final String s) {
		isSimple = true;
		simpleUnformatted = s.replace("%%", "%"); // This doesn't contain variables, so this wasn't done in newInstance!
		assert simpleUnformatted != null;
		simple = Utils.replaceChatStyles(simpleUnformatted);
				
		orig = simple;
		string = null;
		assert simple != null;
		mode = StringMode.MESSAGE;
		
		assert simpleUnformatted != null;
		components = new MessageComponent[] {ChatMessages.plainText(simpleUnformatted)};
	}
	
	/**
	 * Creates a new VariableString which contains variables.
	 * @param orig Original string (unparsed).
	 * @param string Objects, some of them are variables.
	 * @param mode String mode.
	 */
	private VariableString(final String orig, final Object[] string, final StringMode mode) {
		this.orig = orig;
		this.string = new Object[string.length];
		this.stringUnformatted = new Object[string.length];
		
		// Construct unformatted string and components
		List<MessageComponent> components = new ArrayList<>(string.length);
		for (int i = 0; i < string.length; i++) {
			Object o = string[i];
			if (o instanceof String) {
				assert this.string != null;
				this.string[i] = Utils.replaceChatStyles((String) o);
				components.addAll(ChatMessages.parse((String) o));
			} else {
				assert this.string != null;
				this.string[i] = o;
				components.add(null); // Not known parse-time
			}
			
			// For unformatted string, don't format stuff
			assert this.stringUnformatted != null;
			this.stringUnformatted[i] = o;
		}
		MessageComponent[] array = components.toArray(new MessageComponent[components.size()]);
		assert array != null;
		this.components = array;
		
		this.mode = mode;
		
		isSimple = false;
		simple = null;
		simpleUnformatted = null;
	}
	
	/**
	 * Prints errors
	 */
	@Nullable
	public static VariableString newInstance(final String s) {
		return newInstance(s, StringMode.MESSAGE);
	}
	
	public final static Map<String, Pattern> variableNames = new HashMap<>();
	
	/**
	 * Tests whether a string is correctly quoted, i.e. only has doubled double quotes in it.
	 * 
	 * @param s The string
	 * @param withQuotes Whether s must be surrounded by double quotes or not
	 * @return Whether the string is quoted correctly
	 */
	public static boolean isQuotedCorrectly(final String s, final boolean withQuotes) {
		if (withQuotes && (!s.startsWith("\"") || !s.endsWith("\"")))
			return false;
		boolean quote = false;
		for (int i = withQuotes ? 1 : 0; i < (withQuotes ? s.length() - 1 : s.length()); i++) {
			if (s.charAt(i) != '"') {
				if (quote)
					return false;
			} else {
				quote = !quote;
			}
		}
		return !quote;
	}
	
	/**
	 * Removes quoted quotes from a string.
	 * 
	 * @param s The string
	 * @param surroundingQuotes Whether the string has quotes at the start & end that should be removed
	 * @return The string with double quotes replaced with signle ones and optionally with removed surrounding quotes.
	 */
	public static String unquote(final String s, final boolean surroundingQuotes) {
		assert isQuotedCorrectly(s, surroundingQuotes);
		if (surroundingQuotes)
			return "" + s.substring(1, s.length() - 1).replace("\"\"", "\"");
		return "" + s.replace("\"\"", "\"");
	}
	
	/**
	 * Creates an instance of VariableString by parsing given string.
	 * Prints errors and returns null if it is somehow invalid.
	 * 
	 * @param orig Unquoted string to parse.
	 * @param mode
	 * @return A new VariableString instance.
	 */
	@Nullable
	public static VariableString newInstance(final String orig, final StringMode mode) {
		if (!isQuotedCorrectly(orig, false))
			return null;
		final int n = StringUtils.count(orig, '%');
		if (n % 2 != 0) {
			Skript.error("The percent sign is used for expressions (e.g. %player%). To insert a '%' type it twice: %%.");
			return null;
		}
		
		// We must not parse color codes yet, as JSON support would be broken :(
		final String s = orig.replace("\"\"", "\"");
		
		final List<Object> string = new ArrayList<>(n / 2 + 2); // List of strings and expressions
		
		int c = s.indexOf('%');
		if (c != -1) {
			if (c != 0)
				string.add(s.substring(0, c));
			while (c != s.length()) {
				int c2 = s.indexOf('%', c + 1);
				
				int a = c;
				int b;
				while (c2 != -1 && (b = s.indexOf('{', a + 1)) != -1 && b < c2) {
					a = nextVariableBracket(s, b + 1);
					if (a == -1) {
						Skript.error("Missing closing bracket '}' to end variable");
						return null;
					}
					c2 = s.indexOf('%', a + 1);
				}
				if (c2 == -1) {
					assert false;
					return null;
				}
				if (c + 1 == c2) {
					// %% escaped -> one % in result string
					if (string.size() > 0 && string.get(string.size() - 1) instanceof String) {
						string.set(string.size() - 1, (String) string.get(string.size() - 1) + "%");
					} else {
						string.add("%");
					}
				} else {
					final RetainingLogHandler log = SkriptLogger.startRetainingLog();
					try {
						@SuppressWarnings("unchecked")
						final Expression<?> expr = new SkriptParser("" + s.substring(c + 1, c2), SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT).parseExpression(Object.class);
						if (expr == null) {
							log.printErrors("Can't understand this expression: " + s.substring(c + 1, c2));
							return null;
						} else {
							if (mode != StringMode.MESSAGE) {
								string.add(expr);
							} else {
								final ExpressionInfo i = new ExpressionInfo(expr);
								if (c2 <= s.length() - 2 && s.charAt(c2 + 1) == 's' && (c2 == s.length() - 2 || !Character.isLetter(s.charAt(c2 + 2)))) {
									i.flags |= Language.F_PLURAL;
									c2++; // remove the 's'
								}
								if (string.size() > 0 && string.get(string.size() - 1) instanceof String) {
									final String last = (String) string.get(string.size() - 1);
									if (c2 <= s.length() - 2 && s.charAt(c2 + 1) == '>' && last.endsWith("<")) {
										i.toChatStyle = true;
										string.set(string.size() - 1, last.substring(0, last.length() - 1));
										c2++; // remove the '>'
									} else {
										final int l = last.lastIndexOf(' ', last.endsWith(" ") ? last.length() - 2 : last.length() - 1);
										final String lastWord = "" + last.substring(l + 1).trim();
										if (Noun.isLocalIndefiniteArticle(lastWord))
											i.flags |= Language.F_INDEFINITE_ARTICLE;
										else if (Noun.isLocalDefiniteArticle(lastWord))
											i.flags |= Language.F_DEFINITE_ARTICLE;
										if ((i.flags & (Language.F_INDEFINITE_ARTICLE | Language.F_DEFINITE_ARTICLE)) != 0)
											string.set(string.size() - 1, last.substring(0, l + 1));
									}
								}
								string.add(i);
							}
						}
						log.printLog();
					} finally {
						log.stop();
					}
				}
				c = s.indexOf('%', c2 + 1);
				if (c == -1)
					c = s.length();
				final String l = s.substring(c2 + 1, c); // Try to get string (non-variable) part
				if (!l.isEmpty()) { // This is string part (no variables)
					if (string.size() > 0 && string.get(string.size() - 1) instanceof String) {
						// We can append last string part in the list, so let's do so
						string.set(string.size() - 1, (String) string.get(string.size() - 1) + l);
					} else { // Can't append, just add new part
						string.add(l);
					}
				}
			}
		} else {
			// Only one string, no variable parts
			string.add(s);
		}
		
		checkVariableConflicts(s, mode, string);
		
		// Check if this isn't actually variable string, and return
		if (string.size() == 1 && string.get(0) instanceof String)
			return new VariableString(s);
		
		final Object[] sa = string.toArray();
		assert sa != null;
		if (string.size() == 1 && string.get(0) instanceof ExpressionInfo &&
				((ExpressionInfo) string.get(0)).expr.getReturnType() == String.class &&
				((ExpressionInfo) string.get(0)).expr.isSingle()) {
			String expr = ((ExpressionInfo) string.get(0)).expr.toString(null, false);
			Skript.warning(expr + " is already a text, so you should not put it in one (e.g. " + expr + " instead of " + "\"%" + expr.replace("\"", "\"\"") + "%\")");
		}
		return new VariableString(orig, sa, mode);
	}
	
	private static void checkVariableConflicts(final String name, final StringMode mode, final @Nullable Iterable<Object> string) {
		if (mode != StringMode.VARIABLE_NAME || variableNames.containsKey(name))
			return;
		if (name.startsWith("%")) {// inside the if to only print this message once per variable
			final Config script = ScriptLoader.currentScript;
			if (script != null) {
				if (disableVariableStartingWithExpressionWarnings && !ScriptOptions.getInstance().suppressesWarning(script.getFile(), "start expression")) {
					Skript.warning("Starting a variable's name with an expression is discouraged ({" + name + "}). You could prefix it with the script's name: {" + StringUtils.substring(script.getFileName(), 0, -3) + "." + name + "}");
				}
			}
		}
		
		final Pattern pattern;
		if (string != null) {
			final StringBuilder p = new StringBuilder();
			stringLoop: for (final Object o : string) {
				if (o instanceof Expression) {
					for (final ClassInfo<?> ci : Classes.getClassInfos()) {
						final Parser<?> parser = ci.getParser();
						if (parser != null && ci.getC().isAssignableFrom(((Expression<?>) o).getReturnType())) {
							p.append("(?!%)" + parser.getVariableNamePattern() + "(?<!%)");
							continue stringLoop;
						}
					}
					p.append("[^%*](.*[^%*])?"); // [^*] to not report {var::%index%}/{var::*} as conflict
				} else {
					p.append(Pattern.quote(o.toString()));
				}
			}
			pattern = Pattern.compile(p.toString());
		} else {
			pattern = Pattern.compile(Pattern.quote(name));
		}
		if (!SkriptConfig.disableVariableConflictWarnings.value()) {
			Config cs = ScriptLoader.currentScript; //Eclipse's nullness forced me to do this
			if (cs != null) {
				if (!ScriptOptions.getInstance().suppressesWarning(cs.getFile(), "conflict")) {
					for (final Entry<String, Pattern> e : variableNames.entrySet()) {
						if (e.getValue().matcher(name).matches() || pattern.matcher(e.getKey()).matches()) {
							Skript.warning("Possible name conflict of variables {" + name + "} and {" + e.getKey() + "} (there might be more conflicts).");
							break;
						}
					}
				}
			} else {
				for (final Entry<String, Pattern> e : variableNames.entrySet()) {
					if (e.getValue().matcher(name).matches() || pattern.matcher(e.getKey()).matches()) {
						Skript.warning("Possible name conflict of variables {" + name + "} and {" + e.getKey() + "} (there might be more conflicts).");
						break;
					}
				}
			}
		}
		variableNames.put(name, pattern);
	}
	
	/**
	 * Copied from {@link SkriptParser#nextBracket(String, char, char, int, boolean)}, but removed escaping & returns -1 on error.
	 * 
	 * @param s
	 * @param start Index after the opening bracket
	 * @return The next closing curly bracket
	 */
	public static int nextVariableBracket(final String s, final int start) {
		int n = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.charAt(i) == '}') {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == '{') {
				n++;
			}
		}
		return -1;
	}
	
	public static VariableString[] makeStrings(final String[] args) {
		VariableString[] strings = new VariableString[args.length];
		int j = 0;
		for (int i = 0; i < args.length; i++) {
			final VariableString vs = newInstance("" + args[i]);
			if (vs != null)
				strings[j++] = vs;
		}
		if (j != args.length)
			strings = Arrays.copyOf(strings, j);
		assert strings != null;
		return strings;
	}
	
	/**
	 * @param args Quoted strings - This is not checked!
	 * @return a new array containing all newly created VariableStrings, or null if one is invalid
	 */
	@Nullable
	public static VariableString[] makeStringsFromQuoted(final List<String> args) {
		final VariableString[] strings = new VariableString[args.size()];
		for (int i = 0; i < args.size(); i++) {
			assert args.get(i).startsWith("\"") && args.get(i).endsWith("\"");
			final VariableString vs = newInstance("" + args.get(i).substring(1, args.get(i).length() - 1));
			if (vs == null)
				return null;
			strings[i] = vs;
		}
		return strings;
	}
	
	/**
	 * Parses all expressions in the string and returns it.
	 * If this is a simple string, the event may be null.
	 * 
	 * @param e Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toString(@Nullable final Event e) {
		if (isSimple) {
			assert simple != null;
			return simple;
		}

		if (e == null) {
			throw new IllegalArgumentException("Event may not be null in non-simple VariableStrings!");
		}

		final Object[] string = this.string;
		assert string != null;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.length; i++) {
			final Object o = string[i];
			if (o instanceof Expression<?>) {
				assert mode != StringMode.MESSAGE;
				b.append(Classes.toString(((Expression<?>) o).getArray(e), true, mode));
			} else if (o instanceof ExpressionInfo) {
				assert mode == StringMode.MESSAGE;
				final ExpressionInfo info = (ExpressionInfo) o;
				int flags = info.flags;
				if ((flags & Language.F_PLURAL) == 0 && b.length() > 0 && Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1)
					flags |= Language.F_PLURAL;
				if (info.toChatStyle) {
					final String s = Classes.toString(info.expr.getArray(e), flags, getLastColor(b));
					final String style = Utils.getChatStyle(s);
					b.append(style == null ? "<" + s + ">" : style);
				} else {
					b.append(Classes.toString(info.expr.getArray(e), flags, getLastColor(b)));
				}
			} else {
				b.append(o);
			}
		}
		return "" + b.toString();
	}
	
	/**
	 * Parses all expressions in the string and returns it.
	 * Does not parse formatting codes!
	 * @param e Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toUnformattedString(final Event e) {
		if (isSimple) {
			assert simpleUnformatted != null;
			return simpleUnformatted;
		}
		final Object[] string = this.stringUnformatted;
		assert string != null;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.length; i++) {
			final Object o = string[i];
			if (o instanceof Expression<?>) {
				assert mode != StringMode.MESSAGE;
				b.append(Classes.toString(((Expression<?>) o).getArray(e), true, mode));
			} else if (o instanceof ExpressionInfo) {
				assert mode == StringMode.MESSAGE;
				final ExpressionInfo info = (ExpressionInfo) o;
				int flags = info.flags;
				if ((flags & Language.F_PLURAL) == 0 && b.length() > 0 && Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1)
					flags |= Language.F_PLURAL;
				if (info.toChatStyle) {
					final String s = Classes.toString(info.expr.getArray(e), flags, null);
					final String style = Utils.getChatStyle(s);
					b.append(style == null ? "<" + s + ">" : style);
				} else {
					b.append(Classes.toString(info.expr.getArray(e), flags, null));
				}
			} else {
				b.append(o);
			}
		}
		return "" + b.toString();
	}
	
	/**
	 * Gets message components from this string. Formatting is parsed only
	 * in simple parts for security reasons.
	 * @param e Currently running event.
	 * @return Message components.
	 */
	public List<MessageComponent> getMessageComponents(Event e) {
		if (isSimple) { // Trusted, constant string in a script
			assert simpleUnformatted != null;
			return ChatMessages.parse(simpleUnformatted);
		}
		
		// Parse formating
		Object[] string = this.stringUnformatted;
		assert string != null;
		List<MessageComponent> message = new ArrayList<>(components.length); // At least this much space
		int stringPart = -1;
		MessageComponent previous = null;
		for (MessageComponent component : components) {
			if (component == null) { // This component holds place for variable part
				// Go over previous expression part (stringPart >= 0) or take first part (stringPart == 0)
				stringPart++;
				if (previous != null) { // Also jump over literal part
					stringPart++;
				}
				Object o = string[stringPart];
				previous = null;
				
				// Convert it to plain text
				String text = null;
				if (o instanceof ExprColoured) { // Special case: user wants to process formatting
					String unformatted = ((ExprColoured) o).getSingle(e);
					if (unformatted != null) {
						message.addAll(ChatMessages.parse(unformatted));
					}
					continue;
				} else if (o instanceof Expression<?>) {
					assert mode != StringMode.MESSAGE;
					text = Classes.toString(((Expression<?>) o).getArray(e), true, mode);
				} else if (o instanceof ExpressionInfo) {
					assert mode == StringMode.MESSAGE;
					final ExpressionInfo info = (ExpressionInfo) o;
					int flags = info.flags;
					
					// TODO plural handling?
//					if ((flags & Language.F_PLURAL) == 0 && b.length() > 0 && Math.abs(StringUtils.numberBefore(b, b.length() - 1)) != 1)
//						flags |= Language.F_PLURAL;
					if (info.toChatStyle) {
						final String s = Classes.toString(info.expr.getArray(e), flags, null);
						final String style = Utils.getChatStyle(s);
						text = style == null ? "<" + s + ">" : style;
					} else {
						if (info.expr instanceof ExprColoured && ((ExprColoured) info.expr).isUnsafeFormat()) { // Special case: user wants to process formatting
							String unformatted = ((ExprColoured) info.expr).getSingle(e);
							if (unformatted != null) {
								message.addAll(ChatMessages.parse(unformatted));
							}
							continue;
						}
						text = Classes.toString(info.expr.getArray(e), flags, null);
					}
				} else {
					assert false;
				}
				
				assert text != null;
				MessageComponent plain = ChatMessages.plainText(text);
				if (!message.isEmpty()) { // Copy styles from previous component
					ChatMessages.copyStyles(message.get(message.size() - 1), plain);
				} else if (Utils.HEX_SUPPORTED && text.contains("§x")) { // Try to parse hex colors
					int start = text.lastIndexOf("§x");
					if (start + 14 < text.length()) {
						String replace = text.substring(start + 2, start + 14);
						plain.color = Utils.parseHexColor(replace.replace("&", "").replace("§", ""));
						plain.text = text.replace("§x" + replace, "");
					}
				}
				message.add(plain);
			} else {
				message.add(component);
				previous = component;
			}
		}
		
		return message;
	}
	
	/**
	 * Gets message components from this string. Formatting is parsed
	 * everywhere, which is a potential security risk.
	 * @param e Currently running event.
	 * @return Message components.
	 */
	public List<MessageComponent> getMessageComponentsUnsafe(Event e) {
		if (isSimple) { // Trusted, constant string in a script
			assert simpleUnformatted != null;
			return ChatMessages.parse(simpleUnformatted);
		}
		
		return ChatMessages.parse(toUnformattedString(e));
	}
	
	/**
	 * Parses all expressions in the string and returns it in chat JSON format.
	 * 
	 * @param e Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toChatString(final Event e) {
		return ChatMessages.toJson(getMessageComponents(e));
	}
	
	@Nullable
	private static ChatColor getLastColor(final CharSequence s) {
		for (int i = s.length() - 2; i >= 0; i--) {
			if (s.charAt(i) == ChatColor.COLOR_CHAR) {
				final ChatColor c = ChatColor.getByChar(s.charAt(i + 1));
				if (c != null && (c.isColor() || c == ChatColor.RESET))
					return c;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	/**
	 * Use {@link #toString(Event)} to get the actual string
	 */
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (isSimple) {
			assert simple != null;
			return '"' + simple + '"';
		}
		final Object[] string = this.string;
		assert string != null;
		final StringBuilder b = new StringBuilder("\"");
		for (final Object o : string) {
			if (o instanceof Expression) {
				b.append("%").append(((Expression<?>) o).toString(e, debug)).append("%");
			} else if (o instanceof ExpressionInfo) {
				b.append("%").append(((ExpressionInfo) o).expr.toString(e, debug)).append("%");
			} else {
				b.append(o);
			}
		}
		b.append('"');
		return "" + b.toString();
	}
	
	public String getDefaultVariableName() {
		if (isSimple) {
			assert simple != null;
			return simple;
		}
		final Object[] string = this.string;
		assert string != null;
		final StringBuilder b = new StringBuilder();
		for (final Object o : string) {
			if (o instanceof Expression) {
				b.append("<" + Classes.getSuperClassInfo(((Expression<?>) o).getReturnType()).getCodeName() + ">");
			} else {
				b.append(o);
			}
		}
		return "" + b.toString();
	}
	
	public boolean isSimple() {
		return isSimple;
	}
	
	public StringMode getMode() {
		return mode;
	}
	
	public VariableString setMode(final StringMode mode) {
		if (this.mode == mode || isSimple)
			return this;
		final BlockingLogHandler h = SkriptLogger.startLogHandler(new BlockingLogHandler());
		try {
			final VariableString vs = newInstance(orig, mode);
			if (vs == null) {
				assert false : this + "; " + mode;
				return this;
			}
			return vs;
		} finally {
			h.stop();
		}
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getSingle(final Event e) {
		return toString(e);
	}
	
	@Override
	public String[] getArray(final Event e) {
		return new String[] {toString(e)};
	}
	
	@Override
	public String[] getAll(final Event e) {
		return new String[] {toString(e)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c, final boolean negated) {
		return SimpleExpression.check(getAll(e), c, negated, false);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super String> c) {
		return SimpleExpression.check(getAll(e), c, false, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, String.class))
			return (Expression<? extends R>) this;
		return null;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean getAnd() {
		return false;
	}
	
	@Override
	public boolean setTime(final int time) {
		return false;
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public Iterator<? extends String> iterator(final Event e) {
		return new SingleItemIterator<>(toString(e));
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Expression<T> setStringMode(final Expression<T> e, final StringMode mode) {
		if (e instanceof ExpressionList) {
			final Expression<?>[] ls = ((ExpressionList<?>) e).getExpressions();
			for (int i = 0; i < ls.length; i++) {
				final Expression<?> l = ls[i];
				assert l != null;
				ls[i] = setStringMode(l, mode);
			}
		} else if (e instanceof VariableString) {
			return (Expression<T>) ((VariableString) e).setMode(mode);
		}
		return e;
	}
	
	@Override
	public Expression<String> simplify() {
		return this;
	}
	
	/* REMIND allow special characters?
	private static String allowedChars = null;
	private static Field allowedCharacters = null;
	
	static {
		if (Skript.isRunningCraftBukkit()) {
			try {
				allowedCharacters = SharedConstants.class.getDeclaredField("allowedCharacters");
				allowedCharacters.setAccessible(true);
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(allowedCharacters, allowedCharacters.getModifiers() & ~Modifier.FINAL);
				allowedChars = (String) allowedCharacters.get(null);
			} catch (Throwable e) {
				allowedChars = null;
				allowedCharacters = null;
			}
		}
	}
	 */
}
