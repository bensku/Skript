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

import java.lang.reflect.Array;
import java.util.Arrays;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Clicked Block/Entity")
@Description("The clicked block or entity - only useful in click events")
@Examples({"message \"You clicked on a %type of clicked entity%!\"",
		"clicked block is a chest:",
		"	show the inventory of the clicked block to the player"})
@Since("1.0")
@Events("click")
public class ExprClicked extends SimpleExpression<Object> {
	static {
		Skript.registerExpression(ExprClicked.class, Object.class, ExpressionType.SIMPLE, "[the] clicked (block|%-*itemtype/entitydata%)");
	}
	
	@Nullable
	private EntityData<?> entityType = null;
	/**
	 * null for any block
	 */
	@Nullable
	private ItemType itemType = null;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final Object type = exprs[0] == null ? null : ((Literal<?>) exprs[0]).getSingle();
		if (type instanceof EntityData) {
			entityType = (EntityData<?>) type;
			if (!ScriptLoader.isCurrentEvent(PlayerInteractEntityEvent.class) && !ScriptLoader.isCurrentEvent(PlayerInteractAtEntityEvent.class)) {
				Skript.error("The expression 'clicked entity' can only be used in a click event", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		} else {
			itemType = (ItemType) type;
			if (!ScriptLoader.isCurrentEvent(PlayerInteractEvent.class)) {
				Skript.error("The expression 'clicked block' can only be used in a click event", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return entityType != null ? entityType.getType() : Block.class;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		if (e instanceof PlayerInteractEvent) {
			if (entityType != null)
				return null;
			final Block b = ((PlayerInteractEvent) e).getClickedBlock();
			if (itemType == null || itemType.isOfType(b))
				return new Block[] {b};
			return null;
		} else if (e instanceof PlayerInteractEntityEvent) {
			if (entityType == null)
				return null;
			final Entity en;
//			if (e instanceof PlayerInteractEntityEvent) {
			en = ((PlayerInteractEntityEvent) e).getRightClicked();
//			} else {
//				if (!(((EntityDamageByEntityEvent) e).getDamager() instanceof Player))
//					return null;
//				en = ((EntityDamageByEntityEvent) e).getEntity();
//			}
			if (entityType.isInstance(en)) {
				final Entity[] one = (Entity[]) Array.newInstance(entityType.getType(), 1);
				one[0] = en;
				return one;
			}
			return null;
		}
		return null;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the clicked " + (entityType != null ? entityType : itemType != null ? itemType : "block");
	}
	
}
