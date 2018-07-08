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
package ch.njol.skript.entity;

import org.bukkit.entity.Ocelot;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class OcelotData extends EntityData<Ocelot> {
	static {
		EntityData.register(OcelotData.class, "ocelot", Ocelot.class, 1, "wild ocelot", "ocelot", "cat");
	}
	
	int tamed = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		tamed = matchedPattern - 1;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Ocelot> c, final @Nullable Ocelot e) {
		tamed = e == null ? 0 : e.isTamed() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(final Ocelot entity) {
		if (tamed != 0)
			entity.setTamed(tamed == 1);
	}
	
	@Override
	protected boolean match(final Ocelot entity) {
		return tamed == 0 || entity.isTamed() == (tamed == 1);
	}
	
	@Override
	public Class<? extends Ocelot> getType() {
		return Ocelot.class;
	}
	
	@Override
	protected int hashCode_i() {
		return tamed;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof OcelotData))
			return false;
		final OcelotData other = (OcelotData) obj;
		return tamed == other.tamed;
	}
	
//		return "" + tamed;
	@Override
	protected boolean deserialize(final String s) {
		try {
			tamed = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof OcelotData)
			return tamed == 0 || ((OcelotData) e).tamed == tamed;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new OcelotData();
	}
	
}
