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
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.sun.istack.internal.NotNull;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ExprPlayersXP extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprPlayersXP.class, Number.class, ExpressionType.SIMPLE, "(%player%[']s ([e]xp|experience)|([e]xp|experience) of %player%)");
	}
	
	@Nullable
	private Expression<Player> player;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		player = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event e) {
		assert player != null;
		Player p = player.getSingle(e);
		if (p != null) {
			return new Number[] {p.getTotalExperience()};
		}
		return new Number[0];
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "XP level";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL) {
			return null;
		}
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(@NotNull Event event, final @Nullable Object[] delta, ChangeMode mode) {
		assert player != null;
		Player p = player.getSingle(event);
		int xp = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (p != null) {
			switch(mode) {
				case SET:
				case DELETE:
				case RESET:
					p.setTotalExperience(xp);
					break;
				case ADD:
					p.setTotalExperience(p.getTotalExperience() + xp);
					break;
				case REMOVE:
					p.setTotalExperience(p.getTotalExperience() - xp < 0 ? 0 : p.getTotalExperience() - xp);
					break;
				case REMOVE_ALL:
					assert false;
					break;
			}
		}
	}
}
