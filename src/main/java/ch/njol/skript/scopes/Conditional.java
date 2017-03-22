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
package ch.njol.skript.scopes;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.BiPeekingIterator;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a conditional trigger section.
 * <p>
 * TODO: make this an expression
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Condition
 */
@SuppressWarnings("unused")
public class Conditional extends TriggerSection {

	@Nullable
	private Condition cond;
	private ConditionalMode mode;
	private List<Conditional> conditionals;

	{
		this.conditionals = ScriptLoader.loadItems(ScriptLoader.getCurrentNode()).stream()
				.filter(t -> t instanceof Conditional)
				.map(c -> (Conditional) c)
				.collect(Collectors.toList());
	}

	static {
		Skript.registerScope(
				Conditional.class,
				"(0¦|1¦if|2¦else if) <.+>", "else"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 0) {
			cond = Condition.parse(parseResult.regexes.get(0).group(), "Can't understand this condition " + parseResult.regexes.get(0).group());
			mode = ConditionalMode.byId(parseResult.mark);
		} else {
			cond = null;

		}
		mode = matchedPattern == 0 ? ConditionalMode.byId(parseResult.mark) : ConditionalMode.ELSE;
		BiPeekingIterator<Conditional> it = new BiPeekingIterator<>(conditionals);
		while (it.hasNext()) {
			Conditional current = it.next();
			if (current.mode == ConditionalMode.ELSE_IF || current.mode == ConditionalMode.ELSE) {
				if (!it.hasPrevious()) {
					Skript.error("An 'else if' or an 'else' can only be placed after an 'if' or an 'else if'", ErrorQuality.SEMANTIC_ERROR);
					return false;
				} else if (it.peekPrevious().mode != ConditionalMode.IF && it.peekPrevious().mode == ConditionalMode.ELSE_IF) {
					Skript.error("An 'else if' or an 'else' can only be placed after an 'if' or an 'else if'", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		if (cond.run(e)) {
			return walk(e, true);
		} else {
			debug(e, false);
			return getNext();
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return cond.toString(e, debug);
	}

	@SuppressWarnings("unused")
	private enum ConditionalMode {
		NONE(0),
		IF(2),
		ELSE_IF(3),
		ELSE(4);

		private int id;

		ConditionalMode(int id) {
			this.id = id;
		}

		public static ConditionalMode byId(int id) {
			return Arrays.stream(values()).filter(c -> c.id == id).findFirst().orElse(null);
		}
	}
}
