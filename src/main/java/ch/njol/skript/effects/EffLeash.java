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
package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Leash entities")
@Description("Leash living entities to other entities")
@Examples("leash the player to the target entity")
@Since("INSERT VERSION")
public class EffLeash extends Effect {

	static {
		Skript.registerEffect(EffLeash.class,
				"(leash|lead) %livingentities% to %entity%",
				"make %entity% (leash|lead) %livingentities%",
				"un(leash|lead) %livingentities% from %entity%");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> holder;
	@SuppressWarnings("null")
	private Expression<LivingEntity> targets;
	private boolean unleash;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		holder = (Expression<Entity>) exprs[1 - matchedPattern];
		targets = (Expression<LivingEntity>) exprs[matchedPattern];
		unleash = matchedPattern == 2;
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		Entity entity = holder.getSingle(e);
		if (entity == null)
			return;
		for (LivingEntity target : targets.getArray(e)) {
			target.setLeashHolder(!unleash ? entity : null);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return unleash ? "un" : "" + "leash " + targets.toString(e, debug) + (unleash ? " from " : "" + " to ") + holder.toString(e, debug);
	}

}
