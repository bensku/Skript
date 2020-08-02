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
package ch.njol.skript.expressions;

import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Horse Color")
@Description("The <a href='classes.html#horsecolor'>color</a> of a horse.")
@Examples("set all horses' color to brown")
@Since("INSERT VERSION")
public class ExprHorseColor extends SimplePropertyExpression<LivingEntity, Horse.Color> {
	
	static {
		register(ExprHorseColor.class, Horse.Color.class, "colo[u]r", "livingentities");
	}
	
	@Nullable
	@Override
	public Horse.Color convert(LivingEntity horse) {
		return horse instanceof Horse ? ((Horse) horse).getColor() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Horse.Color.class);
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			for (LivingEntity horse : getExpr().getArray(e)) {
				if (horse instanceof Horse)
					((Horse) horse).setColor((Horse.Color) delta[0]);
			}
		}
	}
	
	@Override
	public Class<? extends Horse.Color> getReturnType() {
		return Horse.Color.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "color";
	}
	
}
