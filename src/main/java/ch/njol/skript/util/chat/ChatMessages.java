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
package ch.njol.skript.util.chat;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles parsing chat messages.
 */
public class ChatMessages {
	/**
	 * Link parse mode for potential links which are not marked with tags.
	 */
	public static LinkParseMode linkParseMode = LinkParseMode.DISABLED;

	/**
	 * If color codes should also function as reset code.
	 */
	public static boolean colorResetCodes = false;

	/**
	 * Chat codes, see {@link ChatCode}.
	 */
	private static final Map<String, ChatCode> codes = new HashMap<>();

	private static final ChatCode[] colorChars = new ChatCode[256];

	@SuppressWarnings("null")
	private static final Pattern linkPattern = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");

	/**
	 * Instance of GSON we use for serialization.
	 */
	private static final Gson gson;

	public static void registerListeners() {
		// When language changes or server is loaded loop through all chatcodes
		Language.addListener(() -> {
			codes.clear();

			Skript.debug("Parsing message style lang files");
			for (ChatCode code : ChatCode.values()) {
				if (code.colorCode != null) { // Color code!
					for (String name : Language.getList(Color.LANGUAGE_NODE + "." + code.langName + ".names")) {
						codes.put(name, code);
					}
				} else { // Not color code
					for (String name : Language.getList("chat styles." + code.langName)) {
						codes.put(name, code);
					}
				}

				// Register color char
				if (code.colorChar != 0) {
					colorChars[code.colorChar] = code;
				}
			}

			// Add formatting chars
			colorChars['k'] = ChatCode.obfuscated;
			colorChars['l'] = ChatCode.bold;
			colorChars['m'] = ChatCode.strikethrough;
			colorChars['n'] = ChatCode.underlined;
			colorChars['o'] = ChatCode.italic;
			colorChars['r'] = ChatCode.reset;
		});
	}

	static {
		gson = new GsonBuilder().registerTypeAdapter(boolean.class, new MessageComponent.BooleanSerializer()).create();
	}

	/**
	 * Component list - this is just serialization mapper class.
	 */
	private static class ComponentList {

		public ComponentList(List<MessageComponent> components) {
			this.extra = components;
		}

		@SuppressWarnings("unused")
		public ComponentList(MessageComponent[] components) {
			this.extra = Arrays.asList(components);
		}

		/**
		 * DO NOT USE!
		 */
		@SuppressWarnings("unused")
		@Deprecated
		public String text = "";

		/**
		 * Actual message data.
		 */
		@SuppressWarnings("unused")
		@Nullable
		public List<MessageComponent> extra;
	}

