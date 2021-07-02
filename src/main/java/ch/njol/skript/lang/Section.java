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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * A section that can decide what it does with its contents, as code isn't parsed by default.
 *
 * @see Skript#registerSection(Class, String...)
 */
public abstract class Section extends TriggerSection implements SyntaxElement {

	/**
	 * This method should not be overridden unless you know what you are doing!
	 */
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		SectionContext sectionContext = getParser().getData(SectionContext.class);
		return init(exprs, matchedPattern, isDelayed, parseResult, sectionContext.sectionNode, sectionContext.triggerItems);
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event e) {
		if (last != null) { // We don't want to start running code outside of the section
			last.setNext(null);
		}
		execute(e);
		return getNext();
	}

	public abstract boolean init(Expression<?>[] exprs,
								 int matchedPattern,
								 Kleenean isDelayed,
								 ParseResult parseResult,
								 SectionNode sectionNode,
								 List<TriggerItem> triggerItems);

	/**
	 * This method is called when a section is reached. When overriding this method,
	 * be sure to call {@link #runSection(Event)} to run the section.
	 */
	public void execute(Event event) {
		runSection(event);
	}

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 * <br>
	 * This method itself does not modify {@link ParserInstance#getHasDelayBefore()}
	 * (although the loaded code may change it), the calling code must deal with this.
	 */
	protected void loadCode(SectionNode sectionNode) {
		List<TriggerSection> currentSections = ParserInstance.get().getCurrentSections();
		currentSections.add(this);
		try {
			setTriggerItems(ScriptLoader.loadItems(sectionNode));
		} finally {
			currentSections.remove(currentSections.size() - 1);
		}
	}

	/**
	 * Loads the code using {@link #loadCode(SectionNode)}.
	 * <br>
	 * This method also adjusts {@link ParserInstance#getHasDelayBefore()} to expect the code
	 * to be called zero or more times. This is done by setting {@code hasDelayBefore} to {@link Kleenean#UNKNOWN}
	 * if the loaded section has a possible or definite delay in it.
	 */
	protected void loadOptionalCode(SectionNode sectionNode) {
		Kleenean hadDelayBefore = getParser().getHasDelayBefore();
		loadCode(sectionNode);
		if (hadDelayBefore.isTrue())
			return;
		if (!getParser().getHasDelayBefore().isFalse())
			getParser().setHasDelayBefore(Kleenean.UNKNOWN);
	}

	/**
	 * Runs the code within a section with the given event.
	 * <b>ALL</b> sections must call either {@link #loadCode(SectionNode)} or {@link #loadOptionalCode(SectionNode)}
	 * before calling this method.
	 */
	protected final void runSection(Event event) {
		if (first == null) {
			Skript.exception("All sections must call a load method before calling Section#runSection.");
		}
		TriggerItem.walk(first, event);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	public static Section parse(String expr, @Nullable String defaultError, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		SectionContext sectionContext = ParserInstance.get().getData(SectionContext.class);
		sectionContext.sectionNode = sectionNode;
		sectionContext.triggerItems = triggerItems;

		return (Section) SkriptParser.parse(expr, (Iterator) Skript.getSections().iterator(), defaultError);
	}

	static {
		ParserInstance.registerData(SectionContext.class, SectionContext::new);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	protected static class SectionContext extends ParserInstance.Data {

		protected SectionNode sectionNode;
		protected List<TriggerItem> triggerItems;

		public SectionContext(ParserInstance parserInstance) {
			super(parserInstance);
		}

	}

}
