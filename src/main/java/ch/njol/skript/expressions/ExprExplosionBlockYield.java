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
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Explosion Block Yield")
@Description({"The percentage of exploded blocks dropped in an explosion event.",
				"When changing the yield, a value greater than 1 will function the same as using 1.",
				"Attempting to change the yield to a value less than 0 will have no effect."})
@Examples({"on explode:",
			"set the explosion's block yield to 10%"})
@Events("explosion")
@Since("INSERT VERSION")
public class ExprExplosionBlockYield extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprExplosionBlockYield.class, Number.class, ExpressionType.PROPERTY,
				"[the] [explosion['s]] block (yield|amount)",
				"[the] percentage of blocks dropped"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EntityExplodeEvent.class)) {
			Skript.error("The 'explosion block yield' is only usable in an explosion event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		return new Number[]{((EntityExplodeEvent) e).getYield()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		float n = delta == null ? 0 : ((Number) delta[0]).floatValue();
		if (n < 0) // Yield can't be negative
			return;
		if (n > 1) // Treat 'set the explosion's block yield to 500' as setting it to the maximum value (i.e. 1)
			n = 1;
		EntityExplodeEvent e = (EntityExplodeEvent) event;
		switch (mode) {
			case SET:
				e.setYield(n);
				break;
			case ADD:
				float add = e.getYield() + n;
				if (add < 0)
					return;
				if (add > 1)
					add = 1;
				e.setYield(add);
				break;
			case REMOVE:
				float subtract = e.getYield() - n;
				if (subtract < 0)
					return;
				if (subtract > 1)
					subtract = 1;
				e.setYield(subtract);
				break;
			case DELETE:
				e.setYield(0);
				break;
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the explosion's block yield";
	}

}
