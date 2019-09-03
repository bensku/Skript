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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Bed Occupied")
@Description({"Checks if the bed given is occupied."})
@Examples({"on right click:",
		"	if block is bed:",
		"		if block is occupied:",
		"			send \"Someone's using the bed!\""})
@Since("INSERT VERSION")
public class CondBedOccupied extends Condition {
	
	static {
		Skript.registerCondition(CondBedOccupied.class,
				"%block% is occupied",
				"%block% isn't occupied");
	}
	
	@SuppressWarnings("null")
	private Expression<Block> block;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		block = (Expression<Block>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		Block b = block.getSingle(e);
		if(b == null)
			return false;
		BlockData bd = b.getBlockData();
		if(!(bd instanceof Bed))
			return false;		
		return isNegated() ^ ((Bed)bd).isOccupied();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return block.toString(e, debug) + " is" + (isNegated() ? "n't" : "") + " occupied";
	}
	
}
