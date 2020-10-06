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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.config.SectionNode;

/**
 * @author Peter Güttinger
 */
public class While extends TriggerSection {
	
	private final Condition c;
	
	public While(final Condition c, final SectionNode n) {
		super(n);
		this.c = c;
		super.setNext(this);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "while " + c.toString(e, debug);
	}
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		if (c.check(e)) {
			return walk(e, true);
		} else {
			debug(e, false);
			return actualNext;
		}
	}
	
	@Nullable
	private TriggerItem actualNext;
	
	@Override
	public While setNext(final @Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}
	
	@Nullable
	public TriggerItem getActualNext() {
		return actualNext;
	}
	
}
