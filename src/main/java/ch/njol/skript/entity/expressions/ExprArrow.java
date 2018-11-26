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
package ch.njol.skript.entity.expressions;

import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Arrow.PickupStatus;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.entity.expressions.base.EntityExpression;

public class ExprArrow extends EntityExpression<Arrow> {

	static {
		register(ExprArrow.class, "[attached] block", "knockback [strength]", "pickup stat(us|e)");
	}

	@Override
	public Class<? extends Object> getReturnType() {
		switch (getProperty()) {
			case 0:
				return Block.class;
			case 1:
				return Number.class;
			case 2:
				return PickupStatus.class;
		}
		return Object.class;
	}

	@SuppressWarnings("null")
	@Override
	protected Object[] get(Event e, Arrow[] arrows) {
		switch (getProperty()) {
			case 0:
				Block[] blocks = new Block[arrows.length];
				for (int i = 0; i < arrows.length; i++)
					blocks[i] = arrows[i].getAttachedBlock();
				return blocks;
			case 1:
				Number[] knockback = new Number[arrows.length];
				for (int i = 0; i < arrows.length; i++)
					knockback[i] = arrows[i].getKnockbackStrength();
				return knockback;
			case 2:
				PickupStatus[] pickup = new PickupStatus[arrows.length];
				for (int i = 0; i < arrows.length; i++)
					pickup[i] = arrows[i].getPickupStatus();
				return pickup;
		}
		return null;
	}
	
}
