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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Facing")
@Description("The facing of an entity or block, i.e. exactly north, south, east, west, up or down (unlike <a href='#ExprDirection'>direction</a> which is the exact direction, e.g. '0.5 south and 0.7 east')")
@Examples({"# makes a bridge",
		"loop blocks from the block below the player in the horizontal facing of the player:",
		"	set block to cobblestone"})
@Since("1.4")
public class ExprFacing extends SimplePropertyExpression<Object, Direction> {
	static {
		register(ExprFacing.class, Direction.class, "(1¦horizontal|) facing", "livingentities/blocks");
	}
	
	private boolean horizontal;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		horizontal = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@SuppressWarnings({"deprecation", "null"})
	@Override
	@Nullable
	public Direction convert(final Object o) {
		if (o instanceof Block) {
			final MaterialData d = ((Block) o).getType().getNewData(((Block) o).getData());
			if (d instanceof Directional)
				return new Direction(((Directional) d).getFacing(), 1);
			return null;
		} else if (o instanceof LivingEntity) {
			return new Direction(Direction.getFacing(((LivingEntity) o).getLocation(), horizontal), 1);
		}
		assert false;
		return null;
	}
	
	@Override
	protected String getPropertyName() {
		return (horizontal ? "horizontal " : "") + "facing";
	}
	
	@Override
	public Class<Direction> getReturnType() {
		return Direction.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!Block.class.isAssignableFrom(getExpr().getReturnType()))
			return null;
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Direction.class);
		return null;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		assert mode == ChangeMode.SET;
		assert delta != null;
		
		final Block b = (Block) getExpr().getSingle(e);
		if (b == null)
			return;
		final MaterialData d = b.getType().getNewData(b.getData());
		if (!(d instanceof Directional))
			return;
		((Directional) d).setFacingDirection(toBlockFace(((Direction) delta[0]).getDirection(b)));
		b.setData(d.getData());
	}
	
	private static BlockFace toBlockFace(final Vector dir) {
//		dir.normalize();
		BlockFace r = null;
		double d = Double.MAX_VALUE;
		for (final BlockFace f : BlockFace.values()) {
			final double a = Math.pow(f.getModX() - dir.getX(), 2) + Math.pow(f.getModY() - dir.getY(), 2) + Math.pow(f.getModZ() - dir.getZ(), 2);
			if (a < d) {
				d = a;
				r = f;
			}
		}
		assert r != null;
		return r;
	}
	
}
