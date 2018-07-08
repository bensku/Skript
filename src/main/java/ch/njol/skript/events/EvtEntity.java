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
package ch.njol.skript.events;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public final class EvtEntity extends SkriptEvent {
	static {
		Skript.registerEvent("Death", EvtEntity.class, EntityDeathEvent.class, "death [of %entitydatas%]")
				.description("Called when a living entity (including players) dies.")
				.examples("on death",
						"on death of player",
						"on death of a wither or ender dragon:",
						"	broadcast \"A %entity% has been slain in %world%!\"")
				.since("1.0");
		Skript.registerEvent("Spawn", EvtEntity.class, CreatureSpawnEvent.class, "spawn[ing] [of %entitydatas%]")
				.description("Called when an creature spawns.")
				.examples("on spawn of a zombie",
						"on spawn of an ender dragon:",
						"	broadcast \"A dragon has been sighted in %world%!\"")
				.since("1.0");
	}
	
	@Nullable
	private EntityData<?>[] types;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = args[0] == null ? null : ((Literal<EntityData<?>>) args[0]).getAll();
		if (types != null) {
			if (StringUtils.startsWithIgnoreCase(parser.expr, "spawn")) {
				for (final EntityData<?> d : types) {
					if (!LivingEntity.class.isAssignableFrom(d.getType()) || HumanEntity.class.isAssignableFrom(d.getType())) {
						Skript.error("The spawn event only works for living entities (excluding humans)", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				}
			} else {
				for (final EntityData<?> d : types) {
					if (!LivingEntity.class.isAssignableFrom(d.getType())) {
						Skript.error("The death event only works for living entities", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final Entity en = e instanceof EntityDeathEvent ? ((EntityDeathEvent) e).getEntity() : ((CreatureSpawnEvent) e).getEntity();
		for (final EntityData<?> d : types) {
			if (d.isInstance(en))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "death/spawn" + (types != null ? " of " + Classes.toString(types, false) : "");
	}
	
}
