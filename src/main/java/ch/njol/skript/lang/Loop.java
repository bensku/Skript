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
package ch.njol.skript.lang;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.util.ContainerExpression;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;

/**
 * A trigger section which represents a loop.
 * 
 * @author Peter Güttinger
 */
public class Loop extends TriggerSection {
	
	private final Expression<?> expr;
	
	private transient Map<Event, Object> current = new WeakHashMap<Event, Object>();
	private transient Map<Event, Iterator<?>> currentIter = new WeakHashMap<Event, Iterator<?>>();
	
	@Nullable
	private TriggerItem actualNext;
	
	@SuppressWarnings("unchecked")
	public <T> Loop(final Expression<?> expr, final SectionNode node) {
		assert expr != null;
		assert node != null;
		if (Container.class.isAssignableFrom(expr.getReturnType())) {
			final ContainerType type = expr.getReturnType().getAnnotation(ContainerType.class);
			if (type == null)
				throw new SkriptAPIException(expr.getReturnType().getName() + " implements Container but is missing the required @ContainerType annotation");
			this.expr = new ContainerExpression((Expression<? extends Container<?>>) expr, type.value());
		} else {
			this.expr = expr;
		}
		ScriptLoader.currentSections.add(this);
		ScriptLoader.currentLoops.add(this);
		try {
			setTriggerItems(ScriptLoader.loadItems(node));
		} finally {
			ScriptLoader.currentLoops.remove(ScriptLoader.currentLoops.size() - 1);
			ScriptLoader.currentSections.remove(ScriptLoader.currentSections.size() - 1);
		}
		super.setNext(this);
	}
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		Iterator<?> iter = currentIter.get(e);
		if (iter == null) {
			iter = expr instanceof Variable ? ((Variable<?>) expr).variablesIterator(e) : expr.iterator(e);
			if (iter != null) {
				if (iter.hasNext())
					currentIter.put(e, iter);
				else
					iter = null;
			}
		}
		if (iter == null || !iter.hasNext()) {
			if (iter != null)
				currentIter.remove(e); // a loop inside another loop can be called multiple times in the same event
			debug(e, false);
			return actualNext;
		} else {
			current.put(e, iter.next());
			return walk(e, true);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "loop " + expr.toString(e, debug);
	}
	
	@Nullable
	public Object getCurrent(final Event e) {
		return current.get(e);
	}
	
	public Expression<?> getLoopedExpression() {
		return expr;
	}
	
	@Override
	public Loop setNext(final @Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}
	
	@Nullable
	public TriggerItem getActualNext() {
		return actualNext;
	}
	
}
