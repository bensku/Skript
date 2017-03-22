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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.sun.istack.internal.Nullable;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Used for parsing my custom patterns.<br>
 * <br>
 * Note: All parse methods print one error at most xor any amount of warnings and lower level log messages. If the given string doesn't match any pattern then nothing is printed.
 *
 * @author Peter GÃ¼ttinger
 */
public class SkriptParser {

    //FLAG CONSTANTS
    public final static int PARSE_EXPRESSIONS = 1;
    public final static int PARSE_LITERALS = 2;
    public final static int ALL_FLAGS = PARSE_EXPRESSIONS | PARSE_LITERALS; //3
    public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?"; //Really dunno
    public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\""; //Same
    /**
     * Matches ',', 'and', 'or', etc. as well as surrounding whitespace.
     * <p>
     * group 1 is null for ',', otherwise it's one of and/or/nor (not necessarily lowercase).
     */
    @SuppressWarnings("null")
    public final static Pattern listSplitPattern = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
    @SuppressWarnings("null")
    private final static Pattern varPattern = Pattern.compile("((the )?var(iable)? )?\\{([^{}]|%\\{|\\}%)+\\}", Pattern.CASE_INSENSITIVE);
    private final static String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
    private final static String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";
    @SuppressWarnings("null")
    private final static Pattern functionCallPattern = Pattern.compile("(" + Functions.functionNamePattern + ")\\((.*)\\)"); //Same as "(\\w+)\\((.*)\\)"
    private final static Message m_quotes_error = new Message("skript.quotes error");
    private final static Message m_brackets_error = new Message("skript.brackets error");
    public final ParseContext context;
    final String expr;
    //OBJECT FLAGS
    private final int flags;
    private boolean suppressMissingAndOrWarnings = false;

    public SkriptParser(final String expr) {
        this(expr, ALL_FLAGS);
    }

    public SkriptParser(final String expr, final int flags) {
        this(expr, flags, ParseContext.DEFAULT);
    }

    /**
     * Constructs a new SkriptParser object that can be used to parse the given expression.
     * <p>
     * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
     *
     * @param expr    The expression to parse
     * @param flags   Some parse flags ({@link #PARSE_EXPRESSIONS}, {@link #PARSE_LITERALS})
     * @param context The parse context
     */
    public SkriptParser(final String expr, final int flags, final ParseContext context) {
        assert expr != null;
        assert (flags & ALL_FLAGS) != 0; //Asserts the flag is 1 or 2
        this.expr = "" + expr.trim(); //Removes unnecessary whitespace
        this.flags = flags;
        this.context = context;
    }

    public SkriptParser(final SkriptParser other, final String expr) {
        this(expr, other.flags, other.context);
    }

    /**
     * Parses a single literal, i.e. not lists of literals.
     * <p>
     * Prints errors.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public final static <T> Literal<? extends T> parseLiteral(String expr, final Class<T> c, final ParseContext context) {
        expr = "" + expr.trim();
        if (expr.isEmpty())
            return null;
        return new UnparsedLiteral(expr).getConvertedExpression(context, c);
    }

    /**
     * Parses a string as one of the given syntax elements.
     * <p>
     * Can print an error.
     */
    @Nullable
    public final static <T extends SyntaxElement> T parse(String expr, final Iterator<? extends SyntaxElementInfo<T>> source, final @Nullable String defaultError) {
        expr = "" + expr.trim();
        if (expr.isEmpty()) {
            Skript.error(defaultError);
            return null;
        }
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            final T e = new SkriptParser(expr).parse(source); //Just calls the non-static method
            if (e != null) {
                log.printLog();
                return e;
            }
            log.printError(defaultError);
            return null;
        } finally {
            log.stop();
        }
    }
    
    /**
     * Parses an effect, expression or condition
     * 
     * @param expr the string to parse as an effect, a condition or an expression
     * @param source An iterator containing classes extending Effect, Condition, Expression or SkriptEvent
     */
    @Nullable
    public final static <T extends SyntaxElement> T parseStatic(String expr, final Iterator<? extends SyntaxElementInfo<? extends T>> source, final @Nullable String defaultError) { //Just a static version of 'parse()' that therefore requires a string to go with.
        expr = "" + expr.trim();
        if (expr.isEmpty()) { 
            Skript.error(defaultError);
            return null;
        }
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        final T e; //Either Expression, Condition, Effect or SkriptEvent
        try {
            e = new SkriptParser(expr, PARSE_LITERALS).parse(source); //Just calls the non-static method with PARSE_LITERALS
            if (e != null) {
                log.printLog();
                return e;
            }
            log.printError(defaultError);
            return null;
        } finally {
            log.stop();
        }
    }

    /**
     * Prints errors
     */
    @Nullable
    private final static <T> Variable<T> parseVariable(final String expr, final Class<? extends T>[] returnTypes) {
        if (varPattern.matcher(expr).matches())
            return Variable.newInstance("" + expr.substring(expr.indexOf('{') + 1, expr.lastIndexOf('}')), returnTypes);
        return null;
    }

    /**
     * Prints parse errors (i.e. must start a ParseLog before calling this method)
     *
     * @return Whether the arguments were parsed successfully
     */
    public static boolean parseArguments(final String args, final ScriptCommand command, final ScriptCommandEvent event) {//Should be quite easy
        final SkriptParser parser = new SkriptParser(args, PARSE_LITERALS, ParseContext.COMMAND);
        final ParseResult res = parser.parse_i(command.getPattern(), 0, 0); //I see nothing related to commands in 'parse_i'. Hmmm...
        if (res == null)
            return false;

        final List<Argument<?>> as = command.getArguments();
        assert as.size() == res.exprs.length;
        for (int i = 0; i < res.exprs.length; i++) {
            if (res.exprs[i] == null)
                as.get(i).setToDefault(event);
            else
                as.get(i).set(event, res.exprs[i].getArray(event));
        }
        return true;
    }

    /**
     * Parses the text as the given pattern as {@link ParseContext#COMMAND}.
     * <p>
     * Prints parse errors (i.e. must start a ParseLog before calling this method)
     */
    @Nullable
    public static ParseResult parse(final String text, final String pattern) {
        return new SkriptParser(text, PARSE_LITERALS, ParseContext.COMMAND).parse_i(pattern, 0, 0);
    }

    @Nullable
    public static NonNullPair<SkriptEventInfo<?>, SkriptEvent> parseEvent(final String event, final String defaultError) {
        final RetainingLogHandler log = SkriptLogger.startRetainingLog();
        try {
            final NonNullPair<SkriptEventInfo<?>, SkriptEvent> e = new SkriptParser(event, PARSE_LITERALS, ParseContext.EVENT).parseEvent();
            if (e != null) {
                log.printLog();
                return e;
            }
            log.printErrors(defaultError);
            return null;
        } finally {
            log.stop();
        }
    }

