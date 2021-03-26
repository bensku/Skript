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
package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Update Inventory")
@Description("Forces an inventory update of the given players")
@Examples({"update inventory of player",
	"update inventory of all players"})
@Since("INSERT VERSION")
public class EffUpdateInventory extends Effect {
	
	static {
		Skript.registerEffect(EffUpdateInventory.class,
			"update inventor(y|ies) of %players%",
			"update %players%'[s] inventor(y|ies)");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Player> playerExpression;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		playerExpression = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		playerExpression.stream(e).forEach(Player::updateInventory);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "update inventor" + (playerExpression.isSingle() ? "y" : "ies")
			+ " of " + playerExpression.toString(e, debug);
	}
	
}
