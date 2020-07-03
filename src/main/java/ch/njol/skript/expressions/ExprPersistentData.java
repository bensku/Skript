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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator.Relation;
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
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
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

	private ExpressionList<Variable<?>> variables;
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
		ExpressionList<?> exprList = exprs[0] instanceof ExpressionList ? (ExpressionList<?>) exprs[0] : new ExpressionList<>(new Expression<?>[]{exprs[0]}, Object.class, false);
		for (Expression<?> expr : exprList.getExpressions()) {
			if (!(expr instanceof Variable<?>)) { // Input not a variable
				Skript.error("Persistent Data values are formatted as variables (e.g. \"persistent data value {isAdmin}\")" , ErrorQuality.SEMANTIC_ERROR);
				return false;
			} else if (((Variable<?>) expr).isLocal()) { // Input is a variable, but it's local
				Skript.error("Using local variables in persistent data is not supported."
						+ " If you are trying to set a value temporarily, consider using metadata", ErrorQuality.SEMANTIC_ERROR
				);
				return false;
			}
		}
		variables = (ExpressionList<Variable<?>>) exprList;
		holders = (Expression<Object>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	public T[] get(Event e) {
		List<Object> values = new ArrayList<>();
		for (Expression<?> expr : variables.getExpressions()) {
			String varName = ((Variable<?>) expr).getName().toString(e);
			if (varName.contains(Variable.SEPARATOR)) { // It's a list
				for (Object holder : holders.getArray(e))
					Collections.addAll(values, PersistentDataUtils.getList(holder, varName));
			} else { // It's a single variable
				for (Object holder : holders.getArray(e))
					values.add(PersistentDataUtils.getSingle(holder, varName));
			}
		}
		try {
			return Converters.convertStrictly(values.toArray(), superType);
		} catch (ClassCastException ex) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return null;
		for (Expression<?> expr : variables.getExpressions()) {
			if (!((Variable<?>) expr).isList()) {
				if (mode == ChangeMode.REMOVE_ALL)
					return null;
				return CollectionUtils.array(Object.class);
			}
		}
		return CollectionUtils.array(Object[].class);
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		switch (mode) {
			case SET:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList()) {
						varName = varName.replace("*", "");
						for (Object holder : holders.getArray(e)) {
							for (int i = 1; i <= delta.length; i++) {
								// varName + i = var::i (e.g. exampleList::1, exampleList::2, etc.)
								PersistentDataUtils.setList(holder, varName + i, delta[i - 1]);
							}
						}
					} else if (varName.contains(Variable.SEPARATOR)) { // Specific index of a list
						for (Object holder : holders.getArray(e))
							PersistentDataUtils.setList(holder, varName, delta[0]);
					} else { // It's a single variable
						for (Object holder : holders.getArray(e))
							PersistentDataUtils.setSingle(holder, varName, delta[0]);
					}
				}
				break;
			case DELETE:
				for (Expression<?> expr : variables.getExpressions()) {
					String varName = ((Variable<?>) expr).getName().toString(e);
					if (varName.contains(Variable.SEPARATOR)) { // It's a list
						for (Object holder : holders.getArray(e))
							PersistentDataUtils.removeList(holder, varName);
					} else { // It's a single variable
						for (Object holder : holders.getArray(e))
							PersistentDataUtils.removeSingle(holder, varName);
					}
				}
				break;
			case ADD:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList()) {
						varName = varName.replace("*", "");
						for (Object holder : holders.getArray(e)) {
							Map<String, Object> varMap = PersistentDataUtils.getListMap(holder, varName + "*");
							if (varMap == null) {
								// The list is empty, so we don't need to check for the next available index.
								for (int i = 1; i <= delta.length; i++) {
									// varName + i = var::i (e.g. exampleList::1, exampleList::2, etc.)
									PersistentDataUtils.setList(holder, varName + i, delta[i - 1]);
								}
							} else {
								int start = 1;
								for (Object value : delta) {
									while (varMap.containsKey(String.valueOf(start)))
										start++;
									PersistentDataUtils.setList(holder, varName + start, value);
									start++;
								}
							}
						}
					} else if (delta[0] instanceof Number) {
						for (Object holder : holders.getArray(e)) {
							Object n = PersistentDataUtils.getSingle(holder, varName);
							if (n instanceof Number)
								PersistentDataUtils.setSingle(holder, varName, ((Number) n).doubleValue() + ((Number) delta[0]).doubleValue());
						}
					}
				}
				break;
			case REMOVE:
			case REMOVE_ALL:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList() || mode == ChangeMode.REMOVE_ALL) {
						for (Object holder : holders.getArray(e)) {
							Map<String, Object> varMap = PersistentDataUtils.getListMap(holder, varName);
							int sizeBefore = varMap.size();
							if (varMap != null) {
								for (Object value : delta) {
									Iterator<Entry<String, Object>> entries = varMap.entrySet().iterator();
									while (entries.hasNext()) {
										if (Relation.EQUAL.is(Comparators.compare(entries.next().getValue(), value)))
											entries.remove();
									}
								}
								if (sizeBefore != varMap.size()) // It changed so we should set it
									PersistentDataUtils.setListMap(holder, varName, varMap);
							}
						}
					} else if (delta[0] instanceof Number) {
						for (Object holder : holders.getArray(e)) {
							Object n = PersistentDataUtils.getSingle(holder, varName);
							if (n instanceof Number)
								PersistentDataUtils.setSingle(holder, varName, ((Number) n).doubleValue() - ((Number) delta[0]).doubleValue());
						}
					}
				}
				break;
			case RESET:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return variables.isSingle() && holders.isSingle();
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
		return "persistent data value(s) " + variables.toString(e, debug) + " of " + holders.toString(e, debug);
	}

}
