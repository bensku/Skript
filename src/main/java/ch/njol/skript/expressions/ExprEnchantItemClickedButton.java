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
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchant Event Clicked Button")
@Description("The enchantment button clicked in an enchant event. It will return 1, 2, or 3.")
@Examples({"on enchant:", 
			"\tif the clicked enchantment button is 1:", 
			"\t\tsend \"You clicked button 1!\""})
@Events("enchant")
@Since("INSERT VERSION")
public class ExprEnchantItemClickedButton extends SimpleExpression<Number>{

	static {
		Skript.registerExpression(ExprEnchantItemClickedButton.class, Number.class, ExpressionType.SIMPLE, "[the] (enchant[ment] button clicked|clicked enchant[ment] button)");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(EnchantItemEvent.class)) {
			Skript.error("The clicked enchantment button is only usable in an enchant event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		return new Number[]{((EnchantItemEvent) e).whichButton()+1};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "clicked enchantment button";
	}

}
