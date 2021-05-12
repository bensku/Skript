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

import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation") // Until 1.12: use deprecated methods for compatibility
public class SkeletonData extends EntityData<Skeleton> {
	
	private final static boolean hasWither = Skript.methodExists(Skeleton.class, "getSkeletonType");
	private final static boolean hasStray = Skript.isRunningMinecraft(1, 10);
	private final static boolean separateClasses = Skript.isRunningMinecraft(1, 11);
	
	static {
		// Register nothing on 1.11+ (see SimpleEntityData instead)
		if (!separateClasses) {
			if (hasStray)
				EntityData.register(SkeletonData.class, "skeleton", Skeleton.class, 0, "skeleton", "wither skeleton", "stray");
			else if (hasWither)
				EntityData.register(SkeletonData.class, "skeleton", Skeleton.class, 0, "skeleton", "wither skeleton");
			else
				EntityData.register(SkeletonData.class, "skeleton", Skeleton.class, "skeleton");
		}
	}
	
	private int type;
	public static final int NORMAL = 0, WITHER = 1, STRAY = 2, LAST_INDEX = STRAY;
	
	public SkeletonData() {}
	
	public SkeletonData(final int type) {
		if (type > LAST_INDEX)
			throw new SkriptAPIException("Unsupported skeleton type " + type);
		this.type = type;
	}
	
	public boolean isWither() {
		return type == WITHER;
	}
	
	public boolean isStray() {
		return type == STRAY;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		type = matchedPattern;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Skeleton> c, final @Nullable Skeleton e) {
		if (e == null)
			return true;
		
		if (hasWither && e.getSkeletonType() == SkeletonType.WITHER)
			type = WITHER;
		if (hasStray && e.getSkeletonType() == SkeletonType.STRAY)
			type = STRAY;
		return true;
	}
	
//		return wither ? "1" : "0";
	@Override
	protected boolean deserialize(final String s) {
		try {
			int typeOffer = Integer.parseInt(s);
			if (typeOffer > LAST_INDEX)
				throw new SkriptAPIException("Unsupported skeleton type " + s);
		} catch (NumberFormatException e) {
			throw new SkriptAPIException("Cannot parse skeleton type " + s);
		}
		
		return true;
	}
	
	@Override
	public void set(final Skeleton e) {
		switch (type) {
			case WITHER:
				e.setSkeletonType(SkeletonType.WITHER);
				break;
			case STRAY:
				e.setSkeletonType(SkeletonType.STRAY);
				break;
			default:
				e.setSkeletonType(SkeletonType.NORMAL);
		}
	}
	
	@Override
	protected boolean match(final Skeleton e) {
		switch (type) {
			case WITHER:
				return e.getSkeletonType() == SkeletonType.WITHER;
			case STRAY:
				return e.getSkeletonType() == SkeletonType.STRAY;
			default:
				return e.getSkeletonType() == SkeletonType.NORMAL;
		}
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
		return other.type == type;
	}
	
	@Override
	protected int hashCode_i() {
		return type;
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof SkeletonData)
			return ((SkeletonData) e).type == type;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new SkeletonData(type);
	}
	
}
