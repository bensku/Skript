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
package ch.njol.skript.entity.expressions.base;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.util.ConvertedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * Represents an expression that may only work on the defined Entity.
 */
public abstract class EntityExpression<E extends Entity> extends SimpleExpression<Object> {
	
	private final static Map<Class<? extends EntityExpression<? extends Entity>>, String[]> properties = new HashMap<>();
	@SuppressWarnings("null")
	private Expression<E> expression;
	
	@Nullable
	protected ParseResult parseResult; //Incase someone wants to do complex things with regex etc.
	protected int mark;
	
	/**
	 * Registers an entity expression as {@link ExpressionType#PROPERTY} with the two default property patterns "property of %types%" and "%types%'[s] property"
	 * 
	 * @param c The expression class itself.
	 * @param type The class type that this expression should return, such as a Number or String.
	 * @param property The property name for the syntax, for example <i>shoulder</i> in <i>shoulder of %entities%</i>
	 */
	@SuppressWarnings({"unchecked", "null"})
	protected static <E extends Entity, C extends EntityExpression<E>, T> void register(Class<C> c, String... properties) {
		Class<E> entity = (Class<E>) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
		EntityData<E> data = (EntityData<E>) EntityData.fromClass(entity);
		String name = EntityData.getDefaultCodeName(data);
		String pattern = Utils.createMarkerPattern(properties);
		EntityExpression.properties.put(c, properties);
		Skript.registerExpression(c, Object.class, ExpressionType.PROPERTY, "[the] " + pattern + " (of|from) %" + name + "%", "%" + name + "%'[s] " + pattern);
	}
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public final boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.expression = (Expression<E>) exprs[0];
		this.parseResult = parseResult;
		this.mark = parseResult.mark;
		return false;
	}
	
	public final Expression<? extends E> getExpr() {
		return expression;
	}
	
	protected final E[] getEntities(Event e) {
		return expression.getArray(e);
	}
	
	protected int getProperty() {
		return mark;
	}
	
	@Nullable
	protected String getPropertyName() {
		String[] properties = getProperties();
		assert properties != null;
		if (properties.length - 1 > mark)
			return null;
		return properties[mark];
	}
	
	@Nullable
	protected String[] getProperties() {
		return EntityExpression.properties.get(getClass());
	}
	
	@Override
	protected final Object[] get(Event e) {
		/*for (Entity entity : expression.getArray(e)) {
			if (!entity.getClass().equals(this.entity)) {
				Skript.error("The entities inputted is not of a " + this.entity.getSimpleName() + ". This syntax only accepts those entities.");
				return null;
			}
		}*/
		return get(e, expression.getArray(e));
	}
	
	@Override
	public final Object[] getAll(Event e) {
		/*for (Entity entity : expression.getAll(e)) {
			if (!entity.getClass().equals(this.entity)) {
				Skript.error("The entities inputted is not of a " + this.entity.getSimpleName() + ". This syntax only accepts those entities.");
				return null;
			}
		}*/
		return get(e, expression.getAll(e));
	}
	
	/**
	 * Converts the given source object(s) to the correct type.
	 * <p>
	 * Please note that the returned array must neither be null nor contain any null elements!
	 * 
	 * @param e
	 * @param source
	 * @return An array of the converted entities, which may contain less elements than the source array, but must not be null.
	 * @see Converters#convert(Object[], Class, Converter)
	 */
	protected abstract Object[] get(Event e, E[] entities);
	
	/**
	 * @param source
	 * @param converter must return instances of {@link #getReturnType()}
	 * @return An array containing the converted values
	 * @throws ArrayStoreException if the converter returned invalid values
	 */
	protected Object[] get(E[] source, Converter<? super E, Object> converter) {
		assert source != null;
		assert converter != null;
		return Converters.convertUnsafe(source, getReturnType(), converter);
	}

	@Override
	public final boolean isSingle() {
		return expression.isSingle();
	}
	
	@Override
	public final boolean getAnd() {
		return expression.getAnd();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + getPropertyName() + " of " + getExpr().toString(e, debug);
	}
	
}
