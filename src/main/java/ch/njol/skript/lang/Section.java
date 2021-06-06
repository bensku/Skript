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
package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * A section that can decide what it does with its contents, as code isn't parsed by default.
 *
 * @see Skript#registerSection(Class, String...)
 */
public abstract class Section extends TriggerSection implements SyntaxElement {

	@Override
	public final boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		SectionContext sectionContext = getParser().getData(SectionContext.class);
		return init(exprs, matchedPattern, isDelayed, parseResult, sectionContext.sectionNode, sectionContext.triggerItems);
	}

	public abstract boolean init(Expression<?>[] exprs,
								 int matchedPattern,
								 Kleenean isDelayed,
								 ParseResult parseResult,
								 SectionNode sectionNode,
								 List<TriggerItem> triggerItems);

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	public static Section parse(String expr, String defaultError, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		SectionContext sectionContext = ParserInstance.get().getData(SectionContext.class);
		sectionContext.sectionNode = sectionNode;
		sectionContext.triggerItems = triggerItems;

		return (Section) SkriptParser.parse(expr, (Iterator) Skript.getSections().iterator(), defaultError);
	}

	static {
		ParserInstance.registerData(SectionContext.class, SectionContext::new);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private static class SectionContext extends ParserInstance.Data {

		private SectionNode sectionNode;
		private List<TriggerItem> triggerItems;

		public SectionContext(ParserInstance parserInstance) {
			super(parserInstance);
		}

	}

}
