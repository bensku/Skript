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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Entity from UUID")
@Description({"Get an entity from a UUID. Useful for retrieving an entity from an entity's UUID stored in a variable."})
@Examples({"teleport player to entity from uuid {some::entity::id}", "delete entity from uuid {some::entity::id}"})
@Since("INSERT VERSION")
@RequiredPlugins("Spigot 1.11+")
public class ExprEntityFromUUID extends PropertyExpression<String, Entity> {
	
	static {
		if (Skript.methodExists(Bukkit.class, "getEntity", UUID.class)) {
			Skript.registerExpression(ExprEntityFromUUID.class, Entity.class, ExpressionType.PROPERTY,
				"entit(y|ies) from [uuid[s]] %strings%");
		}
	}
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<String>) exprs[0]);
		return true;
	}
	
	@Override
	protected Entity[] get(Event event, String[] strings) {
		return get(strings, s -> Bukkit.getEntity(UUID.fromString(s)));
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(Event event, boolean d) {
		return "Entity from uuid " + getExpr().toString(event, d);
	}
	
}
