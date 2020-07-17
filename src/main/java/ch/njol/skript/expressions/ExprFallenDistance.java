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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

public class ExprFallenDistance extends SimplePropertyExpression<Entity, Number> {
	static {
		register(ExprFallenDistance.class, Number.class, "fall(en|ed|) distance", "entity");
	}
	@Override
	protected String getPropertyName() {
		return "fall(en|ed|) distance";
	}
	
	@Nullable
	@Override
	public Number convert(Entity entity) {
		return entity.getFallDistance();
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if(delta != null){
			Entity entity = getExpr().getSingle(e);
			if(entity == null) return;
			Float number = ((Number) delta[0]).floatValue();
			switch (mode){
				case ADD:
					entity.setFallDistance(entity.getFallDistance()+number);
					break;
				case SET:
					entity.setFallDistance(number);break;
				case REMOVE:
					entity.setFallDistance(entity.getFallDistance()-number);
					break;
				default:
					entity.setFallDistance(0);
			}
		}
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}
}
