/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Named Item/Inventory")
@Description("Directly names an item/inventory, useful for defining a named item/inventory in a script. " +
		"If you want to (re)name existing items/inventories you can either use this expression or use <code>set <a href='#ExprName'>name of &lt;item/inventory&gt;</a> to &lt;text&gt;</code>.")
@Examples({"give a diamond sword of sharpness 100 named \"<gold>Excalibur\" to the player",
		"set tool of player to the player's tool named \"<gold>Wand\"",
		"set the name of the player's tool to \"<gold>Wand\"",
		"open hopper inventory named \"Magic Hopper\" to player"})
@Since("2.0, 2.2-dev34 (inventories)")
@SuppressWarnings("null")
public class ExprNamed<T> extends SimpleExpression<T> {
	
	static {
		Skript.registerExpression(ExprNamed.class, Object.class, ExpressionType.PROPERTY,
				"%itemtype/inventorytype% (named|with name) %string%"
		);
	}
	
	@Nullable
	private ExprNamed<?> source;
	private Class<T> superType;
	@Nullable
	private Expression<Object> toName;
	@Nullable
	private Expression<String> name;
	
	@SuppressWarnings("unchecked")
	public ExprNamed() {
		this(null, (Class<? extends T>) Object.class);
	}
	
	@SuppressWarnings("unchecked")
	private ExprNamed(ExprNamed<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.toName = source.toName;
			this.name = source.name;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		toName = (Expression<Object>) exprs[0];
		name = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	protected T[] get(final Event e) {
		String name = this.name.getSingle(e);
		Object toName = this.toName.getSingle(e);
		if (name == null || toName == null)
			return null;
		
		try {
			return Converters.convertStrictly(new Object[]{name(toName, name)}, superType);
		} catch (ClassCastException e1) {
			return null;
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Nullable
	public Object name(Object o, String name) {
		if (o instanceof InventoryType) {
			return Bukkit.createInventory(null, (InventoryType) o, name);
		} else if (o instanceof ItemType) {
			ItemType item = (ItemType) o;
			ItemMeta meta = (ItemMeta) item.getItemMeta();
			if (meta == null)
				meta = Bukkit.getItemFactory().getItemMeta(Material.STONE); // Meta is null if the item is air
			meta.setDisplayName(name);
			item.setItemMeta(meta);
			return item;
		}
		return null;
	}
	
	@Override
	public ExprNamed<?> getSource() {
		return source == null ? this : source;
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprNamed<>(this, to);
	}
	
	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return String.format("%s named %s", toName.toString(e, debug), name.toString(e, debug));
	}
}
