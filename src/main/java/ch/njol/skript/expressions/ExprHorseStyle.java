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
import org.bukkit.entity.Horse.Style;
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

@Name("Horse Style")
@Description("The <a href='classes.html#horsestyle'>style</a> of a horse.")
@Examples("set all horses' style to white dots")
@Since("INSERT VERSION")
public class ExprHorseStyle extends SimplePropertyExpression<LivingEntity, Style> {
	
	static {
		register(ExprHorseStyle.class, Style.class, "style", "livingentities");
	}
	
	@Nullable
	@Override
	public Style convert(LivingEntity horse) {
		return horse instanceof Horse ? ((Horse) horse).getStyle() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Style.class);
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			for (LivingEntity horse : getExpr().getArray(e)) {
				if (horse instanceof Horse)
					((Horse) horse).setStyle((Style) delta[0]);
			}
		}
	}
	
	@Override
	public Class<? extends Style> getReturnType() {
		return Style.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "style";
	}
	
}
