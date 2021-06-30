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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public final class EvtExplode extends SkriptEvent {

	private final static int ENTITY = 1, BLOCK = 2, ANY = ENTITY | BLOCK;

	static {
		Class<? extends Event>[] eventTypes = CollectionUtils.array(EntityExplodeEvent.class, BlockExplodeEvent.class);

		Skript.registerEvent("entity/block explode", EvtExplode.class, eventTypes, "[(1¦entity|2¦block)] explo(d(e|ing)|sion)")
				.description(
					"Called when an entity (a primed TNT or a creeper) explodes " +
					"OR Called when a block explodes. (Also triggered by <a href='effects.html#EffExplosion'>create explosion effect</a>)" +
					" " +
					"If explosion type is specified only that type of explosion will trigger the event.")
				.examples("on explode:",
						"\tbroadcast \"a(n) %explosion type% just exploded.\"",
						"on block explode:",

						"\tbroadcast \"A block just exploded\"",
						"on entity explode:",

						"\tbroadcast \"An entity just exploded\"")
				.since("1.0, INSERT VERSION (block explode)");
	}
	
	@Nullable
	private int type;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		type = parser.mark == 0 ? ANY : parser.mark;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(final Event e) {
		if (type == ENTITY)
			return e instanceof EntityExplodeEvent ? true : false;

		if (type == BLOCK)
			return e instanceof BlockExplodeEvent ? true : false;

		else // Any
			return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (type == ENTITY ? "entity" : type == BLOCK ? "block" : "any") + (" explode");
	}
	
}
