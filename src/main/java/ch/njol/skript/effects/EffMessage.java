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

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Kleenean;

@Name("Message")
@Description({"Sends a message to the given player. Only styles written",
		"in given string or in <a href=expressions.html#ExprColoured>formatted expressions</a> will be parsed."})
@Examples({"message \"A wild %player% appeared!\"",
		"message \"This message is a distraction. Mwahaha!\"",
		"send \"Your kill streak is %{kill streak::%uuid of player%}%.\" to player",
		"if the targeted entity exists:",
		"	message \"You're currently looking at a %type of the targeted entity%!\""})
@Since("1.0, 2.2-dev26 (advanced features), INSERT VERSION (send multiple times)")
public class EffMessage extends Effect {

	static {
		Skript.registerEffect(EffMessage.class, "(message|send [message[s]]) %strings% [to %commandsenders%] [%-number% times]");
	}

	@SuppressWarnings("null")
	private Expression<? extends String>[] messages;

	/**
	 * Used for {@link EffMessage#toString(Event, boolean)}
	 */
	@SuppressWarnings("null")
	private Expression<String> messageExpr;

	@SuppressWarnings("null")
	private Expression<CommandSender> recipients;

	@Nullable
	private Expression<Number> repeat;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = exprs[0] instanceof ExpressionList ? ((ExpressionList<String>) exprs[0]).getExpressions() : new Expression[] {exprs[0]};
		messageExpr = (Expression<String>) exprs[0];
		recipients = (Expression<CommandSender>) exprs[1];
		repeat = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(final Event e) {
		int times = 1;
		if (repeat != null) {
            Number n = repeat.getSingle(e);
            if (n != null) {
            	times = n.intValue();
            }
		}
		for (Expression<? extends String> message : messages) {
			for (CommandSender receiver : recipients.getArray(e)) {
				if (receiver instanceof Player) { // Can use JSON formatting
					if (message instanceof VariableString) { // Process formatting that is safe
						for (int i = 1; i <= times; i++) {
							((Player) receiver).spigot().sendMessage(BungeeConverter
									.convert(((VariableString) message).getMessageComponents(e)));
						}
					} else if (message instanceof ExprColoured && ((ExprColoured) message).isUnsafeFormat()) { // Manually marked as trusted
						for (String string : message.getArray(e)) {
							assert string != null;
							for (int i = 1; i <= times; i++) {
								((Player) receiver).spigot().sendMessage(BungeeConverter
										.convert(ChatMessages.parse(string)));
							}
						}
					} else { // It is just a string, no idea if it comes from a trusted source -> don't parse anything
						for (String string : message.getArray(e)) {
							assert string != null;
							assert string != null;
							for (int i = 1; i <= times; i++) {
								receiver.sendMessage(string);
							}
						}
					}
				} else { // Not a player, send plain text with legacy formatting
					for (String string : message.getArray(e)) {
						assert string != null;
						for (int i = 1; i <= times; i++) {
							receiver.sendMessage(string);
						}
					}
				}
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<Number> repeat = this.repeat;
		return "send " + messageExpr.toString(e, debug) + " to " + recipients.toString(e, debug) + (repeat == null ? "" : " " + repeat.toString(e, debug)) + " times";
	}
}
