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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class VillagerData extends EntityData<Villager> {
	static {
		// professions in order!
		// FARMER(0), LIBRARIAN(1), PRIEST(2), BLACKSMITH(3), BUTCHER(4);
		register(VillagerData.class, "villager", Villager.class, 0,
				"villager", "farmer", "librarian", "priest", "blacksmith", "butcher");
		
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");
	}
	
	@Nullable
	private Profession profession = null;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = Profession.values()[matchedPattern - 1];
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Villager> c, final @Nullable Villager e) {
		profession = e == null ? null : e.getProfession();
		return true;
	}
	
	@Override
	public void set(final Villager entity) {
		if (profession != null)
			entity.setProfession(profession);
	}
	
	@Override
	@Nullable
	public Villager spawn(final Location loc) {
		final Villager v = super.spawn(loc);
		if (v == null)
			return null;
		if (profession == null)
			v.setProfession(CollectionUtils.getRandom(Profession.values()));
		return v;
	}
	
	@Override
	protected boolean match(final Villager entity) {
		return profession == null || entity.getProfession() == profession;
	}
	
	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}
	
	@Override
	protected int hashCode_i() {
		return profession != null ? profession.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof VillagerData))
			return false;
		final VillagerData other = (VillagerData) obj;
		return profession == other.profession;
	}
	
//		return profession == null ? "" : profession.name();
	@Override
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		try {
			profession = Profession.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof VillagerData)
			return profession == null || ((VillagerData) e).profession == profession;
		return false;
	}
	
	@Override
	public EntityData<? extends Entity> getSuperType() {
		return new VillagerData();
	}
	
}
