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

@Name("Is in Rain/Lava/Water/BubbleColumn")
@Description("Checks whether an entity is in Rain/Lava/Water/BubbleColumn")
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
			patterns.append("1¦water|");
		if (Skript.methodExists(Entity.class, "isInLava"))
			patterns.append("2¦lava|");
		if (Skript.methodExists(Entity.class, "isInBubbleColumn"))
			patterns.append("3¦bubble[ ]column|");
		if (Skript.methodExists(Entity.class, "isInRain"))
			patterns.append("4¦rain|");
		if (Skript.methodExists(Entity.class, "isInWaterOrRain"))
			patterns.append("5¦water or rain|");
		if (Skript.methodExists(Entity.class, "isInWaterOrBubbleColumn"))
			patterns.append("6¦water or bubble[ ]column|");
		if (Skript.methodExists(Entity.class, "isInWaterOrRainOrBubbleColumn"))
			patterns.append("7¦water or rain or bubble[ ]column");

		if (patterns.toString().endsWith("|")) // Remove last empty '|' if exists
			patterns.deleteCharAt(patterns.length() - 1);

		register(CondEntityIsIn.class, PropertyType.BE, "in (" + patterns + ")", "entities");
	}

	static final int IN_WATER = 1;
	static final int IN_LAVA = 2;
	static final int IN_BUBBLE_COLUMN = 3;
	static final int IN_RAIN = 4;
	static final int IN_WATER_OR_RAIN = 5;
	static final int IN_WATER_OR_BUBBLE_COLUMN = 6;
	static final int IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN = 7;

	private int mark;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Entity>) exprs[0]);
		setNegated(matchedPattern == 1);
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	public boolean check(Entity entity) {
		switch (mark) {
			case IN_WATER:
				return entity.isInWater();
			case IN_LAVA:
				return entity.isInLava();
			case IN_BUBBLE_COLUMN:
				return entity.isInBubbleColumn();
			case IN_RAIN:
				return entity.isInRain();
			case IN_WATER_OR_RAIN:
				return entity.isInWaterOrRain();
			case IN_WATER_OR_BUBBLE_COLUMN:
				return entity.isInWaterOrBubbleColumn();
			case IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN:
				return entity.isInWaterOrRainOrBubbleColumn();
			default:
				return false;
		}
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
