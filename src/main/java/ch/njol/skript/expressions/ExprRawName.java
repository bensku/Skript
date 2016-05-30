/*
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
 */

package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprRawName extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprRawName.class, String.class, ExpressionType.SIMPLE, "(raw|minecraft|vanilla) name of %itemtypes%");
	}
	
	@Nullable
	private Expression<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.types = (Expression<ItemType>) exprs[0];
		return true;
	}
	

	@Override
	@Nullable
	protected String[] get(final Event e) {
		if (types == null) return null;
		
		ItemType[] items = types.getAll(e);
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < items.length; i++) {
			names.addAll(items[i].getRawNames());
		}
		
		return names.toArray(new String[names.size()]);
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
	public String toString(final @Nullable Event e, final boolean debug) {
		String[] strs = get(e);
		if (strs == null) return "";
		return Arrays.toString(strs);
	}
}
