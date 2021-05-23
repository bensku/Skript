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
package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.CodeSection;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NotNullFieldNotInitialized")
public class ConditionalSection extends CodeSection {

	static {
		Skript.registerSection(ConditionalSection.class,
			"else",
			"else if <.+>",
			"[(1¦if)] <.+>");
	}

	private enum ConditionalType {
		ELSE, ELSE_IF, IF
	}

	private ConditionalType type;
	private Condition condition;

	private Kleenean hadDelayBefore;
	private Kleenean hasDelayAfter;

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		type = ConditionalType.values()[matchedPattern];
		if (type == ConditionalType.IF || type == ConditionalType.ELSE_IF) {
			String expr = parseResult.regexes.get(0).group();
			// Don't print a default error if 'if' keyword wasn't provided
			condition = Condition.parse(expr, parseResult.mark != 0 ? "Can't understand this condition: '" + expr + "'" : null);
			if (condition == null)
				return false;
		}

		ConditionalSection lastIf;
		if (type != ConditionalType.IF) {
			lastIf = getIf(triggerItems);
			if (lastIf == null) {
				if (type == ConditionalType.ELSE_IF)
					Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
				else
					Skript.error("'else' has to be placed just after another 'if' or 'else if' section");
				return false;
			}
		} else {
			lastIf = null;
		}

		hadDelayBefore = getParser().getHasDelayBefore();
		loadCode(sectionNode);
		hasDelayAfter = getParser().getHasDelayBefore();
		if (hadDelayBefore.isTrue())
			return true;

		if (type == ConditionalType.ELSE) {
			if (!lastIf.hadDelayBefore.isTrue()) {
				if (getParser().getHasDelayBefore().isTrue()
						&& lastIf.hasDelayAfter.isTrue()
						&& getElseIfs(triggerItems).stream().map(ConditionalSection::getHadDelayBefore).allMatch(Kleenean::isTrue)) {
					getParser().setHasDelayBefore(Kleenean.TRUE);
				} else {
					if (!(getParser().getHasDelayBefore().isFalse()
							&& getElseIfs(triggerItems).stream().map(ConditionalSection::getHadDelayBefore).allMatch(Kleenean::isFalse)))
						getParser().setHasDelayBefore(Kleenean.UNKNOWN);
					else
						getParser().setHasDelayBefore(lastIf.hadDelayBefore);
				}
			}
		} else {
			if (!getParser().getHasDelayBefore().isFalse()) {
				getParser().setHasDelayBefore(Kleenean.UNKNOWN);
			}
		}

		return true;
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event e) {
		if (type == ConditionalType.ELSE || condition.check(e)) {
			if (last != null)
				last.setNext(getSkippedNext());
			return first != null ? first : getSkippedNext();
		} else {
			return getNext();
		}
	}

	@Nullable
	private TriggerItem getSkippedNext() {
		TriggerItem next = getNext();
		while (next instanceof ConditionalSection && ((ConditionalSection) next).type != ConditionalType.IF)
			next = next.getNext();
		return next;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		switch (type) {
			case IF:
				return "if " + condition.toString(e, debug);
			case ELSE_IF:
				return "else if " + condition.toString(e, debug);
			case ELSE:
				return "else";
			default:
				throw new IllegalStateException();
		}
	}

	private Kleenean getHadDelayBefore() {
		return hadDelayBefore;
	}

	@Nullable
	private static ConditionalSection getIf(List<TriggerItem> triggerItems) {
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof ConditionalSection) {
				ConditionalSection conditionalSection = (ConditionalSection) triggerItem;

				if (conditionalSection.type == ConditionalType.IF)
					return conditionalSection;
				else if (conditionalSection.type == ConditionalType.ELSE)
					return null;
			} else {
				return null;
			}
		}
		return null;
	}

	private static List<ConditionalSection> getElseIfs(List<TriggerItem> triggerItems) {
		List<ConditionalSection> list = new ArrayList<>();
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof ConditionalSection) {
				ConditionalSection conditionalSection = (ConditionalSection) triggerItem;

				if (conditionalSection.type == ConditionalType.ELSE_IF)
					list.add(conditionalSection);
				else
					break;
			} else {
				break;
			}
		}
		return list;
	}

}
