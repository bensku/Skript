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

import ch.njol.skript.util.chat.MessageComponent.*;

/**
 * Chat codes; includes color codes (<a href="http://wiki.vg/Chat#Colors">reference</a>)
 * and also, some formatting codes (mostly <a href="http://wiki.vg/Chat">this</a>)
 */
public enum ChatCode {
	
	reset {
		@Override
		public boolean nextComponent() {
			return true;
		}
	},

	// Colors (Vanilla color code, Skript color code if different)
	
	black("black"),
	dark_blue("dark_blue"),
	dark_green("dark_green"),
	dark_aqua("dark_aqua", "dark_cyan"),
	dark_red("dark_red"),
	dark_purple("dark_purple"),
	gold("gold", "orange"),
	gray("gray", "light_grey"),
	dark_gray("dark_gray", "dark_grey"),
	blue("blue"),
	green("green"),
	aqua("aqua", "light_cyan"),
	red("red", "light_red"),
	light_purple("light_purple"),
	yellow("yellow"),
	white("white"),
	
	// Formatting
	
	bold {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.bold = true;
		}
	},
	
	italic {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.italic = true;
		}
	},
	
	underlined {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.underlined = true;
		}
	},
	
	strikethrough {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.strikethrough = true;
		}
	},
	
	obfuscated {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.obfuscated = true;
		}
	},
	
	// clickEvent
	
	open_url(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.open_url, param);
			component.clickEvent = e;
		}
	},
	
	run_command(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.run_command, param);
			component.clickEvent = e;
		}
	},
	
	suggest_command(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.suggest_command, param);
			component.clickEvent = e;
		}
	},
	
	change_page(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.change_page, param);
			component.clickEvent = e;
		}
	},
	
	// hoverEvent
	
	show_text(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			HoverEvent e = new HoverEvent(HoverEvent.Action.show_text, param);
			component.hoverEvent = e;
		}
	};
	
	public boolean hasParam;
	
	@Nullable
	public String colorCode;
	
	@Nullable
	public String colorLangName;
	
	ChatCode(String colorCode, String langName) {
		this.colorCode = colorCode;
		this.colorLangName = langName;
		this.hasParam = false;
	}
	
	ChatCode(String colorCode) {
		this.colorCode = colorCode;
		this.colorLangName = colorCode;
		this.hasParam = false;
	}
	
	ChatCode(boolean hasParam) {
		this.hasParam = hasParam;
	}
	
	ChatCode() {
		this.hasParam = false;
	}
	
	/**
	 * Updates the component.
	 * @param component Component to update.
	 * @param param String parameter.
	 */
	public void updateComponent(MessageComponent component, String param) {
		
	}
	
	public boolean nextComponent() {
		return false;
	}
}
