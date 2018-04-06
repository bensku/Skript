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

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

public class ExprTemperature extends SimplePropertyExpression<Block, Double> {

    static {
        register(ExprTemperature.class, Double.class, "temperature[s]", "blocks");
    }

    @Override
    public Double convert(Block block) {
        return block.getTemperature();
    }

    @Override
    protected String getPropertyName() {
        return "temperature";
    }

    @Override
    public Class<? extends Double> getReturnType() {
        return Double.class;
    }

}
