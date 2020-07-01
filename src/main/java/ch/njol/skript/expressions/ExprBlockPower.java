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

import org.bukkit.block.Block;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

public class ExprBlockPower extends SimplePropertyExpression<Block, Integer> {
	
	static {
		register(ExprBlockPower.class, Integer.class, "power", "block");
	}
	
	@Override
	public Integer convert(Block b) {
		return b.getBlockPower();
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "power";
	}
	
}
