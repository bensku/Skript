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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Custom Inventory")
@Description("Returns a chest inventory with the given amount of rows and the name. Use the <a href=effects.html#EffOpenInventory>open inventory</a> effect to open it. " +
	"You can also open a custom inventory using the <a href=classes.html#inventorytype>inventory type</a> of your choosing. " +
	"Some inventories, such as a creative inventory, can not be created/opened to a player.")
@Examples({"open chest inventory with 1 row named \"test\" to player",
		"set {_inventory} to chest inventory with 1 row",
		"open a hopper inventory named \"Senor Hopper\" to player",
		"set {_inv} to an anvil inventory named \"&aSir Fix-a-Lot\" to player",
		"open a loom inventory named \"&aMr Loomy\" to player"})
@Since("2.2-dev34, INSERT VERSION (Inventory Types)")
public class ExprChestInventory extends SimpleExpression<Inventory> {
	
	private static final boolean CAN_CHECK_INV = Skript.methodExists(InventoryType.class, "isCreatable");

    static {
        Skript.registerExpression(ExprChestInventory.class, Inventory.class, ExpressionType.COMBINED,
        		"[a [new]] chest inventory (named|with name) %string% [with %-number% row[s]]",
        		"[a [new]] chest inventory with %number% row[s] [(named|with name) %-string%]",
				"[a[n] [new]] %inventorytype% [(named|with name) %-string%]");
    }

    @Nullable
    private Expression<Number> rows;
    @Nullable
    private Expression<String> name;
    private boolean chest;
	@Nullable
	private Expression<InventoryType> type;

    @SuppressWarnings({"unchecked", "null"})
	@Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
    	chest = matchedPattern != 2;
    	type = !chest ? (Expression<InventoryType>) exprs[0] : null;
        name = (Expression<String>) exprs[chest ? matchedPattern : 1];
        rows = chest ? (Expression<Number>) exprs[matchedPattern ^ 1] : null;
        if (type != null && CAN_CHECK_INV) {
        	InventoryType t = type.getSingle(null);
        	if (t != null && !t.isCreatable()) {
				Skript.error(type + " can not be created", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
        return true;
    }
	
	@SuppressWarnings("null")
    @Override
    protected Inventory[] get(Event e) {
    	if (chest) {
			String name = this.name != null ? this.name.getSingle(e) : "Chest";
			Number rows = this.rows != null ? this.rows.getSingle(e) : 3;
		
			// Shouldn't be null at this point, but empty variables are a thing
			rows = rows == null ? 3 : rows;
			name = name == null ? "Chest" : name;
		
			int size = rows.intValue() * 9;
			if (size % 9 != 0) {
				size = 27;
			}
		
			// Sanitize inventory size
			if (size < 0) // Negative sizes go and crash stuff deep in NMS code
				size = 0;
			if (size > 255) // Too big values cause visual weirdness
				size = 255 * 9; // Plus, REALLY big values will HANG the server
			return CollectionUtils.array(Bukkit.createInventory(null, size, name));
		} else {
			if (this.type == null)
				return null;
    		InventoryType type = this.type.getSingle(e);
    		if (type == null)
    			return null;
    		String name = this.name != null ? this.name.getSingle(e) : null;
    		
    		if (name != null)
    			return CollectionUtils.array(Bukkit.createInventory(null, type, name));
    		else
    			return CollectionUtils.array(Bukkit.createInventory(null, type));
		}
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Inventory> getReturnType() {
        return Inventory.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
    	if (chest) {
			return "chest inventory" + " named " + (name != null ? name.toString(e, debug) : "\"Chest\"") +
				" with " + (rows != null ? rows.toString(e, debug) : "3" + " rows");
		} else {
    		return (type != null ? type.toString(e, debug) : "invalid inventory type") +
				(name != null ? " named " + name.toString(e, debug) : "");
		}
    }

}
