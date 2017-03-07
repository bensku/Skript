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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Hash")
@Description({"Hashes the given text using the MD5 algorithm. This is useful for storing passwords or IP addresses without having to store them literally.",
		"Please note that an MD5 hash is irreversible, i.e. you won't be able to get the original text back (which is the point of storing passwords like this). Brute-force attacks can still be performed on hashes though which can easily crack short or insecure passwords."})
@Examples({
		"command /setpass <text>:",
		"	trigger:",
		"		set {password.%player%} to hashed text-argument",
		"command /login <text>:",
		"	trigger:",
		"		{password.%player%} is hashed text-argument:",
		"			message \"login successful.\"",
		"		else:",
		"			message \"wrong password!\""})
@Since("2.0")
public class ExprHash extends PropertyExpression<String, String> {
	static {
		Skript.registerExpression(ExprHash.class, String.class, ExpressionType.PROPERTY, "[md5]( |-)hash(ed|[( |-|)code] of) %strings%");
	}
	
	@SuppressWarnings("null")
	private final static Charset UTF_8 = Charset.forName("UTF-8");
	
	@Nullable
	static MessageDigest md5 = null;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new InternalError("JVM does not adhere to Java specifications");
		}
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (md5 == null) {
			Skript.error("The Java Virtual Machine running on this server does not support the MD5 algorithm, thus you cannot use the 'hash' expression.");
			return false;
		}
		setExpr((Expression<? extends String>) exprs[0]);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected String[] get(final Event e, final String[] source) {
		assert md5 != null;
		final String[] r = new String[source.length];
		for (int i = 0; i < r.length; i++)
			r[i] = toHex(md5.digest(source[i].getBytes(UTF_8)));
		return r;
	}
	
	private static String toHex(final byte[] b) {
		final char[] r = new char[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			r[2 * i] = Character.forDigit((b[i] & 0xF0) >> 4, 16);
			r[2 * i + 1] = Character.forDigit(b[i] & 0x0F, 16);
		}
		return new String(r);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "hash of " + getExpr();
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
}