	/**
	 * Parses a string to list of chat message components.
	 *
	 * @param msg Input string.
	 * @return List with components.
	 */
	public static List<MessageComponent> parse(String msg) {
		char[] chars = msg.toCharArray();

		List<MessageComponent> components = new ArrayList<>();
		MessageComponent current = new MessageComponent();
		components.add(current);
		StringBuilder curStr = new StringBuilder();

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			ChatCode code = null;
			String param = "";

			if (c == '<') { // Tag parsing
				// Find where the tag ends
				int end = -1;
				int angleBrackets = 1; // Ignore stuff that looks like tag inside the tag
				for (int j = i + 1; j < chars.length; j++) {
					char c2 = chars[j];
					if (c2 == '<')
						angleBrackets++;
					else if (c2 == '>')
						angleBrackets--;

					if (angleBrackets == 0) {
						end = j;
						break;
					}
				}

				if (end != -1) { // If this COULD be valid tag...
					String tag = msg.substring(i + 1, end);
					String name;
					if (tag.contains(":")) {
						String[] split = tag.split(":", 2);
						name = split[0];
						param = split[1];
					} else {
						name = tag;
					}

					code = codes.get(name);
					if (code != null) { // ... and if the tag IS really valid
						String text = curStr.toString();
						curStr = new StringBuilder();
						current.text = text;

						MessageComponent old = current;
						current = new MessageComponent();

						components.add(current);

						if (code.colorCode != null) // Just update color code
							current.color = code.colorCode;
						else
							code.updateComponent(current, param); // Call ChatCode update

						// Copy styles from old to current if needed
						copyStyles(old, current);

						// Increment i to tag end
						i = end;
						continue;
					}

					// If this is invalid tag, just ignore it. Maybe scripter was trying to be clever with formatting
				}
			} else if (c == '&' || c == '§') {
				// Corner case: this is last character, so we cannot get next
				if (i == chars.length - 1) {
					curStr.append(c);
					continue;
				}

				char color = chars[i + 1];
				code = colorChars[color];
				if (code == null) {
					curStr.append(c).append(color); // Invalid formatting char, plain append
				} else {
					String text = curStr.toString();
					curStr = new StringBuilder();
					current.text = text;

					MessageComponent old = current;
					current = new MessageComponent();

					components.add(current);

					if (code.colorCode != null) // Just update color code
						current.color = code.colorCode;
					else
						code.updateComponent(current, param); // Call ChatCode update

					// Copy styles from old to current if needed
					copyStyles(old, current);
				}

				i++; // Skip this and color char
				continue;
			}

			// Attempt link parsing, if a tag was not found
			if (linkParseMode == LinkParseMode.STRICT && c == 'h') {
				String rest = msg.substring(i); // Get rest of string

				String link = null;
				if (rest.startsWith("http://") || rest.startsWith("https://")) {
					link = rest.split(" ", 2)[0];
				}

				// Link found
				if (link != null && !link.isEmpty()) {
					// Take previous component, create new
					String text = curStr.toString();
					curStr = new StringBuilder();
					current.text = text;

					MessageComponent old = current;
					current = new MessageComponent();
					copyStyles(old, current);

					components.add(current);

					// Make new component a link
					ChatCode.open_url.updateComponent(current, link); // URL for client...
					current.text = link; // ... and for player

					i += link.length() - 1; // Skip link for all other parsing

					// Add one MORE component (this comes after the link)
					current = new MessageComponent();
					components.add(current);
					continue;
				}
			} else if (linkParseMode == LinkParseMode.LENIENT && (i == 0 || chars[i - 1] == ' ')) {
				// Lenient link parsing
				String rest = msg.substring(i); // Get rest of string

				String link = null;
				String potentialLink = rest.split(" ", 2)[0]; // Split stuff
				if (linkPattern.matcher(potentialLink).matches()) { // Check if it is at least somewhat valid URL
					link = potentialLink;
				}

				// Link found
				if (link != null && !link.isEmpty()) {
					// Insert protocol (aka guess it) if it isn't there
					String url;
					if (!link.startsWith("http://") && !link.startsWith("https://")) {
						url = "http://" + link; // Hope that http -> https redirect works on target site...
					} else {
						url = link;
					}

					// Take previous component, create new
					String text = curStr.toString();
					curStr = new StringBuilder();
					current.text = text;

					MessageComponent old = current;
					current = new MessageComponent();
					copyStyles(old, current);

					components.add(current);

					// Make new component a link
					ChatCode.open_url.updateComponent(current, url); // URL for client...
					current.text = link; // ... and for player

					i += link.length() - 1; // Skip link for all other parsing

					// Add one MORE component (this comes after the link)
					current = new MessageComponent();
					components.add(current);
					continue;
				}
			}

			curStr.append(c); // Append this char to curStr
		}

		String text = curStr.toString();
		current.text = text;

		return components;
	}

	@SuppressWarnings("null")
	public static MessageComponent[] parseToArray(String msg) {
		return parse(msg).toArray(new MessageComponent[0]);
	}

	public static String toJson(String msg) {
		ComponentList componentList = new ComponentList(parse(msg));
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}

	public static String toJson(List<MessageComponent> components) {
		ComponentList componentList = new ComponentList(components);
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}

	/**
	 * Copies styles from component to another. Note that this only copies
	 * additional styling, i.e. if text was not bold and is bold, it will remain bold.
	 *
	 * @param from
	 * @param to
	 */
	public static void copyStyles(MessageComponent from, MessageComponent to) {
		if (to.reset)
			return;

		// If we don't have color or colors don't reset formatting, copy formatting
		if (to.color == null || !colorResetCodes) {
			if (!to.bold)
				to.bold = from.bold;
			if (!to.italic)
				to.italic = from.italic;
			if (!to.underlined)
				to.underlined = from.underlined;
			if (!to.strikethrough)
				to.strikethrough = from.strikethrough;
			if (!to.obfuscated)
				to.obfuscated = from.obfuscated;
			if (to.color == null)
				to.color = from.color;
		}

		// Links and such are never reset by color codes - weird, but it'd break too much stuff
		if (to.clickEvent == null)
			to.clickEvent = from.clickEvent;
		if (to.insertion == null)
			to.insertion = from.insertion;
		if (to.hoverEvent == null)
			to.hoverEvent = from.hoverEvent;
	}


	public static void shareStyles(MessageComponent[] components) {
		MessageComponent previous = null;
		for (MessageComponent c : components) {
			if (previous != null) {
				assert c != null;
				copyStyles(previous, c);
			}
			previous = c;
		}
	}

	/**
	 * Constructs plain text only message component.
	 *
	 * @param str
	 */
	public static MessageComponent plainText(String str) {
		MessageComponent component = new MessageComponent();
		component.text = str;
		return component;
	}
}