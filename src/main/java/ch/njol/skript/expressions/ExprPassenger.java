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
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Passenger")
@Description({"The passenger of a vehicle, or the rider of a mob.",
		"See also: <a href='#ExprVehicle'>vehicle</a>"})
@Examples({"passenger of the minecart is a creeper or a cow",
		"the saddled pig's passenger is a player"})
@Since("2.0")
public class ExprPassenger extends SimplePropertyExpression<Entity, Entity> { // REMIND create 'vehicle' and 'passenger' expressions for vehicle enter/exit events?
	static {
		register(ExprPassenger.class, Entity.class, "passenger[s]", "entities");
	}
	
	@Override
	protected Entity[] get(final Event e, final Entity[] source) {
		return get(source, new Converter<Entity, Entity>() {
			@Override
			@Nullable
			public Entity convert(final Entity v) {
				if (getTime() >= 0 && e instanceof VehicleEnterEvent && v.equals(((VehicleEnterEvent) e).getVehicle()) && !Delay.isDelayed(e)) {
					return ((VehicleEnterEvent) e).getEntered();
				}
				if (getTime() >= 0 && e instanceof VehicleExitEvent && v.equals(((VehicleExitEvent) e).getVehicle()) && !Delay.isDelayed(e)) {
					return ((VehicleExitEvent) e).getExited();
				}
				return v.getPassenger();
			}
		});
	}
	
	@Override
	@Nullable
	public Entity convert(final Entity e) {
		assert false;
		return e.getPassenger();
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[] {Entity.class, EntityData.class};
		}
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			final Entity[] vs = getExpr().getArray(e);
			if (vs.length == 0)
				return;
			final Object o = delta[0];
			if (o instanceof Entity) {
				((Entity) o).leaveVehicle();
				final Entity v = CollectionUtils.getRandom(vs);
				assert v != null;
				v.eject();
				v.setPassenger((Entity) o);
			} else if (o instanceof EntityData) {
				for (final Entity v : vs) {
					@SuppressWarnings("null")
					final Entity p = ((EntityData<?>) o).spawn(v.getLocation());
					if (p == null)
						continue;
					v.setPassenger(p);
				}
			} else {
				assert false;
			}
		} else {
			super.change(e, delta, mode);
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "passenger";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}
	
}
