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

import org.bukkit.command.BlockCommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Block Command Sender")
@Description("The command block which sent a command. Mostly useful in <a href='../commands/'>commands</a> and <a href='../events.html#command'>command events</a>.")
@Examples({"broadcast \"%command block executor's location%\"",
		"on command:",
		"	if command sender is block command sender:",
		"		send \"You're not a person!\""})
@Since("INSERT VERSION")
@Events("command")
public class ExprBlockCommandSender extends EventValueExpression<BlockCommandSender> {
	static {
		Skript.registerExpression(ExprBlockCommandSender.class, BlockCommandSender.class, ExpressionType.SIMPLE, "[the] [command][ ]block [command['s]] (sender|executor)");
	}
	
	public ExprBlockCommandSender() {
		super(BlockCommandSender.class);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the block command sender";
	}
	
}
