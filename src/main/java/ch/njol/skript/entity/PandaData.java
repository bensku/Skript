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

import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class PandaData extends EntityData<Panda> {
	
	static {
		if (Skript.isRunningMinecraft(1, 14))
			EntityData.register(PandaData.class, "panda", Panda.class, "panda");
	}
	
	@Nullable
	private Gene mainGene = null, hiddenGene = null;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			mainGene = (Gene) exprs[0].getSingle();
		if (exprs[1] != null)
			hiddenGene = (Gene) exprs[1].getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Panda> c, @Nullable Panda panda) {
		if (panda != null) {
			mainGene = panda.getMainGene();
			hiddenGene = panda.getHiddenGene();
		}
		return true;
	}
	
	@Override
	public void set(Panda entity) {
		Gene gen = mainGene;
		if (gen == null)
			gen = Gene.values()[ThreadLocalRandom.current().nextInt(0, 7)];
		assert gen != null;
		entity.setMainGene(gen);
		entity.setHiddenGene(hiddenGene != null ? hiddenGene : gen);
	}
	
	@Override
	protected boolean match(Panda entity) {
		if (hiddenGene != null) {
			if(mainGene != null)
				return mainGene == entity.getMainGene() && hiddenGene == entity.getHiddenGene();
			else
				return hiddenGene == entity.getHiddenGene();
		} else {
			if(mainGene != null)
				return mainGene == entity.getMainGene();
			else
				return true;
		}
	}
	
	@Override
	public Class<? extends Panda> getType() {
		return Panda.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new PandaData();
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 7;
		int result = 0;
		result = result * prime + (mainGene != null ? mainGene.hashCode() : 0);
		result = result * prime + (hiddenGene != null ? hiddenGene.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof PandaData))
			return false;
		PandaData d = (PandaData) data;
		return d.mainGene == mainGene && d.hiddenGene == hiddenGene;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof PandaData))
			return false;
		PandaData d = (PandaData) data;
		return (mainGene == null || mainGene == d.mainGene) && (hiddenGene == null || hiddenGene == d.hiddenGene);
	}
}
