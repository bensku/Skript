/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Kleenean;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 * @author Peter Güttinger
 */
@Name("Message")
@Description("Sends a message to the given player.")
@Examples({"message \"A wild %player% appeared!\"",
		"message \"This message is a distraction. Mwahaha!\"",
		"send \"Your kill streak is %{kill streak.%player%}%.\" to player",
		"if the targeted entity exists:",
		"\tmessage \"You're currently looking at a %type of the targeted entity%!\""})
@Since("1.0, 2.2-dev26 (advanced features)")
public class EffMessage extends Effect {
	static {
		Skript.registerEffect(EffMessage.class, "(message|send [message]) %strings% [to %commandsenders%]");
	}

	@Nullable
	private Expression<String> messages;
	private boolean canSendRaw;

	@SuppressWarnings("null")
	private Expression<CommandSender> recipients;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		canSendRaw = messages instanceof VariableString;
		recipients = (Expression<CommandSender>) exprs[1];
		return true;
	}

	@Override
	protected void execute(final Event e) {
		if (canSendRaw) {
			assert messages != null;
			List<MessageComponent> componentList = ((VariableString) messages).getMessageComponents(e);
			@SuppressWarnings("null") // Most certainly safe, but I guess not...
					BaseComponent[] components = BungeeConverter.convert(componentList.toArray(new MessageComponent[componentList.size()]));
			for (final CommandSender s : recipients.getArray(e)) {
				if (s instanceof Player) { // Use JSON chat
					((Player) s).spigot().sendMessage(components);
				} else { // Fall back to non-JSON chat
					assert messages != null;
					s.sendMessage(messages.getSingle(e));
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
