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

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ProjectileUtils;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * Used to set saturation of players. Number is used in case something changes in future...
 * @author bensku
 */
@Name("Saturation")
@Description("The saturation of a player.")
@Examples({"saturation of player is 20 #Not hungry!"})
@Since("2.2-Fixes-V10")
public class ExprSaturation extends PropertyExpression<Player, Number> {
	static {
		Skript.registerExpression(ExprSaturation.class, Number.class, ExpressionType.SIMPLE, "[the] saturation [of %players%]");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		return true;
	}
	
	@Override
	protected Number[] get(final Event e, final Player[] source) {
		return get(source, new Converter<Player, Number>() {
			@Override
			@Nullable
			public Number convert(final Player p) {
				return p.getSaturation();
			}
		});
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {Number.class};
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			for (final Player p : getExpr().getArray(e)) {
				assert p != null : getExpr();
				p.setSaturation(((Number) delta[0]).floatValue());
			}
		} else {
			super.change(e, delta, mode);
		}
	}
	
	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the saturation" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
	}
	
}
