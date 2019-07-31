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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Power")
@Description("Gets the power/force within a supported event.")
@Examples("power is greater than or equal to 2")
@Since("2.4")
public class ExprPower extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPower.class, Number.class, ExpressionType.SIMPLE, "power");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (ScriptLoader.isCurrentEvent(HorseJumpEvent.class, FireworkExplodeEvent.class))
			return true;
		Skript.error("The power event expression can only be used within HorseJumpEvent and FireworkExplodeEvent", ErrorQuality.SEMANTIC_ERROR);
		return false;
	}

	@Override
	protected Number[] get(Event e) {
		Number power = 0;
		if (e instanceof HorseJumpEvent)
			power = ((HorseJumpEvent)e).getPower();
		else 
			power = ((FireworkExplodeEvent)e).getEntity().getFireworkMeta().getPower();
		return new Number[] {power};
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
		return "power";
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.SET || mode == ChangeMode.REMOVE)
			return new Class[] {Number.class};
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		Number power = (Number) delta[0];
		if (e instanceof FireworkExplodeEvent)
			((HorseJumpEvent)e).setPower(power.floatValue());
		else
			((FireworkExplodeEvent)e).getEntity().getFireworkMeta().setPower(power.intValue());
	}

}
