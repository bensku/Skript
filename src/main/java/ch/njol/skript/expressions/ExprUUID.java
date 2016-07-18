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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("UUID")
@Description({"The UUID of a player or world.",
        "In the future there will be an option to use a player's UUID instead of the name in variable names (i.e. when %player% is used), but for now this can be used.",
        "<em>Please note that this expression does not work for offline players!</em>"})
// TODO [UUID] update documentation after release. Add note about requiring Bukkit 1.7.(9/10)?
@Examples({"# prevents people from joining the server if they use the name of a player",
        "# who has played on this server at least once since this script has been added",
        "on login:",
        "	{uuids.%name of player%} exists:",
        "		{uuids.%name of player%} is not UUID of player",
        "		kick player due to \"Someone with your name has played on this server before\"",
        "	else:",
        "		set {uuids.%name of player%} to UUID of player"})
@Since("2.1.2, 2.2 (offline players' UUIDs)")
public class ExprUUID extends SimplePropertyExpression<Object, String> {
    private final static boolean offlineUUIDSupported = Skript.methodExists(OfflinePlayer.class, "getUniqueId");

    static {
        register(ExprUUID.class, String.class, "UUID", (offlineUUIDSupported ? "offlineplayers" : "players") + "/worlds");
    }

    @Override
    @Nullable
    public String convert(final Object o) {
        if (o instanceof OfflinePlayer) {
            if (offlineUUIDSupported)
                return ((OfflinePlayer) o).getUniqueId().toString();
            else
                return ((Player) o).getUniqueId().toString();
        } else if (o instanceof World) {
            return ((World) o).getUID().toString();
        }
        return null;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "UUID";
    }

}
