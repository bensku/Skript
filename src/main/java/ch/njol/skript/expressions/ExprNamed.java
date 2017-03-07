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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Named Item")
@Description("Directly names an item, useful for defining a named item in a script. " +
		"If you want to (re)name existing items you can either use this expression or use <code>set <a href='#ExprName'>name of &lt;item&gt;</a> to &lt;text&gt;</code>.")
@Examples({"give a diamond sword of sharpness 100 named \"<gold>Excalibur\" to the player",
		"set tool of player to the player's tool named \"<gold>Wand\"",
		"set the name of the player's tool to \"<gold>Wand\""})
@Since("2.0")
public class ExprNamed extends PropertyExpression<ItemType, ItemType> {
	static {
		Skript.registerExpression(ExprNamed.class, ItemType.class, ExpressionType.PROPERTY, "%itemtypes% (named|with name[s]) %string%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> name;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!Skript.isRunningMinecraft(1, 4, 5)) {
			Skript.error("Item names are only available in Minecraft 1.4.5+");
			return false;
		}
		setExpr((Expression<? extends ItemType>) exprs[0]);
		name = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	protected ItemType[] get(final Event e, final ItemType[] source) {
		final String n = name.getSingle(e);
		if (n == null)
			return new ItemType[0];
		final ItemType[] r = source.clone();
		for (int i = 0; i < r.length; i++) {
			r[i] = source[i].clone();
			ItemMeta m = (ItemMeta) r[i].getItemMeta();
			if (m == null)
				m = Bukkit.getItemFactory().getItemMeta(Material.STONE); // AIR results in null
			assert m != null : r[i];
			m.setDisplayName(n);
			r[i].setItemMeta(m);
		}
		return r;
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return getExpr().toString(e, debug) + " named " + name;
	}
	
}
