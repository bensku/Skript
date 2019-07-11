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
package ch.njol.skript.effects;

// Remove lot of imports
import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.GameMode;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Remove") // Change stuff below here
@Description({"Removes an entity: this isn't the same as killing the entity. This effect doesn't have the effects of dropping items, playing particles and doesn't play the death effect.",
		"Note: This effect cannot be used on players."})
@Examples({"remove last spawned entity",
		"delete entity of target of player",
		"remove all entities"})
@Since("2.4")
public class EffRemoveEntity extends Effect {

	static {
		Skript.registerEffect(EffRemoveEntity.class,
				"remove [entit(y|ies) of] %entities%",
				"delete entit(y|ies) of %entities%");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (Entity entity : entities.getArray(e)) {

			if (entity instanceof EnderDragonPart) {
				entity = ((EnderDragonPart) entity).getParent();
			}

			if (!(entity instanceof Player)) {
				entity.remove();
			}

		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "remove" + entities.toString(e, debug);
	}

}
