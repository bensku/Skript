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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffLoadServerIcon;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.eclipse.jdt.annotation.Nullable;

@Name("Last Loaded Server Icon")
@Description("Returns the last loaded server icon with the <a href='effects.html#EffLoadServerIcon'>load server icon</a> effect.")
@Examples("set {server-icon} to the last loaded server icon")
@Since("INSERT VERSION")
public class ExprLastLoadedServerIcon extends SimpleExpression<CachedServerIcon> {

	static {
		Skript.registerExpression(ExprLastLoadedServerIcon.class, CachedServerIcon.class, ExpressionType.SIMPLE, "[the] [last[ly]] (loaded|cached) server icon");
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	public CachedServerIcon[] get(Event e) {
		return CollectionUtils.array(EffLoadServerIcon.lastLoaded);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends CachedServerIcon> getReturnType() {
		return CachedServerIcon.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the last loaded server icon";
	}

}
