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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A result from pattern matching.
 */
public class MatchResult {

	int exprOffset;

	Expression<?>[] expressions = new Expression[0];
	String expr;
	int mark;
	List<java.util.regex.MatchResult> regexResults = new ArrayList<>();

	// SkriptParser stuff
	ParseContext parseContext = ParseContext.DEFAULT;
	int flags;

	public MatchResult copy() {
		MatchResult matchResult = new MatchResult();
		matchResult.exprOffset = this.exprOffset;
		matchResult.expressions = this.expressions.clone();
		matchResult.expr = this.expr;
		matchResult.mark = this.mark;
		matchResult.regexResults = new ArrayList<>(this.regexResults);
		matchResult.parseContext = this.parseContext;
		matchResult.flags = this.flags;
		return matchResult;
	}

	public ParseResult toParseResult() {
		ParseResult parseResult = new ParseResult(expr, expressions);
		parseResult.regexes.addAll(regexResults);
		parseResult.mark = mark;
		return parseResult;
	}

	@Override
	public String toString() {
		return "MatchResult{" +
			"exprOffset=" + exprOffset +
			", expressions=" + Arrays.toString(expressions) +
			", expr='" + expr + '\'' +
			", mark=" + mark +
			", regexResults=" + regexResults +
			", parseContext=" + parseContext +
			", flags=" + flags +
			'}';
	}

}
