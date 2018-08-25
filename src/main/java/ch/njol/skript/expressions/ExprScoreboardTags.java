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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Scoreboard Tags")
@Description({"Scoreboard tags are a simple list of single-word texts stored directly in the data of an entity.",
		"So this is a Minecraft related thing, not Bukkit. For more info, <a href='https://minecraft.gamepedia.com/Scoreboard#Tags'>visit Minecraft Wiki</a>.",
		"This is changeable and valid for any type of entity. " +
		"Also you can check whether an entity has specified tags using the <a href='conditions.html#CondHasScoreboardTag'>Has Scoreboard Tag</a> condition.",
		"",
		"Requires Minecraft 1.11+ (actually added in 1.9 to the game, but added in 1.11 to Spigot)."})
@Examples({"on spawn of a monster:",
        "    if the spawn reason is mob spawner:",
        "        add \"spawned by a spawner\" to the scoreboard tags of event-entity",
        "",
        "on death of a monster:",
        "    if the attacker is a player:",
        "        if the victim doesn't have the scoreboard tag \"spawned by a spawner\":",
        "            add 1$ to attacker's balance"})
@Since("INSERT VERSION")
public class ExprScoreboardTags extends SimpleExpression<String> {

	static {
		if (Skript.isRunningMinecraft(1, 11))
			Skript.registerExpression(ExprScoreboardTags.class, String.class, ExpressionType.PROPERTY,
					"[(all [[of] the]|the)] scoreboard tags of %entities%",
					"%entities%'[s] scoreboard tags");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		return Stream.of(entities.getArray(e))
				.map(Entity::getScoreboardTags)
				.flatMap(Set::stream)
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String[].class);
		}
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		List<String> values = null;
		if (delta != null)
			values = (List) Arrays.asList(delta);
		for (Entity entity : entities.getArray(e)) {
			Set<String> tags = entity.getScoreboardTags();
			switch (mode) {
				case SET:
					assert values != null;
					tags.clear();
					tags.addAll(values);
					break;
				case ADD:
					assert values != null;
					tags.addAll(values);
					break;
				case REMOVE:
					assert values != null;
					tags.removeAll(values);
					break;
				case DELETE:
				case RESET:
					tags.clear();
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the scoreboard tags of " + entities.toString(e, debug);
	}

}
