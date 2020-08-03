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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockDataUtils;
import ch.njol.skript.util.BlockDataUtils.StateType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Block Data State")
@Description("Get or modify different states of a block.")
@Examples({"set lit state of target block to true",
		"set facing state of target block to north",
		"set {_s} to orientation state of target block"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.13+")
public class ExprBlockDataState extends SimpleExpression<Object> {
	

	static {
		if (Skript.classExists("org.bukkit.block.data.BlockData"))
			Skript.registerExpression(ExprBlockDataState.class, Object.class, ExpressionType.COMBINED,
				"%blockstatetype% [block] state of %blocks%");
	}

	@SuppressWarnings("null")
	private Expression<StateType> stateType;
	@SuppressWarnings("null")
	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		stateType = (Expression<StateType>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		return true;
	}

	@Nullable
	@Override
	protected Object[] get(Event e) {
		List<Object> objects = new ArrayList<>();
		StateType stateType = this.stateType.getSingle(e);
		assert stateType != null;
		for (Block block : blocks.getArray(e)) {
			Object o = BlockDataUtils.getBlockStateType(block, stateType);
			if (o != null)
				objects.add(o);
		}
		return objects.toArray();
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Object.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;

		StateType stateType = this.stateType.getSingle(e);
		assert stateType != null;
		for (Block block : blocks.getArray(e)) {
			BlockDataUtils.setBlockState(block, stateType, delta[0]);
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean d) {
		return stateType.toString(e, d) + " state of " + blocks.toString(e, d);
	}
	
}
