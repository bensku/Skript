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
package ch.njol.skript.conditions;

import ch.njol.skript.ServerPlatform;
import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;


@Name("Is in Rain/Lava/Water/BubbleColumn")
@Description("Checks whether an entity is in Rain/Lava/Water/BubbleColumn")
@Examples({"player is in rain",
		"player is in water",
		"player is in lava",
		"player is in bubble column",
		"player is in water or rain",
		"player is in water or bubble column",
		"player is in water or rain or bubble column"})
@RequiredPlugins("Minecraft 1.16+ / Paper 1.16+ for rain, lava and bubble column")
@Since("INSERT VERSION")
public class CondEntityIsIn extends Condition {
	
	static {
		if (Skript.isRunningMinecraft(1, 16)) {
			PropertyCondition.register(CondEntityIsIn.class, "in (1¦water)", "entities");
			if (Skript.getServerPlatform() == ServerPlatform.BUKKIT_PAPER)
				PropertyCondition.register(CondEntityIsIn.class, "in (2¦lava|3¦bubble[ ]column|4¦rain|5¦water or rain|6¦water or bubble[ ]column|7¦water or rain or bubble[ ]column)", "entities");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private int mark;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern == 1);
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		switch (mark) {
			case 1:
				return entities.check(e, Entity::isInWater, isNegated());
			case 2:
				return entities.check(e, Entity::isInLava, isNegated());
			case 3:
				return entities.check(e, Entity::isInBubbleColumn, isNegated());
			case 4:
				return entities.check(e, Entity::isInRain, isNegated());
			case 5:
				return entities.check(e, Entity::isInWaterOrRain, isNegated());
			case 6:
				return entities.check(e, Entity::isInWaterOrBubbleColumn, isNegated());
			case 7:
				return entities.check(e, Entity::isInWaterOrRainOrBubbleColumn, isNegated());
			default:
				return false;
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is in rain/lava/water/bubblecolumn";
	}
	
}
