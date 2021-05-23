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
package ch.njol.skript.lang.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;

import java.util.List;

/**
 * A {@link Section} with code contents.
 */
public abstract class CodeSection extends Section {

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 * <br>
	 * This method does not modify {@code hasDelayBefore} in {@link ParserInstance},
	 * the calling code must deal with this.
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
	 * Loads the code using {@link #loadCode(SectionNode)}, while also
	 * adjusting {@code hasDelayBefore} from {@link ParserInstance}, expecting the loaded code to be called
	 * 0 or more times during execution of this section.
	 */
	protected void loadOptionalCode(SectionNode sectionNode) {
		Kleenean hadDelayBefore = getParser().getHasDelayBefore();
		loadCode(sectionNode);
		if (hadDelayBefore.isTrue())
			return;
		if (!getParser().getHasDelayBefore().isFalse())
			getParser().setHasDelayBefore(Kleenean.UNKNOWN);
	}

}
