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
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A section that can decide what it does with its contents, as code isn't parsed by default.
 * <br><br>
 * In most cases though, a section should load its code through one of the following loading methods:
 * {@link #loadCode(SectionNode)}, {@link #loadCode(SectionNode, String, Class[])}, {@link #loadOptionalCode(SectionNode)}
 * <br><br>
 * Every section must override the {@link TriggerSection#walk(Event)} method. In this method, you can determine whether
 * or not the section should run. If you have stored a {@link Trigger} from {@link #loadCode(SectionNode, String, Class[])}, you
 * should not run it with this event passed in this walk method.
 * <br><br>
 * If you wish to run the section, you should return {@link TriggerSection#first} or {@link TriggerSection#walk(Event, boolean)}
 * where the boolean value is true. The walk method is likely preferred as it will verify that {@link TriggerSection#first} is not null,
 * and, if it is return {@link TriggerSection#getNext()} instead, meaning execution can continue.
 * <br><br>
 * If you do not wish to run this section, you should return {@link TriggerSection#getNext()} or {@link TriggerSection#walk(Event, boolean)}
 * where the boolean value is false. If you do run the section, the code after the section will be ran unless you are using a {@link Trigger}
 * from {@link #loadCode(SectionNode, String, Class[])}.
 * <br><br>
 * It is possible to run the section without a Trigger and not continue on by setting {@link TriggerItem#setNext(TriggerItem)} to null
 * using {@link TriggerSection#last}. It is recommend that you have a way for the code
 * after the section to eventually run, as to not leave users confused.
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

	public abstract boolean init(Expression<?>[] exprs,
								 int matchedPattern,
								 Kleenean isDelayed,
								 ParseResult parseResult,
								 SectionNode sectionNode,
								 List<TriggerItem> triggerItems);

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 * <br>
	 * This method itself does not modify {@link ParserInstance#getHasDelayBefore()}
	 * (although the loaded code may change it), the calling code must deal with this.
	 */
	protected void loadCode(SectionNode sectionNode) {
		List<TriggerSection> currentSections = getParser().getCurrentSections();
		currentSections.add(this);
		try {
			setTriggerItems(ScriptLoader.loadItems(sectionNode));
		} finally {
			currentSections.remove(currentSections.size() - 1);
		}
	}

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 *
	 * This method differs from {@link #loadCode(SectionNode)} in that it
	 * is meant for code that will be executed in a different event.
	 *
	 * @param sectionNode The section node to load.
	 * @param name The name of the event(s) being used.
	 * @param events The event(s) during the section's execution.
	 * @return A trigger containing the loaded section. This should be stored and used
	 * to run the section one or more times.
	 */
	@SafeVarargs
	protected final Trigger loadCode(SectionNode sectionNode, String name, Class<? extends Event>... events) {
		ParserInstance parser = getParser();

		String previousName = parser.getCurrentEventName();
		Class<? extends Event>[] previousEvents = parser.getCurrentEvents();
		SkriptEvent previousSkriptEvent = parser.getCurrentSkriptEvent();
		List<TriggerSection> previousSections = parser.getCurrentSections();
		Kleenean previousDelay = parser.getHasDelayBefore();

		parser.setCurrentEvent(name, events);
		parser.setCurrentSkriptEvent(null);
		List<TriggerSection> sections = new ArrayList<>();
		sections.add(this);
		parser.setCurrentSections(sections);
		parser.setHasDelayBefore(Kleenean.FALSE);
		List<TriggerItem> triggerItems = ScriptLoader.loadItems(sectionNode);

		//noinspection ConstantConditions - We are resetting it to what it was
		parser.setCurrentEvent(previousName, previousEvents);
		parser.setCurrentSkriptEvent(previousSkriptEvent);
		parser.setCurrentSections(previousSections);
		parser.setHasDelayBefore(previousDelay);

		Config script = parser.getCurrentScript();
		return new Trigger(script != null ? script.getFile() : null, name, new FakeSkriptEvent(name), triggerItems);
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
