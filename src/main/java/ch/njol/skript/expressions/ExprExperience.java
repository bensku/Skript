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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Experience")
@Description("How much experience was spawned in an <a href='../events.html#experience_spawn'>experience spawn</a> event. Can be changed.")
@Examples({"on experience spawn:",
		"	add 5 to the spawned experience"})
@Since("2.1")
@Events("experience spawn")
public class ExprExperience extends SimpleExpression<Experience> {
	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE, "[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(ExperienceSpawnEvent.class)) {
			Skript.error("The experience expression can only be used in experience spawn events");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Experience[] get(final Event e) {
		if (!(e instanceof ExperienceSpawnEvent))
			return new Experience[0];
		return new Experience[] {new Experience(((ExperienceSpawnEvent) e).getSpawnedXP())};
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				return new Class[] {Experience[].class, Number[].class};
			case SET:
				return new Class[] {Experience.class, Number.class};
			case RESET:
				return null;
		}
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (!(e instanceof ExperienceSpawnEvent))
			return;
		if (delta == null) {
			((ExperienceSpawnEvent) e).setSpawnedXP(0);
			return;
		}
		double d = 0;
		for (final Object o : delta) {
			final double v = o instanceof Experience ? ((Experience) o).getXP() : ((Number) o).doubleValue();
			switch (mode) {
				case ADD:
				case SET:
					d += v;
					break;
				case REMOVE:
				case REMOVE_ALL:
					d -= v;
					break;
				case RESET:
				case DELETE:
					assert false;
					break;
			}
		}
		((ExperienceSpawnEvent) e).setSpawnedXP(Math.max(0, (int) Math.round(((ExperienceSpawnEvent) e).getSpawnedXP() + d)));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Experience> getReturnType() {
		return Experience.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the experience";
	}
	
}
