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

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is In")
@Description("Checks whether an entity is in rain, lava, water or a bubble column.")
@Examples({"player is in rain",
		"player is in water",
		"player is in lava",
		"player is in bubble column",
		"player is in water or rain",
		"player is in water or bubble column",
		"player is in water or rain or bubble column"})
@RequiredPlugins("Minecraft 1.16+, Paper 1.16+ (rain, lava and bubble column)")
@Since("INSERT VERSION")
public class CondEntityIsIn extends PropertyCondition<Entity> {
	
	static {
		StringBuilder patterns = new StringBuilder("");
		if (Skript.methodExists(Entity.class, "isInWater"))
			patterns.append("1¦water");
		if (Skript.methodExists(Entity.class, "isInLava")) // All added at the same time + isInWater
			patterns.append("|2¦lava|3¦bubble[ ]column|4¦rain|5¦water or rain|6¦water or bubble[ ]column|7¦water or rain or bubble[ ]column");

		register(CondEntityIsIn.class, PropertyType.BE, "in (" + patterns + ")", "entities");
	}

	static final int
			IN_WATER = 1,
			IN_LAVA = 2,
			IN_BUBBLE_COLUMN = 3,
			IN_RAIN = 4, IN_WATER_OR_RAIN = 5,
			IN_WATER_OR_BUBBLE_COLUMN = 6,
			IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN = 7;

	private int mark;

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Entity>) exprs[0]);
		setNegated(matchedPattern == 1);
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	public boolean check(Entity entity) {
		return switch (mark) {
			case IN_WATER -> entity.isInWater();
			case IN_LAVA -> entity.isInLava();
			case IN_BUBBLE_COLUMN -> entity.isInBubbleColumn();
			case IN_RAIN -> entity.isInRain();
			case IN_WATER_OR_RAIN -> entity.isInWaterOrRain();
			case IN_WATER_OR_BUBBLE_COLUMN -> entity.isInWaterOrBubbleColumn();
			case IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN -> entity.isInWaterOrRainOrBubbleColumn();
			default -> false;
		};
	}

	@Override
	protected String getPropertyName() {
		return getCondName();
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is in " + getCondName();
	}

	private String getCondName() {
		switch (mark) {
			case IN_WATER:
				return "water";
			case IN_LAVA:
				return "lava";
			case IN_BUBBLE_COLUMN:
				return "bubble column";
			case IN_RAIN:
				return "rain";
			case IN_WATER_OR_RAIN:
				return "water or rain";
			case IN_WATER_OR_BUBBLE_COLUMN:
				return "water or bubble column";
			case IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN:
				return "water or rain or bubble column";
			default:
				return "";
		}
	}
}
