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
 * Represents a conditional trigger section.
 * <p>
 * TODO: make this an expression
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Condition
 */
public class Conditional extends TriggerSection {
	
	private final Condition cond;
	
	@Nullable
	private TriggerSection elseClause = null;
	
	public Conditional(final Condition cond, final SectionNode node) {
		super(node);
		this.cond = cond;
	}
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		if (cond.run(e)) {
			return walk(e, true);
		} else {
			debug(e, false);
			if (elseClause != null)
				return elseClause;
			return getNext();
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return cond.toString(e, debug);
	}
	
	public void loadElseClause(final SectionNode node) {
		assert elseClause == null || elseClause instanceof Conditional;
		if (elseClause != null) {
			((Conditional) elseClause).loadElseClause(node);
			return;
		}
		elseClause = new TriggerSection(node) {
			@Override
			@Nullable
			public TriggerItem walk(final Event e) {
				return walk(e, true);
			}
			
			@Override
			public String toString(final @Nullable Event e, final boolean debug) {
				return "else";
			}
		}
				.setParent(getParent())
				.setNext(getNext());
	}
	
	public void loadElseIf(final Condition cond, final SectionNode n) {
		assert elseClause == null || elseClause instanceof Conditional;
		if (elseClause != null) {
			((Conditional) elseClause).loadElseIf(cond, n);
			return;
		}
		elseClause = new Conditional(cond, n)
				.setParent(getParent())
				.setNext(getNext());
	}
	
	public boolean hasElseClause() {
		return elseClause != null && !(elseClause instanceof Conditional);
	}
	
	@Override
	public Conditional setNext(final @Nullable TriggerItem next) {
		super.setNext(next);
		if (elseClause != null)
			elseClause.setNext(next);
		return this;
	}
	
	@Override
	public Conditional setParent(final @Nullable TriggerSection parent) {
		super.setParent(parent);
		if (elseClause != null)
			elseClause.setParent(parent);
		return this;
	}
	
}
