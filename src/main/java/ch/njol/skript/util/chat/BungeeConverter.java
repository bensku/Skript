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
package ch.njol.skript.util.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Converts Skript's chat components into Bungee's BaseComponents which Spigot
 * supports, too.
 */
public class BungeeConverter {
	
	@SuppressWarnings("null")
	public static BaseComponent convert(MessageComponent origin) {
		BaseComponent base = new TextComponent(origin.text);
		
		base.setBold(origin.bold);
		base.setItalic(origin.italic);
		base.setUnderlined(origin.underlined);
		base.setStrikethrough(origin.strikethrough);
		base.setObfuscated(origin.obfuscated);
		if (origin.color != null) // TODO this is crappy way to copy *color* over...
			base.setColor(ChatColor.getByChar(SkriptChatCode.valueOf(origin.color).getColorChar()));
		base.setInsertion(origin.insertion);
		
		if (origin.clickEvent != null)
			base.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(origin.clickEvent.action.spigotName), origin.clickEvent.value));
		if (origin.hoverEvent != null)
			base.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(origin.hoverEvent.action.spigotName),
					new BaseComponent[] {new TextComponent(origin.hoverEvent.value)})); // WAIT WHAT?!?
		
		return base;
	}
	
	@SuppressWarnings("null") // For origins[i] access
	public static BaseComponent[] convert(MessageComponent[] origins) {
		BaseComponent[] bases = new BaseComponent[origins.length];
		for (int i = 0; i < origins.length; i++) {
			bases[i] = convert(origins[i]);
		}
		
		return bases;
	}
}
