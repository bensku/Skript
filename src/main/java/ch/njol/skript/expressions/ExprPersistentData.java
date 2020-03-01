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

import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.PersistentDataUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataHolder;
import org.eclipse.jdt.annotation.Nullable;

@Name("Persistent Data")
@Description({"Persistent data is a way of storing data on entities, items, and some blocks.",
			"Unlike metadata, it is not affected by server restarts.",
			"See <a href='classes.html#persistentdataholder'>persistent data holder</a> for a list of all holders."})
@Examples({"set persistent data value \"nickname\" of player to \"bob\"",
			"broadcast \"%persistent data value \"\"nickname\"\" of player%\"",
			"clear persistent data value value \"nickname\" of player"})
@Since("INSERT VERSION")
@RequiredPlugins("1.14 or newer")
@SuppressWarnings({"unchecked", "null"})
public class ExprPersistentData<T> extends SimpleExpression<T> {

	static {
		if (Skript.isRunningMinecraft(1, 14)) {
			Skript.registerExpression(ExprPersistentData.class, Object.class, ExpressionType.PROPERTY,
					"persistent data [(value|tag)[s]] %strings% of %persistentdataholders/itemtypes/blocks%",
					"%persistentdataholders/itemtypes/blocks%'[s] persistent data [(value|tag)[s]] %string%"
			);
		}
	}

	@Nullable
	private Expression<String> values;
	@Nullable
	private Expression<Object> holders;

	private ExprPersistentData<?> source;
	private Class<T> superType;

	public ExprPersistentData() {
		this(null, (Class<? extends T>) Object.class);
	}

	private ExprPersistentData(ExprPersistentData<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.values = source.values;
			this.holders = source.holders;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		values = (Expression<String>) exprs[0];
		holders = (Expression<Object>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	public T[] get(Event e) {
		List<Object> list = new ArrayList<>();
		for (String value : values.getArray(e)) {
			for (Object holder : holders.getArray(e)) {
				list.add(PersistentDataUtils.get(holder, value));
			}
		}
		try {
			return Converters.convertStrictly(list.toArray(), superType);
		} catch (ClassCastException e1) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Object.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		for (String value : values.getArray(e)) {
			for (Object holder : holders.getArray(e)) {
				switch (mode) {
					case SET:
						PersistentDataUtils.set(holder, value, delta[0]);
						break;
					case DELETE:
						PersistentDataUtils.remove(holder, value);
						break;
					case ADD:
					case REMOVE:
					case REMOVE_ALL:
					case RESET:
						assert false;
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return values.isSingle() && holders.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprPersistentData<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "persistent data value(s) " + values.toString(e, debug) + " of " + holders.toString(e, debug);
	}

}
