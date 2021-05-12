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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

/**
 * @author Peter Güttinger
 */
@Name("Is Blocking")
@Description("Checks whether a player is blocking with their shield.")
@Examples({"on damage of player:",
	  	"	victim is blocking",
	 	"	damage attacker by 0.5 hearts"})
@Since("<i>unknown</i> (before 2.1)")
public class CondIsBlocking extends PropertyCondition<Player> {
	
	static {
		register(CondIsBlocking.class, "(blocking|defending) [with [a] shield]", "players");
	}
	
	@Override
	public boolean check(final Player p) {
		return p.isBlocking();
	}
	
	@Override
	protected String getPropertyName() {
		return "blocking";
	}
	
}
