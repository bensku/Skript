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

import org.bukkit.entity.Guardian;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;


public class GuardianData extends EntityData<Guardian> {

	static {
		if(Skript.classExists("org.bukkit.entity.Guardian") && !Skript.isRunningMinecraft(1, 11)){
			EntityData.register(GuardianData.class, "guardian", Guardian.class, 1, "normal guardian", "guardian", "elder guardian");
		}
	}
	
	
	
	private boolean isElder = false;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isElder = matchedPattern == 2;
		return true;
	}

	@SuppressWarnings({"null", "deprecation"})
	@Override
	protected boolean init(Class<? extends Guardian> c, Guardian e) {
		if(e != null)
			isElder = e.isElder();
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void set(Guardian entity) {
		if(isElder)
			entity.setElder(true);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean match(Guardian entity) {
		return entity.isElder() == isElder;
	}

	@Override
	public Class<? extends Guardian> getType() {
		return Guardian.class;
	}

	@Override
	public EntityData getSuperType() {
		return new GuardianData();
	}

	@Override
	protected int hashCode_i() {
		return isElder ? 1 : 0;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof GuardianData))
			return false;
		final GuardianData other = (GuardianData) obj;
		return other.isElder == isElder;
	}

	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof GuardianData)
			return ((GuardianData) e).isElder == isElder;
		return false;
	}
	
}
