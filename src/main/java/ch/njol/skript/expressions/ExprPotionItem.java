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
 */

package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.util.Kleenean;

/**
 * Simple interface for creating vanilla potions (if supported by server).
 * @author bensku
 */
@Name("Potion (item)")
@Description("Potion in item form, with advanced parameters.")
@Examples("strong splash potion of instant damage")
@Since("unknown (2.2)")
public class ExprPotionItem extends SimpleExpression<ItemType> {
	
	public static final String POTION_MODS = "[(0¦(regular|normal)|1¦(strong|upgraded|level 2)|2¦(extended|long)) ][(20¦(splash|exploding)|40¦lingering) ]";
	
	static {
		if (Skript.classExists("org.bukkit.potion.PotionData")) {
			Skript.registerExpression(ExprPotionItem.class, ItemType.class, ExpressionType.SIMPLE,
					POTION_MODS + "potion of %potioneffecttype%", POTION_MODS + "%potioneffecttype% potion", "(water bottle|bottle of water)", "potion");
		}
	}
	
	@Nullable
	private Expression<PotionEffectType> type;
	private int mod = 0; // 1=upgraded, 2=extended
	private int usage = 0; // 0=normal, 1=splash, 2=exploding
	private boolean water = false;
	private boolean matchAll = false;
	
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 2) {
			water = true;
			return true;
		} else if (matchedPattern == 3) {
			water = true;
			matchAll = true;
			return true;
		}
		
		type = (Expression<PotionEffectType>) exprs[0];
		mod = parseResult.mark;
		if (parseResult.mark >= 20) {
			usage = 1;
			mod = parseResult.mark - 20;
		}
		if (parseResult.mark >= 40) {
			usage = 2;
			mod = parseResult.mark - 40;
		}
		if (exprs.length == 0) return false;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected ItemType[] get(final Event e) {
		Material mat = Material.POTION;
		if (usage == 1) mat = Material.SPLASH_POTION;
		if (usage == 2) mat = Material.LINGERING_POTION;
		
		ItemStack item = new ItemStack(mat);
		PotionData potion;
		if (water)
			potion = new PotionData(PotionType.WATER);
		else
			potion = new PotionData(PotionEffectUtils.effectToType(type.getSingle(e)), mod == 2, mod == 1);
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setBasePotionData(potion);
		item.setItemMeta(meta);
		
		ItemType it = new ItemType(item);
		if (matchAll) it.setIgnoreMeta(true);
		return new ItemType[] {it};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null || type == null) return "bottle of water";
		assert type != null;
		return PotionEffectUtils.getPotionName(type.getSingle(e), mod == 2, mod == 1);
	}
}
