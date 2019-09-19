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

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Cod;
import org.bukkit.entity.Fish;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.TropicalFish;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class FishData extends EntityData<Fish> {

	static {
		if (Skript.isRunningMinecraft(1, 13))
			register(FishData.class, "fish", Fish.class, 0, 
					"fish", "cod", "puffer fish", "salmon", "tropical fish");
	}
	
	private int pattern = -1;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		pattern = (matchedPattern == 0) ? ThreadLocalRandom.current().nextInt(1, 5) : matchedPattern;
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Fish> c, @Nullable Fish e) {
		int matchedPattern = getInitPattern(e);
		pattern = (matchedPattern == 0) ? ThreadLocalRandom.current().nextInt(1, 5) : matchedPattern;
		return true;
	}

	@Override
	public void set(Fish entity) {
		// Setting of entity already handled by init() functions 
	}

	@Override
	protected boolean match(Fish entity) {
		return getPattern(entity) == pattern;
	}

	@Override
	public Class<? extends Fish> getType() {
		if(pattern == 1)
			return Cod.class;
		else if(pattern == 2)
			return PufferFish.class;
		else if(pattern == 3)
			return Salmon.class;
		else if(pattern == 4)
			return TropicalFish.class;
		return Fish.class;
	}

	@Override
	public EntityData getSuperType() {
		return new FishData();
	}

	@Override
	protected int hashCode_i() {
		return pattern;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		return obj instanceof Fish ? getPattern((Fish) obj) == pattern : false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		return e instanceof Fish ? getPattern((Fish) e) == pattern : false;
	}
	
	private static int getInitPattern(@Nullable Fish f) {
		if(f == null)
			return 0;
		else if(f instanceof Cod)
			return 1;
		else if(f instanceof PufferFish)
			return 2;
		else if(f instanceof Salmon)
			return 3;
		else if(f instanceof TropicalFish)
			return 4;
		return 0;
	}
	
	private int getPattern(@Nullable Fish f) {
		int p = getInitPattern(f);
		if(p == 0)
			return pattern;
		return p;
	}
	
}
