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
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Adult")
@Description("Whether or not an entity is an adult.")
@Examples("player's target is an adult")
@Since("INSERT VERSION")
public class CondIsAdult extends Condition {
	
	static {
		Skript.registerCondition(CondIsAdult.class, "%entity% (1¦is|2¦is(n't| not)) [a[n]] adult");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		setNegated(parser.mark == 2);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		if (entities.getAnd())
			return entities.stream(e).allMatch(entity -> {
				if (entity instanceof Ageable) return ((Ageable) entity).isAdult();
				else if (entity instanceof Zombie) return !((Zombie) entity).isBaby();
				else return false;
			}) != isNegated();
		else
			return entities.stream(e).anyMatch(entity -> {
				if (entity instanceof Ageable) return ((Ageable) entity).isAdult();
				else if (entity instanceof Zombie) return !((Zombie) entity).isBaby();
				else return false;
			}) != isNegated();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return entities.toString(e, debug) + (isNegated() ? " isn't " : " is ") + "an adult";
	}
}