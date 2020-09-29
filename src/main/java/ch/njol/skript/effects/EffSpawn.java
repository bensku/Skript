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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Peter Güttinger
 */
@Name("Spawn")
@Description("Spawn a creature.")
@Examples({"spawn 3 creepers at the targeted block",
		"spawn a ghast 5 meters above the player"})
@Since("1.0")
public class EffSpawn extends Effect {
	static {
		Skript.registerEffect(EffSpawn.class,
				"(spawn|summon) %entitytypes% [%directions% %locations%]",
				"(spawn|summon) %number% of %entitytypes% [%directions% %locations%]");
	}
	
	@SuppressWarnings("null")
	private Expression<Location> locations;
	@SuppressWarnings("null")
	private Expression<EntityType> types;
	@Nullable
	private Expression<Number> amount;
	
	@Nullable
	public static Entity lastSpawned = null;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		amount = matchedPattern == 0 ? null : (Expression<Number>) (exprs[0]);
		types = (Expression<EntityType>) exprs[matchedPattern];
		locations = Direction.combine((Expression<? extends Direction>) exprs[1 + matchedPattern], (Expression<? extends Location>) exprs[2 + matchedPattern]);
		return true;
	}
	
	@Override
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void execute(final Event e) {
		lastSpawned = null;
		final Number a = amount != null ? amount.getSingle(e) : 1;
		if (a == null)
			return;
		final EntityType[] ts = types.getArray(e);
		for (final Location l : locations.getArray(e)) {
			assert l != null : locations;
			for (final EntityType type : ts) {
				for (int i = 0; i < a.doubleValue() * type.getAmount(); i++) {
					lastSpawned = type.data.spawn(l);
				}
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "spawn " + (amount != null ? amount.toString(e, debug) + " " : "") + types.toString(e, debug) + " " + locations.toString(e, debug);
	}
	
}