//	@SuppressWarnings("unchecked")
//	@Nullable
//	private final Expression<?> parseObjectExpression() {
//		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
//		try {
//			if ((flags & PARSE_EXPRESSIONS) != 0) {
//				final Expression<?> r = new SkriptParser(expr, PARSE_EXPRESSIONS, context).parseSingleExpr(Object.class);
//				if (r != null) {
//					log.printLog();
//					return r;
//				}
//				if ((flags & PARSE_LITERALS) == 0) {
//					log.printError();
//					return null;
//				}
//				log.clear();
//			}
//
//			if ((flags & PARSE_LITERALS) != 0) {
//				// Hack as items use '..., ... and ...' for enchantments. Numbers and times are parsed beforehand as they use the same (deprecated) id[:data] syntax.
//				final SkriptParser p = new SkriptParser(expr, PARSE_LITERALS, context);
//				for (final Class<?> c : new Class[] {Number.class, Time.class, ItemType.class, ItemStack.class}) {
//					final Expression<?> e = p.parseExpression(c);
//					if (e != null) {
//						log.printLog();
//						return e;
//					}
//					log.clear();
//				}
//			}
//		} finally {
//			// log has been printed already or is not used after this (except for the error)
//			log.clear();
//			log.printLog();
//		}
//
//		final Matcher m = listSplitPattern.matcher(expr);
//		if (!m.find())
//			return new UnparsedLiteral(expr, log.getError());
//		m.reset();
//
//		final List<Expression<?>> ts = new ArrayList<Expression<?>>();
//		Kleenean and = Kleenean.UNKNOWN;
//		boolean last = false;
//		boolean isLiteralList = true;
//		int start = 0;
//		while (!last) {
//			final Expression<?> t;
//			if (context != ParseContext.COMMAND && expr.charAt(start) == '(') {
//				final int end = next(expr, start, context);
//				if (end == -1)
//					return null;
//				last = end == expr.length();
//				if (!last) {
//					m.region(end, expr.length());
//					if (!m.lookingAt())
//						return null;
//				}
//				t = new SkriptParser("" + expr.substring(start + 1, end - 1), flags, context).parseObjectExpression();
//			} else {
//				m.region(start, expr.length());
//				last = !m.find();
//				final String sub = last ? expr.substring(start) : expr.substring(start, m.start());
//				t = new SkriptParser("" + sub, flags, context).parseSingleExpr(Object.class);
//			}
//			if (t == null)
//				return null;
//			if (!last)
//				start = m.end();
//
//			isLiteralList &= t instanceof Literal;
//			if (!last && m.group(1) != null) {
//				if (and.isUnknown()) {
//					and = Kleenean.get(!m.group(1).equalsIgnoreCase("or")); // nor is and
//				} else {
//					if (and != Kleenean.get(!m.group(1).equalsIgnoreCase("or"))) {
//						Skript.warning(MULTIPLE_AND_OR);
//						and = Kleenean.TRUE;
//					}
//				}
//			}
//			ts.add(t);
//		}
//		assert ts.size() >= 1 : expr;
//		if (ts.size() == 1)
//			return ts.get(0);
//		if (and.isUnknown())
//			Skript.warning(MISSING_AND_OR);
//
//		final Class<?>[] exprRetTypes = new Class[ts.size()];
//		int i = 0;
//		for (final Expression<?> t : ts)
//			exprRetTypes[i++] = t.getReturnType();
//
//		if (isLiteralList) {
//			final Literal<Object>[] ls = ts.toArray(new Literal[ts.size()]);
//			assert ls != null;
//			return new LiteralList<Object>(ls, (Class<Object>) Utils.getSuperType(exprRetTypes), !and.isFalse());
//		} else {
//			final Expression<Object>[] es = ts.toArray(new Expression[ts.size()]);
//			assert es != null;
//			return new ExpressionList<Object>(es, (Class<Object>) Utils.getSuperType(exprRetTypes), !and.isFalse());
//		}
//	}

    /**
     * Finds the closing bracket of the group at <tt>start</tt> (i.e. <tt>start</tt> has to be <i>in</i> a group).
     *
     * @param pattern
     * @param closingBracket The bracket to look for, e.g. ')'
     * @param openingBracket A bracket that opens another group, e.g. '('
     * @param start          This must not be the index of the opening bracket!
     * @param assertIsGroup        Whether <tt>start</tt> is assumed to be in a group (will print an error if this is not the case, otherwise it returns <tt>pattern.length()</tt>)
     * @return The index of the next bracket or -1 if it wasn't found (meaning it didn't throw an error, too)
     * @throws MalformedPatternException If the group is not closed
     */
    private static int nextBracket(final String pattern, final char closingBracket, final char openingBracket, final int start, final boolean assertIsGroup) throws MalformedPatternException {
        int groupLevel = 0;
        for (int i = start; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '\\') {
                i++;
                continue;
            } else if (pattern.charAt(i) == closingBracket) {
                if (groupLevel == 0) {
                    if (!assertIsGroup)
                        throw new MalformedPatternException(pattern, "Unexpected closing bracket '" + closingBracket + "'");
                    return i;
                }
                groupLevel--;
            } else if (pattern.charAt(i) == openingBracket) {
                groupLevel++;
            }
        }
        if (assertIsGroup)
            throw new MalformedPatternException(pattern, "Missing closing bracket '" + closingBracket + "'");
        return -1;
    }

    /**
     * Gets the next occurrence of a character in a string that is not escaped with a preceding backslash.
     *
     * @param pattern
     * @param c       The character to search for
     * @param from    The index to start searching from
     * @return The next index where the character occurs unescaped or -1 if it doesn't occur.
     */
    private static int nextUnescaped(final String pattern, final char c, final int from) {
        for (int i = from; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '\\') {
                i++;
            } else if (pattern.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Counts how often the given character occurs in the given string, ignoring any escaped occurrences of the character.
     *
     * @param pattern
     * @param c       The character to search for
     * @return The number of unescaped occurrences of the given character
     */
    static int countUnescaped(final String pattern, final char c) {
        return countUnescaped(pattern, c, 0, pattern.length());
    }

    static int countUnescaped(final String pattern, final char c, final int start, final int end) {
        assert start >= 0 && start <= end && end <= pattern.length() : start + ", " + end + "; " + pattern.length();
        int r = 0;
        for (int i = start; i < end; i++) {
            final char x = pattern.charAt(i);
            if (x == '\\') {
                i++;
            } else if (x == c) {
                r++;
            }
        }
        return r;
    }

    /**
     * Find the next unescaped (i.e. single) double quote in the string.
     *
     * @param s
     * @param from Index after the starting quote
     * @return Index of the end quote
     */
    private static int nextQuote(final String s, final int from) {
        for (int i = from; i < s.length(); i++) {
            if (s.charAt(i) == '"') {
                if (i == s.length() - 1 || s.charAt(i + 1) != '"')
                    return i;
                i++;
            }
        }
        return -1;
    }

    /**
     * @param cs
     * @return "not an x" or "neither an x, a y nor a z"
     */
    public final static String notOfType(final Class<?>... cs) {
        if (cs.length == 1) {
            final Class<?> c = cs[0];
            assert c != null;
            return Language.get("not") + " " + Classes.getSuperClassInfo(c).getName().withIndefiniteArticle();
        } else {
            final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
            for (int k = 0; k < cs.length; k++) {
                if (k != 0) {
                    if (k != cs.length - 1)
                        b.append(", ");
                    else
                        b.append(" " + Language.get("nor") + " ");
                }
                final Class<?> c = cs[k];
                assert c != null && Classes.getSuperClassInfo(c).getName() != null;
                b.append(Classes.getSuperClassInfo(c).getName().withIndefiniteArticle());
            }
            return "" + b.toString();
        }
    }

    public final static String notOfType(final ClassInfo<?>... cs) {
        if (cs.length == 1) {
            return Language.get("not") + " " + cs[0].getName().withIndefiniteArticle();
        } else {
            final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
            for (int k = 0; k < cs.length; k++) {
                if (k != 0) {
                    if (k != cs.length - 1)
                        b.append(", ");
                    else
                        b.append(" " + Language.get("nor") + " ");
                }
                b.append(cs[k].getName().withIndefiniteArticle());
            }
            return "" + b.toString();
        }
    }

    /**
     * Returns the next character in the expression, skipping strings, variables and parentheses (unless <tt>context</tt> is {@link ParseContext#COMMAND}).
     *
     * @param expr The expression
     * @param i    The last index
     * @return The next index (can be expr.length()), or -1 if an invalid string, variable or bracket is found or if <tt>i >= expr.length()</tt>.
     * @throws StringIndexOutOfBoundsException if <tt>i < 0</tt>
     */
    public final static int next(final String expr, final int i, final ParseContext context) {
        if (i >= expr.length())
            return -1;
        if (i < 0)
            throw new StringIndexOutOfBoundsException(i);
        if (context == ParseContext.COMMAND)
            return i + 1;
        final char c = expr.charAt(i);
        if (c == '"') {
            final int i2 = nextQuote(expr, i + 1);
            return i2 < 0 ? -1 : i2 + 1;
        } else if (c == '{') {
            final int i2 = VariableString.nextVariableBracket(expr, i + 1);
            return i2 < 0 ? -1 : i2 + 1;
        } else if (c == '(') {
            for (int j = i + 1; j >= 0 && j < expr.length(); j = next(expr, j, context)) {
                if (expr.charAt(j) == ')')
                    return j + 1;
            }
            return -1;
        }
        return i + 1;
    }

    private final static int getGroupLevel(final String pattern, final int j) {
        assert j >= 0 && j <= pattern.length() : j + "; " + pattern;
        int level = 0;
        for (int i = 0; i < j; i++) {
            final char c = pattern.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '(') {
                level++;
            } else if (c == ')') {
                if (level == 0)
                    throw new MalformedPatternException(pattern, "Unexpected closing bracket ')'");
                level--;
            }
        }
        return level;
    }

    /**
     * Validates a user-defined pattern (used in {@link ExprParse}).
     *
     * @param pattern
     * @return The pattern with %codenames% and a boolean array that contains whetehr the expressions are plural or not
     */
    @Nullable
    public final static NonNullPair<String, boolean[]> validatePattern(final String pattern) {
        final List<Boolean> ps = new ArrayList<Boolean>();
        int groupLevel = 0, optionalLevel = 0;
        final Deque<Character> groups = new LinkedList<Character>();
        final StringBuilder b = new StringBuilder(pattern.length());
        int last = 0;
        for (int i = 0; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            if (c == '(') {
                groupLevel++;
                groups.addLast(c);
            } else if (c == '|') {
                if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
                    return error("Cannot use the pipe character '|' outside of groups. Escape it if you want to match a literal pipe: '\\|'");
                groups.removeLast();
                groups.addLast(c);
            } else if (c == ')') {
                if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
                    return error("Unexpected closing group bracket ')'. Escape it if you want to match a literal bracket: '\\)'");
                if (groups.peekLast() == '(')
                    return error("(...|...) groups have to contain at least one pipe character '|' to separate it into parts. Escape the brackets if you want to match literal brackets: \"\\(not a group\\)\"");
                groupLevel--;
                groups.removeLast();
            } else if (c == '[') {
                optionalLevel++;
                groups.addLast(c);
            } else if (c == ']') {
                if (optionalLevel == 0 || groups.peekLast() != '[')
                    return error("Unexpected closing optional bracket ']'. Escape it if you want to match a literal bracket: '\\]'");
                optionalLevel--;
                groups.removeLast();
            } else if (c == '<') {
                final int j = pattern.indexOf('>', i + 1);
                if (j == -1)
                    return error("Missing closing regex bracket '>'. Escape the '<' if you want to match a literal bracket: '\\<'");
                try {
                    Pattern.compile(pattern.substring(i + 1, j));
                } catch (final PatternSyntaxException e) {
                    return error("Invalid Regular Expression '" + pattern.substring(i + 1, j) + "': " + e.getLocalizedMessage());
                }
                i = j;
            } else if (c == '>') {
                return error("Unexpected closing regex bracket '>'. Escape it if you want to match a literal bracket: '\\>'");
            } else if (c == '%') {
                final int j = pattern.indexOf('%', i + 1);
                if (j == -1)
                    return error("Missing end sign '%' of expression. Escape the percent sign to match a literal '%': '\\%'");
                final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + pattern.substring(i + 1, j));
                final ClassInfo<?> ci = Classes.getClassInfoFromUserInput(p.getFirst());
                if (ci == null)
                    return error("The type '" + p.getFirst() + "' could not be found. Please check your spelling or escape the percent signs if you want to match literal %s: \"\\%not an expression\\%\"");
                ps.add(p.getSecond());
                b.append(pattern.substring(last, i + 1));
                b.append(Utils.toEnglishPlural(ci.getCodeName(), p.getSecond()));
                last = j;
                i = j;
            } else if (c == '\\') {
                if (i == pattern.length() - 1)
                    return error("Pattern must not end in an unescaped backslash. Add another backslash to escape it, or remove it altogether.");
                i++;
            }
        }
        b.append(pattern.substring(last));
        final boolean[] plurals = new boolean[ps.size()];
        for (int i = 0; i < plurals.length; i++)
            plurals[i] = ps.get(i);
        return new NonNullPair<String, boolean[]>("" + b.toString(), plurals);
    }

    @Nullable
    private final static NonNullPair<String, boolean[]> error(final String error) {
        Skript.error("Invalid pattern: " + error);
        return null;
    }

    public final static boolean validateLine(final String line) {
        if (StringUtils.count(line, '"') % 2 != 0) {
            Skript.error(m_quotes_error.toString());
            return false;
        }
        for (int i = 0; i < line.length(); i = next(line, i, ParseContext.DEFAULT)) {
            if (i == -1) {
                Skript.error(m_brackets_error.toString());
                return false;
            }
        }
        return true;
    }

    private static ExprInfo getExprInfo(String s) throws MalformedPatternException, IllegalArgumentException, SkriptAPIException {
        final ExprInfo r = new ExprInfo(StringUtils.count(s, '/') + 1);
        r.isOptional = s.startsWith("-");
        if (r.isOptional)
            s = "" + s.substring(1);
        if (s.startsWith("*")) {
            s = "" + s.substring(1);
            r.flagMask &= ~PARSE_EXPRESSIONS;
        } else if (s.startsWith("~")) {
            s = "" + s.substring(1);
            r.flagMask &= ~PARSE_LITERALS;
        }
        if (!r.isOptional) {
            r.isOptional = s.startsWith("-");
            if (r.isOptional)
                s = "" + s.substring(1);
        }
        final int a = s.indexOf("@");
        if (a != -1) {
            r.time = Integer.parseInt(s.substring(a + 1));
            s = "" + s.substring(0, a);
        }
        final String[] classes = s.split("/");
        assert classes.length == r.classes.length;
        for (int i = 0; i < classes.length; i++) {
            final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + classes[i]);
            r.classes[i] = Classes.getClassInfo(p.getFirst());
            r.isPlural[i] = p.getSecond();
        }
        return r;
    }
    /**
     * Parses a string as an Expression, a Condition, an Effect or a SkriptEvent
     * 
     * @param source An Iterator containing 
     */
    @Nullable
    private final <T extends SyntaxElement> T parse(final Iterator<? extends SyntaxElementInfo<? extends T>> source) { //This method relies heavily on parse_i()
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            while (source.hasNext()) { //Iterating over "source"
                final SyntaxElementInfo<? extends T> info = source.next();
                patternsLoop:
                for (int i = 0; i < info.patterns.length; i++) { //Iterates over all patterns for each syntax element (expression, condition, effect)
                    log.clear();
                    try {
                        final String pattern = info.patterns[i]; //Gets the current pattern
                        assert pattern != null;
                        final ParseResult res = parse_i(pattern, 0, 0); //Gets the ParseResult of the current pattern
                        if (res != null) {
                            int x = -1; //Crappy variable naming
                            for (int j = 0; (x = nextUnescaped(pattern, '%', x + 1)) != -1; j++) { //Iterating variable is j ; Continue condition : There's still more '%'s  //'x' is the index of next unescaped '%' which starts a new type
                                final int x2 = nextUnescaped(pattern, '%', x + 1); //Gets the '%' that closes the current type
                                if (res.exprs[j] == null) { //If an expression is omitted in the statement (e.g it was between '[]'). Therefore, checks all of that DefaultExpression stuff
                                    final String name = pattern.substring(x + 1, x2); //Gets the current type's name in the syntax
                                    if (!name.startsWith("-")) { //If the default value shoudn't be null
                                        final ExprInfo vi = getExprInfo(name); //Gets info about the current classinfo name
                                        final DefaultExpression<?> expr = vi.classes[0].getDefaultExpression(); //Gets the class' DefaultExpression
                                        //Errors, needless to explain
                                        if (expr == null)
                                            throw new SkriptAPIException("The class '" + vi.classes[0].getCodeName() + "' does not provide a default expression. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
                                        if (!(expr instanceof Literal) && (vi.flagMask & PARSE_EXPRESSIONS) == 0)
                                            throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a literal. Either allow null (with %-*" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
                                        if (expr instanceof Literal && (vi.flagMask & PARSE_LITERALS) == 0)
                                            throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is a literal. Either allow null (with %-~" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
                                        if (!vi.isPlural[0] && !expr.isSingle())
                                            throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + info.patterns[i] + "]");
                                        if (vi.time != 0 && !expr.setTime(vi.time))
                                            throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' does not have distinct time states. [pattern: " + info.patterns[i] + "]");
                                        if (!expr.init())
                                            continue patternsLoop; //Strangely enough, the named 'continue' explains perfectly what it does
                                        res.exprs[j] = expr; //Edits the ParseResult's expressions
                                    }
                                }
                                x = x2; //Makes the search for '%'s in the next iteration begin after the current closing '%'
                            }
                            //Creates a new instance of either an expression, an effect, a condition or a SkriptEvent
                            T t = info.c.newInstance();
                            if (t.init(res.exprs, i, ScriptLoader.hasDelayBefore, res)) { //If the SyntaxElement can be 'init'-ed successfully...
                                log.printLog();
                                return t; //... Return it
                            }
                        }
                    } catch (final InstantiationException | IllegalAccessException e) {
                        assert false;
                    }
                }
            }
            log.printError();
            return null; //Returns null if no syntax of any SyntaxElement has been found
        } finally {
            log.stop();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private final <T> Expression<? extends T> parseSingleExpr(final boolean allowUnparsedLiteral, @Nullable final LogEntry error, final Class<? extends T>... types) {
        assert types.length > 0;
        assert types.length == 1 || !CollectionUtils.contains(types, Object.class);
        if (expr.isEmpty())
            return null;
        if (context != ParseContext.COMMAND && expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length())
            return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseSingleExpr(allowUnparsedLiteral, error, types);
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            if (context == ParseContext.DEFAULT || context == ParseContext.EVENT) {
                final Variable<? extends T> var = parseVariable(expr, types);
                if (var != null) {
                    if ((flags & PARSE_EXPRESSIONS) == 0) {
                        Skript.error("Variables cannot be used here.");
                        log.printError();
                        return null;
                    }
                    log.printLog();
                    return var;
                } else if (log.hasError()) {
                    log.printError();
                    return null;
                }
                final FunctionReference<T> fr = parseFunction(types);
                if (fr != null) {
                    log.printLog();
                    return new ExprFunctionCall(fr);
                } else if (log.hasError()) {
                    log.printError();
                    return null;
                }
            }
            log.clear();
            if ((flags & PARSE_EXPRESSIONS) != 0) {
                final Expression<?> e;
                e = expr.startsWith("\"") && expr.endsWith("\"") && expr.length() != 1 && (types[0] == Object.class || CollectionUtils.contains(types, String.class)) ? VariableString.newInstance("" + expr.substring(1, expr.length() - 1)) : (Expression<?>) parse(expr, (Iterator) Skript.getExpressions(types), null);
                if (e != null) {
                    for (final Class<? extends T> t : types) {
                        if (t.isAssignableFrom(e.getReturnType())) {
                            log.printLog();
                            return (Expression<? extends T>) e;
                        }
                    }
                    for (final Class<? extends T> t : types) {
                        final Expression<? extends T> r = e.getConvertedExpression(t);
                        if (r != null) {
                            log.printLog();
                            return r;
                        }
                    }
                    log.printError(e.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
                    return null;
                }
                log.clear();
            }
            if ((flags & PARSE_LITERALS) == 0) {
                log.printError();
                return null;
            }
            if (types[0] == Object.class) {
                if (!allowUnparsedLiteral) {
                    log.printError();
                    return null;
                }
                log.clear();
                log.printLog();
                final LogEntry e = log.getError();
                return (Literal<? extends T>) new UnparsedLiteral(expr, e != null && (error == null || e.quality > error.quality) ? e : error);
            }
            for (final Class<? extends T> c : types) {
                log.clear();
                assert c != null;
                final T t = Classes.parse(expr, c, context);
                if (t != null) {
                    log.printLog();
                    return new SimpleLiteral<T>(t, false);
                }
            }
            log.printError();
            return null;
        } finally {
            log.stop();
        }
    }

    private SkriptParser suppressMissingAndOrWarnings() {
        suppressMissingAndOrWarnings = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    
    public final <T> Expression<? extends T> parseExpression(final Class<? extends T>... types) { //Parses a literal list of expressions
        //NOTE : 'literal list' means lists like '..., ... and ...' and not multiple literals
        
        if (expr.length() == 0)
            return null;
        
        assert types != null && types.length > 0;
        assert types.length == 1 || !CollectionUtils.contains(types, Object.class); //Makes sure that the types do not contain Object and, if not, makes sure only Object is in the types

        final boolean isObject = types.length == 1 && types[0] == Object.class; //I dunno why the second check would be of any use, but hey !
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            //Mirre << Not by Syst3ms
            if (isObject) {
                if ((flags & PARSE_LITERALS) != 0) { //if 'flags' isn't PARSE_EXPRESSIONS
                    // Hack as items use '..., ... and ...' for enchantments. Numbers and times are parsed beforehand as they use the same (deprecated) id[:data] syntax. << Not by Syst3ms
                    final SkriptParser p = new SkriptParser(expr, PARSE_LITERALS, context);
                    for (final Class<?> c : new Class[]{Number.class, Time.class, ItemType.class, ItemStack.class}) {
                        final Expression<?> e = p.parseExpression(c); //Self-calling method. Oh God. Actually it's not too bad.
                        if (e != null) {
                            log.printLog();
                            return (Expression<? extends T>) e;
                        }
                        log.clear();
                    }
                }
            }
            //Mirre
            final Expression<? extends T> r = parseSingleExpr(false, null, types); //Welp.
            if (r != null) {
                log.printLog();
                return r;
            }
            log.clear();

            final List<Expression<? extends T>> ts = new ArrayList<Expression<? extends T>>(); //Omg variable naming
            /**
             Determines what kind of literal list this expression is
             TRUE : 'and' or 'nor' list
             FALSE : 'or' list
             UNKNOWN : none specified
             */
            Kleenean isAndList = Kleenean.UNKNOWN; 
            boolean isLiteralList = true;
            
            /*
             * This whole part (until line 946) :
             *   - adds to 'pieces' the beginning and ending indexes of literal list separators ("," , "and", "or" or "nor")
             *   - Parses all elements of the literal list EXCEPT the 'and'/'or'/'nor' and the following expression
             *   - The last separator can't be a comma
             */
            final List<int[]> pieces = new ArrayList<int[]>();
            { //Anonymous codeblock restricts variable scope
                final Matcher m = listSplitPattern.matcher(expr); //A pattern that matches any literal list separator
                int i = 0, j = 0; 
                for (; i >= 0 && i <= expr.length(); i = next(expr, i, context)) { //Runs if 'i' is in between 0 and the expression's length. Each time, 'i' is set to the 'next()' character
                    if (i == expr.length() /*The search reached the end*/ || /*or*/ m.region(i, expr.length()).lookingAt() /*The listSplitPattern matches when starting from the pos'i'*/) { 
                        pieces.add(new int[]{j, i}); //j = the beginning of the literal list separator's index, i = its end index
                        if (i == expr.length()) //In this case, the search can't go further
                            break;
                        j = i = m.end(); //Sets both to the index of the end of the match so that the search can happen again without any interference
                    }
                }
                if (i != expr.length()) { //Happens if 'next()' returns -1, which happens when there are invalid brackets/quotes
                    assert i == -1 /*Just as I said*/ && context != ParseContext.COMMAND /*'next()' ignores variables, quotes and parenthesises when the ParseContext is COMMAND*/ : i + "; " + expr;
                    log.printError("Invalid brackets/variables/text in '" + expr + "'", ErrorQuality.NOT_AN_EXPRESSION);
                    return null;
                }
            }

            if (pieces.size() == 1) { // not a list of expressions, and a single one has failed to parse above << Not by Syst3ms. By Syst3ms >> If the expression was only a single literal list
                if (expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length() /*Technically unnecessary*/) { //If the list is enclosed between '()'
                    log.clear();
                    log.printLog();
                    return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseExpression(types); //Parse the list inside the parenthesises, excluding them from the expression to avoid infinite loop
                }
                if (isObject && (flags & PARSE_LITERALS) != 0) { // single expression - can return an UnparsedLiteral now << Not by Syst3ms. By Syst3ms >> if the expression is a single object and if 'flags' is PARSE_LITERALS or ALL_FLAGS
                    log.clear();
                    log.printLog();
                    return (Expression<? extends T>) new UnparsedLiteral(expr, log.getError()); //You should know how Literals work better than me, Bensku. Seems legit anyway
                }
              	//Mirre start
                // results in useless errors most of the time
//				log.printError("'" + expr + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION); 
              	//Mirre end
                log.printError();
                return null; //Makes kinda sense
            }

            outer: //Oh no. Well, actually it's quite descriptive
            for (int b = 0; b < pieces.size(); ) { 
                for (int a = pieces.size() - b; a >= 1; a--) { 
                    if (b == 0 && a == pieces.size()) // i.e. the whole expression - already tried to parse above << Not by Syst3ms. By Syst3ms >> The first iteration does nothing
                      continue;
                    final int x = pieces.get(b)[0], y = pieces.get(b + a - 1)[1];
                    final String subExpr = "" + expr.substring(x, y).trim();
                    assert subExpr.length() < expr.length() : subExpr; //Obvious
                    final Expression<? extends T> t; //That's how you create an expression then
                    if (subExpr.startsWith("(") && subExpr.endsWith(")") && next(subExpr, 0, context) == subExpr.length())
                        t = new SkriptParser(this, subExpr).parseExpression(types); // parse as a literal list if its surrounded by brackets
                    else 
                        t = new SkriptParser(this, subExpr).parseSingleExpr(a == 1, log.getError(), types); // otherwise parse as a single expression only
                    if (t != null) {
                        isLiteralList &= t instanceof Literal; //Same as 'isLiteralList = isLiteralList && t instanceof Literal'
                        ts.add(t); //Add to the expression list
                        if (b != 0) {
                            final String d = expr.substring(pieces.get(b - 1)[1], x).trim();
                            if (!d.equals(",")) {
                                if (isAndList.isUnknown()) {
                                    isAndList = Kleenean.get(!d.equalsIgnoreCase("or")); // nor == and
                                } else {
                                    if (isAndList != Kleenean.get(!d.equalsIgnoreCase("or"))) {
                                        Skript.warning(MULTIPLE_AND_OR + " List: " + expr);
                                        isAndList = Kleenean.TRUE;
                                    }
                                }
                            }
                        }
                        b += a;
                        continue outer;
                    }
                }
                log.printError();
                return null; //Most likely means the parsing above has fucked up
            }

            log.printLog();

            if (ts.size() == 1) //That'll make sense with what follows, I guess
                return ts.get(0); 

            if (isAndList.isUnknown() && !suppressMissingAndOrWarnings) //If the literal list hasn't any type specified and the corresponding warnings are enabled
                Skript.warning(MISSING_AND_OR + ": " + expr);

            final Class<? extends T>[] exprRetTypes = new Class[ts.size()]; //Stores the return types of each expression
            //TODO replace with final Class<? extends T>[] exprRetTypes = ts.stream().map(expr -> expr.getReturnType()).toArray(Class<? extends T>[]::new);
            for (int i = 0; i < ts.size(); i++)
                exprRetTypes[i] = ts.get(i).getReturnType();

            if (isLiteralList) {
                final Literal<T>[] ls = ts.toArray(new Literal[ts.size()]);
                return new LiteralList<T>(ls, (Class<T>) Utils.getSuperType(exprRetTypes), !isAndList.isFalse());
            } else {
                final Expression<T>[] es = ts.toArray(new Expression[ts.size()]);
                return new ExpressionList<T>(es, (Class<T>) Utils.getSuperType(exprRetTypes), !isAndList.isFalse());
            }
        } finally {
            log.stop();
        }
    }

    /**
     * @param types The required return type or null if it is not used (e.g. when calling a void function)
     * @return The parsed function, or null if the given expression is not a function call or is an invalid function call (check for an error to differentiate these two)
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public final <T> FunctionReference<T> parseFunction(final @Nullable Class<? extends T>... types) { //It seems that the method is made to be able to expect multiple return types
        if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
            return null; //Of course
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            final Matcher m = functionCallPattern.matcher(expr); //Gets a matcher
            if (!m.matches()) {
                log.printLog();
                return null;
            }
            if ((flags & PARSE_EXPRESSIONS) == 0) { //If 'flags' is 'PARSE_LITERALS'. That's the reason why you can't use functions in events
                Skript.error("Functions cannot be used here.");
                log.printError();
                return null;
            }
            //Descriptive variable naming
            final String functionName = "" + m.group(1);
            final String args = m.group(2);
            final Expression<?>[] params;
            if (args.length() != 0) {
                final Expression<?> ps = new SkriptParser(args, flags | PARSE_LITERALS /*Results in 3 in any case, since 'flags' can't be PARSE_LITERALS because of the above check. Could be replaced with ALL_FLAGS*/, context).suppressMissingAndOrWarnings().parseExpression(Object.class); //Will have to investigate this, but it does what we think it does
                if (ps == null) {
                    log.printError();
                    return null;
                }
                if (ps instanceof ExpressionList) {
                    if (!ps.getAnd()) { //If the parameters is only a "'or' list"
                        Skript.error("Function arguments must be separated by commas and optionally an 'and', but not an 'or'."
                                + " Put the 'or' into a second set of parentheses if you want to make it a single parameter, e.g. 'give(player, (sword or axe))'");
                        log.printError();
                        return null;
                    }
                    params = ((ExpressionList<?>) ps).getExpressions(); //Gets all expressions of an expression list in case there is one
                } else {
                    params = new Expression[]{ps};
                }
            } else {
                params = new Expression[0];
            }

            final FunctionReference<T> e = new FunctionReference<T>(functionName, SkriptLogger.getNode(), ScriptLoader.currentScript != null ? ScriptLoader.currentScript.getFile() : null, types, params);//.toArray(new Expression[params.size()])); //We know what this does, I'm too lazy to explain it in-depth
            if (!e.validateFunction(true)) { //If the function is wrong (can be because of it's name, return type, argument count and/or argument types). Those checks aren't done when FunctionReference is instantiated
                log.printError();
                return null;
            }
            log.printLog();
            return e; 
        } finally {
            log.stop();
        }
    }

    @Nullable
    private NonNullPair<SkriptEventInfo<?>, SkriptEvent> parseEvent() {
        assert context == ParseContext.EVENT; //Of course
        assert flags == PARSE_LITERALS; //Same
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            for (final SkriptEventInfo<?> info : Skript.getEvents()) { //Iterates over info about all events
                for (int i = 0; i < info.patterns.length; i++) { //Iterates over all patterns of the current event
                    log.clear();
                    try {
                        final String pattern = info.patterns[i]; //Gets the current event's current pattern
                        assert pattern != null;
                        final ParseResult res = parse_i(pattern, 0, 0); //Parses the event
                        if (res != null) {
                            final SkriptEvent e = info.c.newInstance();
                            final Literal<?>[] ls = Arrays.copyOf(res.exprs, res.exprs.length, Literal[].class); //Converts all of the ParseResult's expressions into Literals
                            assert ls != null;
                            if (!e.init(ls, i, res)) { //If init wasn't successful
                                log.printError();
                                return null;
                            }
                            log.printLog();
                            return new NonNullPair<>(info, e); //Returns a NonNullPair composed of the event's SkriptEventInfo and an event's instance
                        }
                    } catch (final InstantiationException | IllegalAccessException e) {
                        assert false;
                    }
                }
            }
            log.printError(null); //If no event was found
            return null;
        } finally {
            log.stop();
        }
    }

    /**
     * Prints errors
     *
     * @param pattern
     * @param stringPosition       Position in the input string
     * @param patternPosition       Position in the pattern
     * @return Parsed result or null on error (which does not imply that an error was printed)
     */
    @Nullable
    private final ParseResult parse_i(final String pattern, int stringPosition, int patternPosition) { //When is method is ran for the first time, it will call itself again after a short amount of characters, to parse more characters and call itself again. In fact, a 'return' won't be called until the whole pattern is parsed
        ParseResult res; //Empty parse result
        int closingCharacterIndex, nextMethodChar; //'closingCharacterIndex' represent the closing character and 'nextMethodChar' the next character, according to the description of 'next()'

        while (patternPosition < pattern.length()) {//While there are still letters in the pattern
            switch (pattern.charAt(patternPosition)) { //Gets each character of the pattern
                case '[': {//Opening bracket case :
                    final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                    try {
                        res = parse_i(pattern, stringPosition, patternPosition + 1); //Parses what's in the brackets, since the method seems to stop (and call itself again) when encountering a Skript-special character
                        if (res != null) { //Needless to explain
                            log.printLog();
                            return res;
                        }
                        log.clear();
                        patternPosition = nextBracket(pattern, ']', '[', patternPosition + 1, true) + 1; //Gets the index of the character after the closing bracket
                        res = parse_i(pattern, stringPosition, patternPosition); //Parses everything after the closing bracket, while keeping the same position in the string, since a bracket section can be completely ignored file-wise
                        if (res == null)
                            log.printError();
                        else
                            log.printLog();
                        return res; //Won't happen until the whole pattern is parsed, actually
                    } finally {
                        log.stop();
                    }
                }
                case '(': { //Opening parenthesis case :
                    final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                    try {
                        final int start = patternPosition; //Tracks the opening bracket, since the parser will have to match that against the string
                        for (; patternPosition < pattern.length(); patternPosition++) { //Iterates over the characters inside '()'
                            log.clear();
                            if (patternPosition == start || pattern.charAt(patternPosition) == '|') { //This will check for a mark and set it if it is present
                                int mark = 0;
                            /*If*/
                                if (patternPosition != pattern.length() - 1 /*This is not the closingCharacterIndex*/ && ('0' <= pattern.charAt(patternPosition + 1) && pattern.charAt(patternPosition + 1) <= '9' /*The character after the opening '(' or '|' is a number*/ || pattern.charAt(patternPosition + 1) == '-' /*Wait dafuq*/)) {
                                    final int j2 = pattern.indexOf('¦', patternPosition + 2); //Get where the broken bar is at
                                    if (j2 != -1) { //Technically, this is unnecessary, but better safe than sorry
                                        try {
                                            mark = Integer.parseInt(pattern.substring(patternPosition + 1, j2)); //Set the mark
                                            patternPosition = j2; //Go to character after the broken bar
                                        } catch (final NumberFormatException e) {
                                        }
                                    }
                                }
                                res = parse_i(pattern, stringPosition, patternPosition + 1); //Parses what's in '(' (or after the broken bar part)
                                if (res != null) {
                                    log.printLog();
                                    res.mark ^= mark; // doesn't do anything if no mark was set as x ^ 0 == x     << Not by Syst3ms
                                    return res; //Won't happen until the whole pattern is parsed, actually
                                }
                            } else if (pattern.charAt(patternPosition) == '(') {
                                patternPosition = nextBracket(pattern, ')', '(', patternPosition + 1, true); //Apparently skips another group when one is encountered. I think the method handles that group though
                            } else if (pattern.charAt(patternPosition) == ')') {
                                break; //Breaks if the closingCharacterIndex of the group is reached
                            } else if (patternPosition == pattern.length() - 1) { //If the closingCharacterIndex of the pattern is reached
                                throw new MalformedPatternException(pattern, "Missing closing bracket ')'");
                            }
                        }
                        log.printError();
                        return null;
                    } finally {
                        log.stop();
                    }
                }
                case '%': { //Percent case. Now this is going to be difficult :
                    if (stringPosition == expr.length()) //If 'stringPosition' is at the closingCharacterIndex of the input string
                        return null;
                    closingCharacterIndex = pattern.indexOf('%', patternPosition + 1); //Get the next '%'
                    if (closingCharacterIndex == -1) //If it is not found
                        throw new MalformedPatternException(pattern, "Odd number of '%'");
                    final String name = "" + pattern.substring(patternPosition + 1, closingCharacterIndex); //Gets the class' name in the pattern
                    final ExprInfo vi = getExprInfo(name); //Gets the info about that class
                    if (closingCharacterIndex == pattern.length() - 1) { //If the closing '%' is at the closingCharacterIndex of the pattern
                        nextMethodChar = expr.length(); //Now 'nextMethodChar' appears. Here, it's set to the input string's length
                    } else {
                        nextMethodChar = next(expr, stringPosition, context); //Gets the next character in the expression, according to 'next()'
                        if (nextMethodChar == -1) //In case 'next()' fails
                            return null;
                    }
                    final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                    try {
                        for (; nextMethodChar != -1; nextMethodChar = next(expr, nextMethodChar, context)) { //Iterates over each 'next()' character of the expression,
                            log.clear();
                            res = parse_i(pattern, nextMethodChar, closingCharacterIndex + 1); //Parses from 'nextMethodChar' in the expression and after the closing '%' in the pattern
                            if (res != null) {
                                final ParseLogHandler log2 = SkriptLogger.startParseLogHandler();
                                try {
                                    for (int k = 0; k < vi.classes.length; k++) { //Iterates over all the classes (separated by '/') in the current '%%'
                                        if ((flags & vi.flagMask) == 0) //Each ExprInfo's flagMask stores whether it is expecting expressions (-2), literals (-3) or both (-1). This check determines if both expect the same thing (literals/expressions). It returns false if both can expect literals and expressions
                                            continue;
                                        log2.clear();
                                        @SuppressWarnings("unchecked")
                                        final Expression<?> e = new SkriptParser("" + expr.substring(stringPosition, nextMethodChar), flags & vi.flagMask /*What that means is explained in the check above*/, context).parseExpression(vi.classes[k].getC());
                                        if (e != null) {
                                            if (!vi.isPlural[k] && !e.isSingle()) { //If the current class is plural and the expression is single, and vice-versa. Seems weird, but not that important
                                                if (context == ParseContext.COMMAND) {
                                                    Skript.error(Commands.m_too_many_arguments.toString(vi.classes[k].getName().getIndefiniteArticle(), vi.classes[k].getName().toString()), ErrorQuality.SEMANTIC_ERROR);
                                                    return null;
                                                } else {
                                                    Skript.error("'" + expr.substring(0, stringPosition) + "<...>" + expr.substring(nextMethodChar) + "' can only accept a single " + vi.classes[k].getName() + ", not more", ErrorQuality.SEMANTIC_ERROR);
                                                    return null;
                                                }
                                            }
                                            if (vi.time != 0) { //About the 'time' stuff. Basically it checks if the current type is supposed to be future or past
                                                if (e instanceof Literal<?>) //Quite obviously, a literal can't be future or past
                                                    return null;
                                                if (ScriptLoader.hasDelayBefore == Kleenean.TRUE) {
                                                    Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
                                                    return null;
                                                }
                                                if (!e.setTime(vi.time)) {
                                                    Skript.error(e + " does not have a " + (vi.time == -1 ? "past" : "future") + " state", ErrorQuality.SEMANTIC_ERROR);
                                                    return null;
                                                }
                                                //The errors are very descriptive.
                                            }
                                            log2.printLog();
                                            log.printLog();
                                            res.exprs[countUnescaped(pattern, '%', 0, patternPosition) / 2] = e; //In the ParseResult's expression array, sets the one corresponding to the current expression to the parsed expression
                                            return res; //Won't happen until the whole pattern is parsed, actually... I think.
                                        }
                                    }
                                    // results in useless errors most of the time   << Not by Syst3ms
//									Skript.error("'" + expr.substring(stringPosition, nextMethodChar) + "' is " + notOfType(vi.classes), ErrorQuality.NOT_AN_EXPRESSION);
                                    return null;
                                } finally {
                                    log2.printError();
                                }
                            }
                        }
                    } finally {
                        if (!log.isStopped())
                            log.printError();
                    }
                    return null;
                }
                case '<': { //I've made it past the hardest part. Now opening angle bracket (regex) case :
                    closingCharacterIndex = pattern.indexOf('>', patternPosition + 1);// not next() << Not by Syst3ms. Syst3ms' comment >> gets the closing angle bracket's index
                    if (closingCharacterIndex == -1)
                        throw new MalformedPatternException(pattern, "Missing closing regex bracket '>'");
                    Pattern p;
                    try {
                        p = Pattern.compile(pattern.substring(patternPosition + 1, closingCharacterIndex)); //Tries to compile the regex between '<' and '>'
                    } catch (final PatternSyntaxException e) {
                        throw new MalformedPatternException(pattern, "Invalid regex <" + pattern.substring(patternPosition + 1, closingCharacterIndex) + ">", e);
                    }
                    final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                    try {
                        final Matcher m = p.matcher(expr); //Get the regex pattern's matcher
                        for (nextMethodChar = next(expr, stringPosition, context); nextMethodChar != -1; nextMethodChar = next(expr, nextMethodChar, context)) { //Iterates over all 'next()' characters
                            log.clear();
                            m.region(stringPosition, nextMethodChar);
                            if (m.matches()) {
                                res = parse_i(pattern, nextMethodChar, closingCharacterIndex + 1); //Parses the rest of the expression
                                if (res != null) {
                                    res.regexes.add(0, m.toMatchResult()); //Adds the MatchResult to the ParseResult's list
                                    log.printLog();
                                    return res; //Won't happen until the whole pattern is parsed, actually
                                }
                            }
                        }
                        log.printError(null);
                        return null;
                    } finally {
                        log.stop();
                    }
                }
                case ']': //Those 2 previous cases were the hardest. Now it's easy, I think.
                case ')': //Closing  ']' or ')' case
                    patternPosition++; // Just goes to the next character in the pattern
                    continue; //Continues so that :
                    // - the rest of the switch doesn't trigger
                    // - The switch can be re-triggered
                case '|':
                    final int nextBracketIndex = nextBracket(pattern, ')', '(', patternPosition + 1, getGroupLevel(pattern, patternPosition) != 0); //Gets the closing ')'
                    if (nextBracketIndex == -1) { //if the bracket wasn't found without it throwing an error, meaning the pipe wasn't in a group
                        if (stringPosition == expr.length()) { //If 'stringPosition' is the end of the string
                            patternPosition = pattern.length(); //Make 'patternPosition' go to the end of the pattern
                            break;
                        } else { //This part is really confusing me. Maybe having no group makes lonely pipes behave as if they were in a group ? It seems like it. Does the same in regex
                            stringPosition = 0; //Reset the string position ?
                            patternPosition++; //Go to the next pattern character
                            continue;
                        }
                    } else { //If the closing ')' was found
                        patternPosition = nextBracketIndex + 1; //Go after it
                        break;
                    }
                case ' ': //Space case ? What ? I guess it's for ignoring whitespace but we'll see
              /*If...*/ if (stringPosition == 0 /*We're at the beginning of the expression*/ || /*Or at the end of it*/ stringPosition == expr.length() || /*Or...*/(stringPosition > 0 /*We're after the start and*/ && expr.charAt(stringPosition - 1) == ' ') /*The character behind us is a space too*/) {
                        patternPosition++; //We go forward, ignoring that whitespace
                        continue;
                    } else if (expr.charAt(stringPosition) != ' ') { //If the character corresponding to the pattern's whitespace in the expression is not a space, then that's not a match.
                        return null;
                    }
                    //If none of these cases happened, we just go forward
                    stringPosition++;
                    patternPosition++;
                    continue;
                case '\\': //Backslash case :
                    patternPosition++; //We go forward
                    if (patternPosition == pattern.length()) //A pattern can't end with a backslash
                        throw new MalformedPatternException(pattern, "Must not end with a backslash");
                    //There's a fall-through because the backslash needs another character to work with
                    //$FALL-THROUGH$
                default:
                    if (stringPosition == expr.length() /*I don't understand this at all*/ || Character.toLowerCase(pattern.charAt(patternPosition)) != Character.toLowerCase(expr.charAt(stringPosition)) /*Makes sense. The parsing fails if the the character in the pattern and the expression isn't the same. Note : the match is case-insensitive*/) //TODO debug the first check
                        return null;
                    //Those are obvious, but the above check is completely blocking me
                    stringPosition++;
                    patternPosition++;
            } //End of switch
        } //End of while loop
        if (stringPosition == expr.length() && patternPosition == pattern.length()) //If the pattern had no special characters in it
            return new ParseResult(this, pattern);
        return null;
    }

    public final static class ParseResult {
        public final Expression<?>[] exprs;
        public final List<MatchResult> regexes = new ArrayList<>(1);
        public final String expr;
        /**
         * Defaults to 0. Any marks encountered in the pattern will be XORed with the existing value, in particular if only one mark is encountered this value will be set to that
         * mark.
         */
        public int mark = 0;

        public ParseResult(final SkriptParser parser, final String pattern) {
            expr = parser.expr;
            exprs = new Expression<?>[countUnescaped(pattern, '%') / 2]; //How many actual types there are in the pattern
        }
    }

    private final static class MalformedPatternException extends RuntimeException {
        private static final long serialVersionUID = -5133477361763823946L;

        public MalformedPatternException(final String pattern, final String message) {
            this(pattern, message, null);
        }

        public MalformedPatternException(final String pattern, final String message, final @Nullable Throwable cause) {
            super(message + " [pattern: " + pattern + "]", cause);
        }
    }

    private final static class ExprInfo {
        final ClassInfo<?>[] classes;
        final boolean[] isPlural;
        boolean isOptional;
        int flagMask = ~0;
        int time = 0;
        public ExprInfo(final int length) {
            classes = new ClassInfo[length];
            isPlural = new boolean[length];
        }
    }
}
