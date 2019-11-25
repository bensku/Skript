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
package ch.njol.skript.conditions;

import org.bukkit.OfflinePlayer;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Op")
@Description("Check whether a player is an operator.")
@Examples({"if player is op:", "if all players are not ops:"})
@Since("INSERT VERSION")
public class CondIsOp  extends PropertyCondition<OfflinePlayer> {
	
	static {
		register(CondIsOp.class, PropertyType.BE, "[an] op[erator][s]", "offlineplayers");
	}
	
	@Override
	public boolean check(OfflinePlayer player) {
		return player.isOp();
	}
	
	@Override
	protected String getPropertyName() {
		return "operator";
	}
	
}
