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
package ch.njol.skript.conditions;

import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Projectile Can Bounce")
@Description("Whether or not a projectile can bounce.")
@Examples({"on shoot:",
	"\tsend \"Boing!\" to all players if projectile can bounce"})
@Since("INSERT VERSION")
public class CondProjectileCanBounce extends Condition {
	
	static {
		Skript.registerCondition(CondProjectileCanBounce.class, "%projectiles% (1¦can|2¦can( not|'t)) bounce");
	}
	
	@SuppressWarnings("null")
	Expression<Projectile> projectiles;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		projectiles = (Expression<Projectile>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return (projectiles.getAnd() ? projectiles.stream(e).allMatch(projectile -> projectile.doesBounce()) : projectiles.stream(e).anyMatch(projectile -> projectile.doesBounce())) == isNegated();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return projectiles.toString(e, debug) + " can bounce";
	}
	
}
