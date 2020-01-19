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

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTransformEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
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

@Name("Transformed Entities")
@Description("Entity/entities that are the result of an entity transform event. Cannot be used outside of entity transform events.")
@Events("entity transform")
@Examples({"the transformed entity #Get a single entity produced from the transformation, e.g. zombie drown", 
	"the transformed entities #Get all entities produced from the transformation, e.g. slime split"})
@Since("INSERT VERSION")
public class ExprTransformedEntities extends SimpleExpression<Entity> {
	
	static {
		if (Skript.classExists("org.bukkit.event.entity.EntityTransformEvent")) {
			Skript.registerExpression(ExprTransformedEntities.class, Entity.class, ExpressionType.SIMPLE, 
					"[the] transformed entity",
					"[the] transformed entities");
		}
	}
	
	private boolean getSingle;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EntityTransformEvent.class)) {
			Skript.error("The transformed entities are only usable in entity transform events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		getSingle = matchedPattern == 0;
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(Event e) {
		if (getSingle)
			return new Entity[] {((EntityTransformEvent) e).getTransformedEntity()};
		List<Entity> es = ((EntityTransformEvent) e).getTransformedEntities();
		return es.stream()
			.toArray(Entity[]::new);
	}
	
	@Override
	@Nullable
	public Iterator<Entity> iterator(Event e) {
		return ((EntityTransformEvent) e).getTransformedEntities().iterator();
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	public boolean isSingle() {
		return getSingle;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "transformed " + (getSingle ? "entity" : "entities");
	}
	
}
