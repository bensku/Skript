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

import java.util.Arrays;
import java.util.List;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Scoreboard Tag")
@Description("Checks whether the given entities has the given <a href='expressions.html#ExprScoreboardTags'>scoreboard tags</a>.")
@Since("INSERT VERSION")
public class CondHasScoreboardTag extends Condition {

	static {
		if (Skript.isRunningMinecraft(1, 11))
			Skript.registerCondition(CondHasScoreboardTag.class,
					"%entities% ha(s|ve) [the] score[ ]board tag[s] %strings%",
					"%entities% (do[es]n't|don't|do[es] not) have [the] score[ ]board tag[s] %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	@SuppressWarnings("null")
	private Expression<String> tags;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		entities = (Expression<Entity>) exprs[0];
		tags = (Expression<String>) exprs[1];
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean check(Event e) {
		List<String> tagsList = (List) Arrays.asList(tags.getArray(e));
		return entities.check(e, entity -> entity.getScoreboardTags().containsAll(tagsList), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return entities.toString(e, debug) + (isNegated() ? " doesn't have " : " has ") + "the scoreboard tags " + tags.toString(e, debug);
	}

}