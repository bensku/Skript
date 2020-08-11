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
package ch.njol.skript.conditions;

import org.bukkit.block.Block;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Block Redstone Powered")
@Description("Checks if a block is (indirectly) powered by redstone")
@Examples({"if clicked block is redstone powered:",
	"\tsend \"This block is well-powered by redstone!\"",
	"if clicked block is indirectly redstone powered:",
	"\tsend \"This block is indirectly redstone powered.\""})
@Since("2.5")
public class CondIsBlockRedstonePowered extends PropertyCondition<Block> {
	
	static {
		register(CondIsBlockRedstonePowered.class, "(0¦|1¦indirectly) redstone powered", "blocks");
	}
	
	private boolean isIndirectlyPowered;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
		isIndirectlyPowered = parseResult.mark == 1;
		return true;
	}
	
	@Override
	public boolean check(Block b) {
		return isIndirectlyPowered ? b.isBlockIndirectlyPowered() : b.isBlockPowered();
	}
	
	@Override
	protected String getPropertyName() {
		return isIndirectlyPowered ? "indirectly" : "" + "redstone powered";
	}
}
