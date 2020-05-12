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

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.PersistentDataUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Persistent Data")
@Description({"Persistent data is a way of storing data on entities, items, and some blocks.",
			"Unlike metadata, it is not affected by server restarts.",
			"See <a href='classes.html#persistentdataholder'>persistent data holder</a> for a list of all holders.",
			"If the new value when changing a persistent data value can't be persistently stored in variables",
			"(meaning it gets cleared on a restart), it will be set in metadata and a warning will be printed in console.",
			"That value will still be accessible through this expression, but it will be from metadata."
			})
@Examples("set persistent data value {isAdmin} of player to true")
@Since("INSERT VERSION")
@RequiredPlugins("1.14 or newer")
@SuppressWarnings({"null", "unchecked"})
public class ExprPersistentData<T> extends SimpleExpression<T> {

	static {
		if (Skript.isRunningMinecraft(1, 14)) {
			Skript.registerExpression(ExprPersistentData.class, Object.class, ExpressionType.PROPERTY,
					"persistent data [(value|tag)[s]] %objects% of %persistentdataholders/itemtypes/blocks%"
			);
		}
	}

	/**
	 * Persistent data is meant to <i>look</i> like variables
	 * <br>e.g. <b>set persistent data value {isCool} of player to true</b>
	 * <br>e.g. <b>set {_value} to persistent data value {isCool} of player</b>
	 */
	private Variable<?>[] variables = new Variable<?>[]{};

	private Expression<Object> varExpression;
	private Expression<Object> holders;

	private ExprPersistentData<?> source;
	private Class<T> superType;

	public ExprPersistentData() {
		this(null, (Class<? extends T>) Object.class);
	}

	private ExprPersistentData(ExprPersistentData<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.variables = source.variables;
			this.holders = source.holders;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<Variable<?>> vars = new ArrayList<>();
		ExpressionList<?> exprList = exprs[0] instanceof ExpressionList ? (ExpressionList<?>) exprs[0] : new ExpressionList<>(new Expression<?>[]{exprs[0]}, Object.class, false);
		for (Expression<?> expr : exprList.getExpressions()) {
			if (expr instanceof Variable<?>) {
				Variable<?> v = (Variable<?>) expr;
				if (v.isLocal()) {
					Skript.error("Using local variables in Persistent Data is not supported."
								+ " If you are trying to set a value temporarily, consider using Metadata", ErrorQuality.SEMANTIC_ERROR
					);
					return false;
				}
				vars.add(v);
			}
		}
		if (!vars.isEmpty()) {
			variables = vars.toArray(new Variable<?>[0]);
			varExpression = (Expression<Object>) exprs[0];
			holders = (Expression<Object>) exprs[1];
			return true;
		}
		Skript.error("Persistent Data values are formatted as variables (e.g. \"persistent data value {isAdmin}\")" , ErrorQuality.SEMANTIC_ERROR);
		return false;
	}

	@Override
	@Nullable
	public T[] get(Event e) {
		List<Object> values = new ArrayList<>();
		for (Variable<?> v : variables) {
			// TODO handle list stuff here maybe
			String varName = v.getName().toString(e);
			for (Object holder : holders.getArray(e))
				values.add(PersistentDataUtils.get(holder, varName));
		}
		try {
			return Converters.convertStrictly(values.toArray(), superType);
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
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		switch (mode) {
			case SET:
				for (Variable<?> v : variables) {
					String varName = v.getName().toString(e);
					for (Object holder : holders.getArray(e))
						PersistentDataUtils.set(holder, varName, delta[0]);
				}
				break;
			case DELETE:
				for (Variable<?> v : variables) {
					String varName = v.getName().toString(e);
					for (Object holder : holders.getArray(e))
						PersistentDataUtils.remove(holder, varName);
				}
				break;
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return variables.length == 1 && holders.isSingle();
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
		return "persistent data value(s) " + varExpression.toString(e, debug) + " of " + holders.toString(e, debug);
	}

}
