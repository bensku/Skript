/*
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
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is on Ground")
@Description("Checks if entities are on ground or not.")
@Examples("player is not on ground")
@Since("2.2-dev26")
public class CondIsOnGround extends Condition {
	static {
		Skript.registerCondition(CondIsOnGround.class, "%entities% (is|are) on ground");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	public boolean check(final Event e) {
		return entities.check(e, (Entity::isOnGround));
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is " + (e != null ? entities.getAll(e) : "") + " on ground";
	}
}
