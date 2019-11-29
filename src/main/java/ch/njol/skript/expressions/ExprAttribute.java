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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
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
@Description({"Expression that represents a particular attribute type, such as GENERIC_ATTACK_SPEED.",
		"Note that this merely represents the attribute type, and does not contain any numerical values."})
@Examples("attribute \"GENERIC_ATTACK_SPEED\"")
@Since("INSERT VERSION")
public class ExprAttribute extends SimpleExpression<Attribute>{
	
    static {
    	Skript.registerExpression(ExprAttribute.class, Attribute.class, ExpressionType.SIMPLE, "attribute %string%");
    }

	@SuppressWarnings("null")
	private Expression<String> exprStrings;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		exprStrings = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Attribute[] get(Event e) {
		try {
			return CollectionUtils.array(Attribute.valueOf(exprStrings.getSingle(e)));
		} catch (IllegalArgumentException ex) {}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Attribute> getReturnType() {
		return Attribute.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "attribute " + exprStrings.toString(e, debug);
	}
	
}
