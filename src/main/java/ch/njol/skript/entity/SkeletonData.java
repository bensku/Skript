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

package ch.njol.skript.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class SkeletonData extends EntityData<Skeleton> {
	private final static boolean hasWither = Skript.methodExists(Skeleton.class, "getSkeletonType");
	static {
		if (hasWither)
			register(SkeletonData.class, "skeleton", Skeleton.class, 0, "skeleton", "wither skeleton");
		else
			register(SkeletonData.class, "skeleton", Skeleton.class, "skeleton");
		
	}
	
	private boolean wither;
	
	public SkeletonData() {}
	
	public SkeletonData(final boolean wither) {
		this.wither = wither;
	}
	
	public boolean isWither() {
		return wither;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		wither = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Skeleton> c, final @Nullable Skeleton e) {
		wither = (e == null || !hasWither) ? false : e.getSkeletonType() == SkeletonType.WITHER;
		return true;
	}
	
//		return wither ? "1" : "0";
	@Override
	protected boolean deserialize(final String s) {
		if (s.equals("1"))
			wither = true;
		else if (s.equals("0"))
			wither = false;
		else
			return false;
		return true;
	}
	
	@Override
	public void set(final Skeleton entity) {
		if (hasWither)
			entity.setSkeletonType(wither ? SkeletonType.WITHER : SkeletonType.NORMAL);
	}
	
	@Override
	protected boolean match(final Skeleton entity) {
		return hasWither ? (entity.getSkeletonType() == SkeletonType.WITHER) == wither : true;
	}
	
	@Override
	public Class<? extends Skeleton> getType() {
		return Skeleton.class;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof SkeletonData))
			return false;
		final SkeletonData other = (SkeletonData) obj;
		return other.wither == wither;
	}
	
	@Override
	protected int hashCode_i() {
		return wither ? 1 : 0;
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof SkeletonData)
			return ((SkeletonData) e).wither == wither;
		return false;
	}
	
	@Override
	public EntityData<? extends Entity> getSuperType() {
		return new SkeletonData(wither);
	}
	
}
