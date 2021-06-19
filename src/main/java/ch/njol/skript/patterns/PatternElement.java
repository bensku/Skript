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

import org.jetbrains.annotations.Nullable;

// TODO add more log handlers (match result copies)
public abstract class PatternElement {

	@Nullable
	PatternElement next;

	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		return match(expr, matchResult, next);
	}

	@Nullable
	public abstract MatchResult match(String expr, MatchResult matchResult, @Nullable PatternElement next);

	@Nullable
	protected MatchResult matchNext(String expr, MatchResult matchResult) {
		if (next == null) {
			return matchResult.exprOffset == expr.length() ? matchResult : null;
		}
		return next.match(expr, matchResult);
	}

	@Override
	public abstract String toString();

	protected String nextToString() {
		return next == null ? "" : next.toString();
	}

}
