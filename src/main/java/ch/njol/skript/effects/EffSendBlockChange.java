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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Send Block Change")
@Description("Makes a player see a block as something it really isn't")
@Examples("make player see block at player as dirt")
@Since("2.2-dev37c")
public class EffSendBlockChange extends Effect {

	private static final boolean SUPPORTED =
			Skript.methodExists(
					Player.class,
					"sendBlockChange",
					Location.class,
					Material.class,
					byte.class
			);

	static {
		Skript.registerEffect(EffSendBlockChange.class,
				"make %players% see %blocks% as %itemtype%"
		);
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings("null")
	private Expression<Block> blocks;

	@SuppressWarnings("null")
	private Expression<ItemType> as;

	@Override
	protected void execute(Event e) {
		ItemType as = this.as.getSingle(e);
		if (as == null)
			return;
		for (Player player : players.getArray(e)) {
			for (Block block : blocks.getArray(e)) {
				Material m = as.getMaterial();
				ItemStack stack = as.getRandom();
				assert stack != null;
				if (Skript.isRunningMinecraft(1, 13))
					player.sendBlockChange(block.getLocation(), m.createBlockData());
				else
					player.sendBlockChange(block.getLocation(), m, (byte) stack.getDurability());
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return String.format(
				"make %s see %s as %s",
				players.toString(e, debug),
				blocks.toString(e, debug),
				as.toString(e, debug)
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The send block change effect is not supported on this version. " +
					"If Spigot has added a replacement method without magic values " +
					"please open an issue at https://github.com/SkriptLang/Skript/issues " +
					"and support will be added for it.");
			return false;
		}
		players = (Expression<Player>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		as = (Expression<ItemType>) exprs[2];
		return true;
	}
}
