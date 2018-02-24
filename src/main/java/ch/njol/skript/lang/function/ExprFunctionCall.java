/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class ExprFunctionCall<T> extends SimpleExpression<T> {
	private final FunctionReference<T> function;

	public ExprFunctionCall(final FunctionReference<T> function) {
		this.function = function;
	}

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		assert false;
		return false;
	}

	@Override
	@Nullable
	protected T[] get(final Event e) {
		return function.execute(e);
	}

	@Override
	public boolean isSingle() {
		return function.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return function.getReturnType();
	}

	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return function.toString(e, debug);
	}
}
