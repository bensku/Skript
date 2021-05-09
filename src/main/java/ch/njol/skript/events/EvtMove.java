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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtMove extends SkriptEvent {

	private static final boolean HAS_ENTITY_MOVE = Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent");

	// There was a point the event existed but this method did not
	private static final boolean HAS_POS_METHOD = HAS_ENTITY_MOVE &&
		Skript.methodExists(EntityMoveEvent.class, "hasChangedPosition", boolean.class);

	static {
		Class<? extends Event>[] events;
		if (HAS_ENTITY_MOVE)
			events = CollectionUtils.array(PlayerMoveEvent.class, EntityMoveEvent.class);
		else
			events = CollectionUtils.array(PlayerMoveEvent.class);

		Skript.registerEvent("Move", EvtMove.class, events,
			"(0¦player|1¦%entitydatas%) move")
			.description("Called when a player or entity moves.",
				"NOTE: Entity move event will only be called when the entity moves position, not orientation (ie: looking around).",
				"NOTE: These events can be performance heavy as they are called quite often.",
				"If you use these events, and later remove them, a server restart is recommended to clear registered events from Skript.")
			.examples("on player move:",
				"\tif player does not have permission \"player.can.move\":",
				"\t\tcancel event",
				"on skeleton move:",
				"\tif event-entity is not in world \"world\":",
				"\t\tkill event-entity")
			.requiredPlugins("Paper 1.16.5+ (for entity move)")
			.since("INSERT VERSION");
	}

	@Nullable
	private EntityData<?>[] types = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		if (HAS_ENTITY_MOVE && parseResult.mark == 1) {
			types = ((Literal<EntityData<?>>) args[0]).getAll();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null && event instanceof PlayerMoveEvent) {
			return true;
		} else if (types != null && HAS_ENTITY_MOVE && event instanceof EntityMoveEvent) {
			EntityMoveEvent entityEvent = (EntityMoveEvent) event;
			LivingEntity entity = entityEvent.getEntity();
			for (EntityData<?> type : types) {
				if (type.isInstance(entity)) {
					if (!HAS_POS_METHOD) {
						return true;
					} else if (((EntityMoveEvent) event).hasChangedPosition()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (HAS_ENTITY_MOVE && types != null ? types.toString() : "player") + " move";
	}
}
