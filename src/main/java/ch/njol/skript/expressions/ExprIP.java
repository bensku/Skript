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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;

/**
 * @author Peter Güttinger
 */
@Name("IP")
@Description("The IP address of a player.")
@Examples({"IP-ban the player # is equal to the next line",
		"ban the IP-address of the player",
		"broadcast \"Banned the IP %IP of player%\""})
@Since("1.4")
public class ExprIP extends SimplePropertyExpression<Player, String> {
	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.PROPERTY, "IP[s][( |-)address[es]] of %players%", "%players%'[s] IP[s][( |-)address[es]]");
	}
	
	@Override
	@Nullable
	public String convert(final Player p) {
		InetSocketAddress socketAddr = p.getAddress();
		if (socketAddr == null)
			return "unknown";
		InetAddress addr = socketAddr.getAddress();
		if (addr == null)
			return "unresolved";
		String hostAddr = addr.getHostAddress();
		if (hostAddr == null)
			return "unresolved";
		return hostAddr;
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "IP address" + (getExpr().isSingle() ? "" : "es");
	}
	
}
