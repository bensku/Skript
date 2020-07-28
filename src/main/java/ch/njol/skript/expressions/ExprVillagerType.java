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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Villager Type")
@Description("Get or modify the type of a villager. By default, a villager's type is determined by the biome they spawn in.")
@Examples({"set villager type of last spawned villager to taiga",
	"set {_type} to villager type of last spawned villager"})
@RequiredPlugins("Minecraft 1.14+")
@Since("INSERT VERSION")
public class ExprVillagerType extends SimplePropertyExpression<LivingEntity, Type> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Villager$Type")) {
			register(ExprVillagerType.class, Type.class, "villager type", "livingentities");
		}
	}
	
	@Nullable
	@Override
	public Type convert(LivingEntity villager) {
		if (villager instanceof Villager)
			return ((Villager) villager).getVillagerType();
		return null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return CollectionUtils.array(Type.class);
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Type type = delta == null ? Type.PLAINS : (Type) delta[0];
		for (LivingEntity entity : getExpr().getArray(e)) {
			if (entity instanceof Villager) {
				((Villager) entity).setVillagerType(type);
			}
		}
	}
	
	@Override
	public Class<? extends Type> getReturnType() {
		return Type.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "villager type";
	}
	
}
