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

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Entity Attribute")
@Description("The attribute value of an entity.")
@Examples({"on damage of player:",
		"	send \"You are wounded!\"",
		"	set victim's attack speed attribute to 2"})
@Since("INSERT VERSION")
public class ExprEntityAttribute extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprEntityAttribute.class, Number.class, ExpressionType.COMBINED,
				"%attributetype% [value] of %entities%",
				"%entities%'[s] %attributetype% [value]");
	}
	
	@Nullable
	private Expression<Attribute> attributes = null;
	
	@Nullable
	private Expression<Entity> entities = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attributes = (Expression<Attribute>) exprs[matchedPattern];
		entities = (Expression<Entity>) exprs[matchedPattern ^ 1];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Number[] get(Event e) {
		Attribute a = attributes.getSingle(e);
		Entity[] ea = entities.getArray(e);
		Number[] arr = new Number[ea.length];
		for (int i = 0; i < ea.length; i++) {
			arr[i] = ((Attributable) ea[i]).getAttribute(a).getValue();
		}
		return arr;
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
		for (Entity entity : entities.getArray(e)) {
			if (mode == ChangeMode.SET) {
				((Attributable) entity).getAttribute(a).setBaseValue(d);
			}
		}
		return;
	}

	@SuppressWarnings("null")
	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "entity attribute";
	}
	
}
