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

@Name("Attribute")
@Description("The attribute of an entity.")
@Examples({"on damage of player:",
		"	send \"You are wounded!\"",
		"	set victim's attribute \"GENERIC_ATTACK_SPEED\" to 2"})
@Since("INSERT VERSION")
public class ExprAttribute extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprAttribute.class, Number.class, ExpressionType.PROPERTY,
				"attribute %string% of %entities%",
				"%entities%'s attribute %string%");
	}
	
	@Nullable
	private Expression<String> exprString = null;
	
	@Nullable
	private Expression<Entity> exprEntity = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 0) {
			exprString = (Expression<String>) exprs[0];
			exprEntity = (Expression<Entity>) exprs[1];
		} else {
			exprString = (Expression<String>) exprs[1];
			exprEntity = (Expression<Entity>) exprs[0];
		}
		return true;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Number[] get(final Event e) {
		try {
			Attribute a = Attribute.valueOf(exprString.getSingle(e));
			Entity[] entities = exprEntity.getAll(e);
			Number[] arr = new Number[entities.length];
			for(int i = 0; i < entities.length; i++) {
				arr[i] = ((Attributable) entities[i]).getAttribute(a).getValue();
			}
			return arr;
		} catch (IllegalArgumentException | NullPointerException ex) {}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		try {
			Attribute a = Attribute.valueOf(exprString.getSingle(e));
			double d = ((Number) delta[0]).doubleValue();
			for(Entity entity : exprEntity.getAll(e)) {
				if (mode == ChangeMode.SET) {
					((Attributable) entity).getAttribute(a).setBaseValue(d);
				}
			}
		} catch (IllegalArgumentException | NullPointerException ex) {}
		return;
	}

	@SuppressWarnings("null")
	@Override
	public boolean isSingle() {
		return exprEntity.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "attribute";
	}
	
}
