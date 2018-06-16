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
package ch.njol.skript.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class PotionEffectUtils {
	
	private PotionEffectUtils() {}
	
	final static Map<String, PotionEffectType> types = new HashMap<>();
	
	final static String[] names = new String[getMaxPotionId() + 1];
	
	// MCPC+ workaround
	private static int getMaxPotionId() {
		int i = 0;
		for (final PotionEffectType t : PotionEffectType.values()) {
			if (t != null && t.getId() > i)
				i = t.getId();
		}
		return i;
	}
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				types.clear();
				for (final PotionEffectType t : PotionEffectType.values()) {
					if (t == null)
						continue;
					final String[] ls = Language.getList("potions." + t.getName());
					names[t.getId()] = ls[0];
					for (final String l : ls) {
						types.put(l.toLowerCase(), t);
					}
				}
			}
		});
	}
	
	@Nullable
	public static PotionEffectType parseType(final String s) {
		return types.get(s.toLowerCase());
	}
	
	@SuppressWarnings("null")
	public static String toString(final PotionEffectType t) {
		return names[t.getId()];
	}
	
	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final PotionEffectType t, final int flags) {
		return names[t.getId()];
	}
	
	public static String[] getNames() {
		return names;
	}
	
	public static short guessData(final ThrownPotion p) {
		if (p.getEffects().size() == 1) {
			final PotionEffect e = p.getEffects().iterator().next();
			final Potion d = new Potion(PotionType.getByEffect(e.getType())).splash();
			return d.toDamageValue();
		}
		return 0;
	}
	
	/**
	 * Checks if given string represents a known potion type and returns that type.
	 * Unused currently, will be used soon (TM).
	 * @param name Name of potion type
	 * @return
	 */
	@Nullable
	public static PotionType checkPotionType(String name) {
		switch (name) {
			case "uncraftable":
			case "empty":
				return PotionType.UNCRAFTABLE;
			case "mundane":
				return PotionType.MUNDANE;
			case "thick":
				return PotionType.THICK;
			case "night vision":
			case "darkvision":
				return PotionType.NIGHT_VISION;
			case "invisibility":
				return PotionType.INVISIBILITY;
			case "leaping":
			case "jump boost":
				return PotionType.JUMP;
			case "fire resistance":
			case "fire immunity":
				return PotionType.FIRE_RESISTANCE;
			case "swiftness":
			case "speed":
				return PotionType.SPEED;
			case "slowness":
				return PotionType.SLOWNESS;
			case "water breathing":
				return PotionType.WATER_BREATHING;
			case "instant health":
			case "healing":
			case "health":
				return PotionType.INSTANT_HEAL;
			case "instant damage":
			case "harming":
			case "damage":
				return PotionType.INSTANT_DAMAGE;
			case "poison":
				return PotionType.POISON;
			case "regeneration":
			case "regen":
				return PotionType.REGEN;
			case "strength":
				return PotionType.STRENGTH;
			case "weakness":
				return PotionType.WEAKNESS;
			case "luck":
				return PotionType.LUCK;
		}
		
		return null;
	}
	
	/**
	 * Wrapper around deprecated API function, in case it gets removed.
	 * Changing one method is easier that changing loads of them from different expressions.
	 * @param effect Type.
	 * @return Potion type.
	 */
	@Nullable
	public static PotionType effectToType(PotionEffectType effect) {
		return PotionType.getByEffect(effect);
	}
	
	/**
	 * Get potion string representation.
	 * @param effect
	 * @param extended
	 * @param strong
	 * @return
	 */
	public static String getPotionName(@Nullable PotionEffectType effect, boolean extended, boolean strong) {
		if (effect == null) return "bottle of water"; 
		
		String s = "";
		if (extended) s += "extended";
		else if (strong) s += "strong";
		s += " potion of ";
		s += toString(effect);
		
		return s;
	}
}
