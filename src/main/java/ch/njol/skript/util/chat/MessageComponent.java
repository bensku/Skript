/*
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
 * 
 */

package ch.njol.skript.util.chat;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Component for chat messages. This can be serialized with GSON and then
 * sent to client.
 */
public class MessageComponent {
	
	public String text = "";
	
	/**
	 * Makes text <b>bold</b>.
	 */
	public boolean bold = false;
	
	/**
	 * Makes text <i>italic</i>.
	 */
	public boolean italic = false;
	
	/**
	 * Makes texxt <u>underlined</u>.
	 */
	public boolean underlined = false;
	
	/**
	 * Makes text <s>strikethrough</s>
	 */
	public boolean strikethrough = false;
	
	/**
	 * Makes text obfuscated, i.e. each tick the client will scramble
	 * all letters with random ones.
	 */
	public boolean obfuscated = false;
	
	/**
	 * Color of this text. Defaults to reseting it.
	 */
	public String color = "reset";
	
	/**
	 * Value of this, if present, will appended on what player is currently
	 * writing to chat.
	 */
	@Nullable
	public String insertion;
	
	@Nullable
	public ClickEvent clickEvent;
	
	public static class ClickEvent {
		
		public ClickEvent(ClickEvent.Action action, String value) {
			this.action = action;
			this.value = value;
		}
		
		public static enum Action  {
			
			open_url,
			
			run_command,
			
			suggest_command,
			
			change_page
		}
		
		public ClickEvent.Action action;
		
		public String value;
	}
	
	public static class HoverEvent {
		
		public HoverEvent(HoverEvent.Action action, String value) {
			this.action = action;
			this.value = value;
		}
		
		public static enum Action {
			
			show_text,
			
			show_item,
			
			show_entity,
			
			show_achievement
		}
		
		public HoverEvent.Action action;
		
		public String value;
	}
	
	@Nullable
	public HoverEvent hoverEvent;
}
