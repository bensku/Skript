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

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Game Rule")
@Description("The gamerule of a world.")
@Examples({"set the gamerule \"commandBlockOutput\" of world \"world\" to false"})
@Since("2.5")
public class ExprGameRule extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprGameRule.class, Object.class, ExpressionType.PROPERTY,
			"[the] game[ ]rule %string% of %world%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> gamerule;
	@SuppressWarnings("null")
	private Expression<World> world;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		gamerule = (Expression<String>) exprs[0];
		world = (Expression<World>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET) return CollectionUtils.array(Object.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) {
		assert delta != null;
		if (mode == Changer.ChangeMode.SET) {
			World gameruleWorld = world.getSingle(e);
			if (gameruleWorld == null) return;
			GameRule bukkitGamerule = getGamerule(e);
			if (bukkitGamerule == null) return;
			gameruleWorld.setGameRule(bukkitGamerule, delta[0]);
		}
	}
		
	@Nullable
	@Override
	protected Object[] get(Event e) {
		World gameruleWorld = world.getSingle(e);
		if (gameruleWorld == null) return null;
		GameRule<?> bukkitGamerule = getGamerule(e);
		if (bukkitGamerule == null) return null;
		return new Object[] {gameruleWorld.getGameRuleValue(bukkitGamerule)};
	}
	
	@Nullable
	private GameRule<?> getGamerule(Event e) {
		String stringGamerule = gamerule.getSingle(e);
		if (stringGamerule == null) return null;
		GameRule<?> bukkitGamerule = GameRule.getByName(stringGamerule);
		return bukkitGamerule;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<?> getReturnType() {
		return GameRule.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the gamerule value of " + gamerule.toString(e, debug) + " for world " + world.toString(e, debug);
	}
}
