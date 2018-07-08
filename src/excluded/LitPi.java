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
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;

/**
 * trigonometry uses degrees, so pi is mostly useless
 * 
 * @author Peter Güttinger
 */
@Name("Pi")
@Description("The most famous constant. It has the value 3.141592653589793 in Java.")
@Examples("")
public class LitPi extends SimpleLiteral<Double> {
	static {
		Skript.registerExpression(LitPi.class, Double.class, ExpressionType.SIMPLE, "pi");
	}
	
	public LitPi() {
		super(new Double(Math.PI), false);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "pi";
	}
	
}
