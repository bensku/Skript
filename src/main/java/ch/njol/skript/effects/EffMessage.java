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

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * @author Peter Güttinger
 */
@Name("Message")
@Description("Sends a message to the given player.")
@Examples({"message \"A wild %player% appeared!\"",
		"message \"This message is a distraction. Mwahaha!\"",
		"send \"Your kill streak is %{kill streak.%player%}%.\" to player",
		"if the targeted entity exists:",
		"	message \"You're currently looking at a %type of the targeted entity%!\""})
@Since("1.0")
public class EffMessage extends Effect {
	
	private static final boolean hasSendRaw = Skript.classExists("org.bukkit.conversations.Conversable");
	
	static {
		Skript.registerEffect(EffMessage.class, "(message|send [message]) %chatmessages% [to %commandsenders%]");
	}
	
	@Nullable
	private Expression<String> messages;
	
	@SuppressWarnings("null")
	private Expression<CommandSender> recipients;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		recipients = (Expression<CommandSender>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		if (hasSendRaw) {
			assert messages != null;
			for (final String message : messages.getArray(e)) {
				for (final CommandSender s : recipients.getArray(e)) {
					if (s instanceof Conversable)
						((Conversable) s).sendRawMessage(ChatMessages.get(message));
					// If command block was supposed to receive this message, just ignore it
				}
			}
		} else {
			assert messages != null;
			for (final String message : messages.getArray(e)) {
				for (final CommandSender s : recipients.getArray(e)) {
					s.sendMessage(message);
				}
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		assert messages != null;
		return "send " + messages.toString(e, debug) + " to " + recipients.toString(e, debug);
	}
}
