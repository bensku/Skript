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
package ch.njol.skript.entity;

import org.bukkit.entity.Wolf;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class WolfData extends EntityData<Wolf> {
	static {
		EntityData.register(WolfData.class, "wolf", Wolf.class, 1,
				"angry wolf", "wolf", "peaceful wolf",
				"wild wolf", "tamed wolf");
	}
	
	private int angry = 0;
//	private String owner = null;
	private int tamed = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern <= 2)
			angry = matchedPattern - 1;
		else
			tamed = matchedPattern == 3 ? -1 : 1;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Wolf> c, final @Nullable Wolf e) {
		angry = e == null ? 0 : e.isAngry() ? 1 : -1;
		tamed = e == null ? 0 : e.isTamed() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(final Wolf entity) {
		if (angry != 0)
			entity.setAngry(angry == 1);
		if (tamed != 0)
			entity.setTamed(tamed == 1);
//		if (owner != null) {
//			if (owner.isEmpty())
//				entity.setOwner(null);
//			else
//				entity.setOwner(Bukkit.getOfflinePlayer(owner));
//		}
	}
	
	@Override
	public boolean match(final Wolf entity) {
		return (angry == 0 || entity.isAngry() == (angry == 1)) && (tamed == 0 || entity.isTamed() == (tamed == 1));
//				&& (owner == null || owner.isEmpty() && entity.getOwner() == null || entity.getOwner() != null && entity.getOwner().getName().equalsIgnoreCase(owner));
	}
	
	@Override
	public Class<Wolf> getType() {
		return Wolf.class;
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + angry;
		result = prime * result + tamed;
		return result;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof WolfData))
			return false;
		final WolfData other = (WolfData) obj;
		if (angry != other.angry)
			return false;
		if (tamed != other.tamed)
			return false;
		return true;
	}
	
//		return angry + "|" + tamed;
	@Override
	protected boolean deserialize(final String s) {
		final String[] split = s.split("\\|");
		if (split.length != 2)
			return false;
		try {
			angry = Integer.parseInt(split[0]);
			tamed = Integer.parseInt(split[1]);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof WolfData)
			return (angry == 0 || ((WolfData) e).angry == angry) && (tamed == 0 || ((WolfData) e).tamed == tamed);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new WolfData();
	}
	
}
