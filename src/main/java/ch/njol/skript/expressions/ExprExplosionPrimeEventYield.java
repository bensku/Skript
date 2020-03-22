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
import org.bukkit.event.entity.ExplosionPrimeEvent;
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

@Name("Explosion Prime Event Explosion Yield")
@Description("The yield/radius of the explosion in an explosion prime event. This is how big the explosion is.")
@Events("explosion prime")
@Examples({"on explosion prime:",
		"\tset the explosion radius to 10"})
@Since("INSERT VERSION")
public class ExprExplosionPrimeEventYield extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprExplosionPrimeEventYield.class, Number.class, ExpressionType.SIMPLE, "[the] explosion (radius|size|yield)");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(ExplosionPrimeEvent.class)) {
			Skript.error("The explosion radius is only usable in explosion prime events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		return CollectionUtils.array(((ExplosionPrimeEvent) e).getRadius());
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
		float f = delta == null ? 0 : ((Number) delta[0]).floatValue();
		ExplosionPrimeEvent e = (ExplosionPrimeEvent) event;
		switch (mode) {
			case SET:
				e.setRadius(f);
				break;
			case ADD:
				float add = e.getRadius() + f;
				if (add < 0) add = 0;
				e.setRadius(add);
				break;
			case REMOVE:
				float subtract = e.getRadius() - f;
				if (subtract < 0) subtract = 0;
				e.setRadius(subtract);
				break;
			case DELETE:
				e.setRadius(0);
				break;
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "explosion prime event explosion yield";
	}

}
