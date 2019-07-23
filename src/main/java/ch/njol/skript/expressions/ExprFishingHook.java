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

import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Fishing Hook")
@Description("The <a href='../classes.html#entity'>fishing hook</a> involved in a player fishing event.")
@Examples({"on fishing:", "\tteleport player to fishing hook"})
@Since("2.4")
public class ExprFishingHook extends EventValueExpression<FishHook> {

	static {
		Skript.registerExpression(ExprFishingHook.class, FishHook.class, ExpressionType.SIMPLE, "[the] [fishing] hook");
	}

	public ExprFishingHook() {
		super(FishHook.class);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the fishing hook";
	}

}
