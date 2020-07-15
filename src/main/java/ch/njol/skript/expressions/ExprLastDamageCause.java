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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author bensku
 *
 */
@Name("Last Damage Cause")
@Description("Cause of last damage done to an entity")
@Examples({"set last damage cause of event-entity to fire tick"})
@Since("2.2-Fixes-V10")
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, DamageCause>{
	
	static {
		register(ExprLastDamageCause.class, DamageCause.class, "last damage (cause|reason|type)", "livingentities");
	}
	
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<LivingEntity>) vars[0]);
		return true;
	}
	
	@Override
	protected DamageCause[] get(final Event e, final LivingEntity[] source) {
		return get(source, new Getter<DamageCause, LivingEntity>() {
			@SuppressWarnings("null")
			@Override
			public DamageCause get(final LivingEntity entity) {
				EntityDamageEvent dmgEvt = entity.getLastDamageCause();
				if (dmgEvt == null) return DamageCause.CUSTOM;
				return dmgEvt.getCause();
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the damage cause " + getExpr().toString(e, debug);
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(DamageCause.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		DamageCause d = delta == null ? DamageCause.CUSTOM : (DamageCause) delta[0];
		assert d != null;
		switch (mode) {
			case DELETE:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, DamageCause.CUSTOM);
				}
				break;
			case SET:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, d);
				}
				break;
			case RESET:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, DamageCause.CUSTOM); // Reset damage cause? Umm, maybe it is custom.
				}
				break;
			case REMOVE_ALL:
				assert false;
				break;
				//$CASES-OMITTED$
			default:
				break;
		}
	}
	
	@Override
	public Class<DamageCause> getReturnType() {
		return DamageCause.class;
	}
}
