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

import org.bukkit.attribute.Attribute;

import java.util.stream.Stream;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Entity Attribute")
@Description({"The attribute value of an entity.",
			 "Note that the movement speed attribute cannot be reliably used for players. For that purpose, use the speed expression instead."})
@Examples({"on damage of player:",
		"	send \"You are wounded!\"",
		"	set victim's attack speed attribute to 2"})
@Since("INSERT VERSION")
public class ExprEntityAttribute extends PropertyExpression<Entity, Number> {
	
	static {
		Skript.registerExpression(ExprEntityAttribute.class, Number.class, ExpressionType.COMBINED,
				"%attributetype% [value] of %entities%",
				"%entities%'[s] %attributetype% [value]");
	}
	
	@Nullable
	private Expression<Attribute> attributes;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attributes = (Expression<Attribute>) exprs[matchedPattern];
		setExpr((Expression<? extends Entity>) exprs[matchedPattern ^ 1]);
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected Number[] get(Event e, Entity[] entities) {
		Attribute a = attributes.getSingle(e);
		return Stream.of(entities)
		    .map(ent -> getAttribute(ent, a).getBaseValue())
		    .toArray(Number[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Attribute a = attributes.getSingle(e);
		double d = ((Number) delta[0]).doubleValue();
		if (mode == ChangeMode.SET) {
			for (Entity entity : getExpr().getArray(e)) {
				AttributeInstance ai = getAttribute(entity, a);
				if (ai != null) {
					ai.setBaseValue(d);
				}
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@SuppressWarnings("null") // For some reason Java thinks that entities.toString(...) is null even though there's a null check ternary 
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "entity " + getExpr().toString(e, debug) + "'s " + (attributes == null ? "" : attributes.toString(e, debug)) + "attribute";
	}
	
	@Nullable
	private static AttributeInstance getAttribute(Entity e, @Nullable Attribute a) {
	    if (a != null && e instanceof Attributable) {
	        return ((Attributable) e).getAttribute(a);
	    }
	   return null;
	}
	
}
