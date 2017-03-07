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

import java.text.SimpleDateFormat;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;

@Name("Unix Timestamp")
@Description("Converts real world date to Unix timestamp (yyyy-MM-dd HH:mm:ss z).")
@Examples("unix time of now")
@Since("2.2-dev20")
public class ExprUnixTime extends SimplePropertyExpression<Date,String> {
	
	static {
		register(ExprUnixTime.class, String.class, "[unix] time[stamp]", "dates");
	}
	
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
	@Override
	@Nullable
	public String convert(Date f) {
		return format.format(new java.util.Date(f.getTimestamp()));
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "unix timestamp";
	}
	
}
