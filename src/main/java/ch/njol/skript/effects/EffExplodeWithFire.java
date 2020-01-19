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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Explode With Fire")
@Description("Sets if an entity will explode with fire. Use \"make the explosion fiery\" in explosion prime events.")
@Examples({"on explosion prime:", 
	"\tmake the explosion fiery"})
@Since("INSERT VERSION")
public class EffExplodeWithFire extends Effect {

	static {
		Skript.registerEffect(EffExplodeWithFire.class,
				"make %entities% [(1¦not)] explode with fire",
				"make %entities% [(1¦not)] cause (a fiery|an incendiary explosion)",
				"make the [event(-| )]explosion [(1¦not)] fiery");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean causeFire;

	private boolean isEvent;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		isEvent = matchedPattern == 2;
		if (isEvent) {
			if (!ScriptLoader.isCurrentEvent(ExplosionPrimeEvent.class)) {
				Skript.error("Making the explosion fiery is only usable in explosion prime events", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		if (matchedPattern < 2)
			entities = (Expression<Entity>) exprs[0];
		causeFire = parseResult.mark != 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (isEvent) {
			((ExplosionPrimeEvent) e).setFire(causeFire);
		} else {
			for (Entity entity : entities.getArray(e)) {
				if (entity instanceof Explosive)
					((Explosive) entity).setIsIncendiary(causeFire);
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isEvent)
			return "make the explosion " + (causeFire == true ? "" : "not") + " fiery (ExplosionPrimeEvent)";
		return "make " + entities.toString(e, debug) + (causeFire == true ? "" : " not") + " explode with fire";
	}
}
