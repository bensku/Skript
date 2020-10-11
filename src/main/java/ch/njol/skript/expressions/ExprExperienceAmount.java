/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.ExpBottleEvent;
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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Experience Amount")
@Description("The amount of experience released in an experience bottle hit event.")
@Examples({"on experience bottle hit:",
	"\texperience amount is below 5",
	"\tincrease experience amount by 3"})
@Since("INSERT VERSION")
@Events("Experience Bottle Hit")
public class ExprExperienceAmount extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprExperienceAmount.class, Integer.class, ExpressionType.SIMPLE, "[the] [e]xp[erience] amount");
	}

	@SuppressWarnings("null")
	private Kleenean delay;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(ExpBottleEvent.class)) {
			Skript.error("The expression 'experience amount' may only be used in a experience bottle hit event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		delay = isDelayed;
		return true;
	}

	@Nullable
	@Override
	protected Integer[] get(Event e) {
		return new Integer[]{((ExpBottleEvent) e).getExperience()};
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (delay != Kleenean.FALSE) {
			Skript.error("The experience amount cannot be changed after the event has already passed");
			return null;
		}
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
			case DELETE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int value = delta == null ? 0 : ((Number) delta[0]).intValue();
		switch (mode) {
			case SET:
			case DELETE:
				((ExpBottleEvent) e).setExperience(value);
				break;
			case ADD:
				((ExpBottleEvent) e).setExperience(((ExpBottleEvent) e).getExperience() + value);
				break;
			case REMOVE:
				((ExpBottleEvent) e).setExperience(((ExpBottleEvent) e).getExperience() - value);
				break;
			default:
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "experience amount";
	}

}
