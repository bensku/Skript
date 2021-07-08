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

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * To be used where a SkriptEvent may be needed, but no actual one exists.
 * @see Section#loadCode(SectionNode, String, Class[]) 
 */
public class FakeSkriptEvent extends SkriptEvent {

	private final String name;

	public FakeSkriptEvent(String name) {
		this.name = name;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return false;
	}

	@Override
	public boolean check(Event e) {
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return name;
	}

}
