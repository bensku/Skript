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

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Target")
@Description("For players this is the entity at the crosshair, while for mobs and experience orbs it represents the entity they are attacking/following (if any).")
@Examples({"on entity target:",
			"\tentity's target is a player",
			"\tsend \"You're being followed by an %entity%!\" to target of entity"})
@Since("<i>unknown</i> (before 2.1)")
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {
	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.PROPERTY,
				"[the] target[[ed] %-*entitydata%] [of %livingentities%]",
				"%livingentities%'[s] target[[ed] %-*entitydata%]");
	}
	
	private static final boolean PATHFIND_EXISTS = Skript.classExists("com.destroystokyo.paper.event.entity.EntityPathfindEvent");
	private boolean PATHFIND_TARGET = false;
	
	@Nullable
	EntityData<?> type;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (PATHFIND_EXISTS && ScriptLoader.isCurrentEvent(EntityPathfindEvent.class))
			PATHFIND_TARGET = true;
		type = exprs[matchedPattern] == null ? null : (EntityData<?>) exprs[matchedPattern].getSingle(null);
		setExpr((Expression<? extends LivingEntity>) exprs[1 - matchedPattern]);
		return true;
	}
	
	@Override
	protected Entity[] get(final Event e, final LivingEntity[] source) {
		if (PATHFIND_TARGET)
			return new Entity[] {((EntityPathfindEvent) e).getTargetEntity()};
		return get(source, new Converter<LivingEntity, Entity>() {
			@Override
			@Nullable
			public Entity convert(final LivingEntity en) {
				if (getTime() >= 0 && e instanceof EntityTargetEvent && en.equals(((EntityTargetEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					final Entity t = ((EntityTargetEvent) e).getTarget();
					if (t == null || type != null && !type.isInstance(t))
						return null;
					return t;
				}
				return Utils.getTarget(en, type);
			}
		});
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type != null ? type.getType() : Entity.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return "the target" + (type == null ? "" : "ed " + type) + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
		return Classes.getDebugMessage(getAll(e));
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, EntityTargetEvent.class, getExpr());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(LivingEntity.class);
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			final LivingEntity target = delta == null ? null : (LivingEntity) delta[0];
			for (final LivingEntity entity : getExpr().getArray(e)) {
				if (getTime() >= 0 && !Delay.isDelayed(e)) {
					if (e instanceof EntityTargetEvent && entity.equals(((EntityTargetEvent) e).getEntity())) {
						((EntityTargetEvent) e).setTarget(target);
					} else if (PATHFIND_TARGET && entity instanceof Mob){
						EntityPathfindEvent pathfindEvent = (EntityPathfindEvent) e;
						if (entity.equals(((EntityPathfindEvent) e).getEntity())){
							pathfindEvent.setCancelled(true);
							if (target != null)
								((Mob) entity).getPathfinder().moveTo(target);
						}
					}
				} else {
					if (entity instanceof Creature)
						((Creature) entity).setTarget(target);
				}
			}
		} else {
			super.change(e, delta, mode);
		}
	}
	
}
