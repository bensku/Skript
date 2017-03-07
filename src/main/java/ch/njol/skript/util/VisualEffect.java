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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.hooks.ParticlesPlugin;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.YggdrasilSerializable;
import de.slikey.effectlib.util.ParticleEffect;
import de.slikey.effectlib.util.ParticleEffect.ParticleData;
import de.slikey.effectlib.util.ParticleEffect.BlockData;
import de.slikey.effectlib.util.ParticleEffect.ItemData;

/**
 * @author Peter Güttinger
 */
public final class VisualEffect implements SyntaxElement, YggdrasilSerializable {
	public static boolean EFFECT_LIB = false;
	private final static String LANGUAGE_NODE = "visual effects";
	
	public static enum Type implements YggdrasilSerializable {
		ENDER_SIGNAL(Effect.ENDER_SIGNAL),
		MOBSPAWNER_FLAMES(Effect.MOBSPAWNER_FLAMES),
		POTION_BREAK(Effect.POTION_BREAK) {
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				return new PotionEffect(raw == null ? PotionEffectType.SPEED : (PotionEffectType) raw, 1, 0);
			}
		},
		SMOKE(Effect.SMOKE) {
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				if (raw == null)
					return BlockFace.SELF;
				return Direction.getFacing(((Direction) raw).getDirection(l), false); // TODO allow this to not be a literal
			}
		},
		HURT(EntityEffect.HURT),
		SHEEP_EAT(EntityEffect.SHEEP_EAT),
		WOLF_HEARTS(EntityEffect.WOLF_HEARTS),
		WOLF_SHAKE(EntityEffect.WOLF_SHAKE),
		WOLF_SMOKE(EntityEffect.WOLF_SMOKE),
		
		// Particles
		FIREWORKS_SPARK(Effect.FIREWORKS_SPARK),
		CRIT(Effect.CRIT),
		MAGIC_CRIT(Effect.MAGIC_CRIT),
		POTION_SWIRL(Effect.POTION_SWIRL) {
			@Override
			public boolean isColorable() {
				return true;
			}
		},
		POTION_SWIRL_TRANSPARENT(Effect.POTION_SWIRL_TRANSPARENT) {
			@Override
			public boolean isColorable() {
				return true;
			}
		},
		SPELL(Effect.SPELL),
		INSTANT_SPELL(Effect.INSTANT_SPELL),
		WITCH_SPELL(Effect.WITCH_MAGIC),
		NOTE(Effect.NOTE),
		PORTAL(Effect.PORTAL),
		FLYING_GLYPH(Effect.FLYING_GLYPH),
		FLAME(Effect.FLAME),
		LAVA_POP(Effect.LAVA_POP),
		FOOTSTEP(Effect.FOOTSTEP),
		SPLASH(Effect.SPLASH),
		PARTICLE_SMOKE(Effect.PARTICLE_SMOKE), // Why separate particle... ?
		EXPLOSION_HUGE(Effect.EXPLOSION_HUGE),
		EXPLOSION_LARGE(Effect.EXPLOSION_LARGE),
		EXPLOSION(Effect.EXPLOSION),
		VOID_FOG(Effect.VOID_FOG),
		SMALL_SMOKE(Effect.SMALL_SMOKE),
		CLOUD(Effect.CLOUD),
		COLOURED_DUST(Effect.COLOURED_DUST) {
			@Override
			public boolean isColorable() {
				return true;
			}
		},
		SNOWBALL_BREAK(Effect.SNOWBALL_BREAK),
		WATER_DRIP(Effect.WATERDRIP),
		LAVA_DRIP(Effect.LAVADRIP),
		SNOW_SHOVEL(Effect.SNOW_SHOVEL),
		SLIME(Effect.SLIME),
		HEART(Effect.HEART),
		ANGRY_VILLAGER(Effect.VILLAGER_THUNDERCLOUD),
		HAPPY_VILLAGER(Effect.HAPPY_VILLAGER),
		LARGE_SMOKE(Effect.LARGE_SMOKE),
		ITEM_CRACK(Effect.ITEM_BREAK) {
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				if (raw == null)
					return Material.IRON_SWORD;
				else if (raw instanceof ItemType) {
					ItemStack rand = ((ItemType) raw).getRandom();
					if (rand == null) return Material.IRON_SWORD;
					Material type = rand.getType();
					assert type != null;
					return type;
				} else {
					return raw;
				}
			}
		},
		BLOCK_BREAK(Effect.TILE_BREAK) {
			@SuppressWarnings("null")
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				if (raw == null)
					return Material.STONE.getData();
				else if (raw instanceof ItemType) {
					ItemStack rand = ((ItemType) raw).getRandom();
					if (rand == null) return Material.STONE.getData();
					MaterialData type = rand.getData();
					assert type != null;
					return type;
				} else {
					return raw;
				}
			}
		},
		BLOCK_DUST(Effect.TILE_DUST) {
			@SuppressWarnings("null")
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				if (raw == null)
					return Material.STONE.getData();
				else if (raw instanceof ItemType) {
					ItemStack rand = ((ItemType) raw).getRandom();
					if (rand == null) return Material.STONE.getData();
					MaterialData type = rand.getData();
					assert type != null;
					return type;
				} else {
					return raw;
				}
			}
		};
		
		final Object effect;
		@Nullable
		final String name;
		
		private Type(final Effect effect) {
			this.effect = effect;
			this.name = effect.getName();
		}
		
		private Type(final EntityEffect effect) {
			this.effect = effect;
			this.name = null;
		}
		
		/**
		 * Converts the data from the pattern to the data required by Bukkit
		 */
		@Nullable
		public Object getData(final @Nullable Object raw, final Location l) {
			assert raw == null;
			return null;
		}
		
		/**
		 * Checks if this effect has color support.
		 */
		public boolean isColorable() {
			return false;
		}
		
		/**
		 * Gets Minecraft name of the effect, if it exists.
		 * @return Name or null if effect uses numeric id instead.
		 */
		@Nullable
		public String getMinecraftName() {
			return this.name;
		}
	}
	
	private final static String TYPE_ID = "VisualEffect.Type";
	static {
		Variables.yggdrasil.registerSingleClass(Type.class, TYPE_ID);
		Variables.yggdrasil.registerSingleClass(Effect.class, "Bukkit_Effect");
		Variables.yggdrasil.registerSingleClass(EntityEffect.class, "Bukkit_EntityEffect");
	}
	
	@Nullable
	static SyntaxElementInfo<VisualEffect> info;
	final static List<Type> types = new ArrayList<Type>(Type.values().length);
	final static Noun[] names = new Noun[Type.values().length];
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				final Type[] ts = Type.values();
				types.clear();
				final List<String> patterns = new ArrayList<String>(ts.length);
				for (int i = 0; i < ts.length; i++) {
					final String node = LANGUAGE_NODE + "." + ts[i].name();
					final String pattern = Language.get_(node + ".pattern");
					if (pattern == null) {
						if (Skript.testing())
							Skript.warning("Missing pattern at '" + (node + ".pattern") + "' in the " + Language.getName() + " language file");
					} else {
						types.add(ts[i]);
						if (ts[i].isColorable())
							patterns.add(pattern);
						else {
							String dVarExpr = Language.get_(LANGUAGE_NODE + ".area_expression");
							if (dVarExpr == null) dVarExpr = "";
							patterns.add(pattern + " " + dVarExpr);
						}
					}
					if (names[i] == null)
						names[i] = new Noun(node + ".name");
				}
				final String[] ps = patterns.toArray(new String[patterns.size()]);
				assert ps != null;
				info = new SyntaxElementInfo<VisualEffect>(ps, VisualEffect.class);
			}
		});
	}
	
	private Type type;
	@Nullable
	private Object data;
	private float speed = 0;
	private float dX, dY, dZ = 0;
	@Nullable
	private org.bukkit.Color color;
	
	/**
	 * For parsing & deserialisation
	 */
	@SuppressWarnings("null")
	public VisualEffect() {}
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = types.get(matchedPattern);
		
		if (type.isColorable()) {
			for (Expression<?> expr : exprs) {
				if (expr == null) continue;
				else if (expr.getReturnType() == Color.class) {
					color = ((Color) expr.getSingle(null)).getBukkitColor();
				} else {
					data = expr.getSingle(null);
				}
			}
		} else {
			int numberParams = 0;
			for (Expression<?> expr : exprs) {
				if (expr.getReturnType() == Long.class || expr.getReturnType() == Integer.class || expr.getReturnType() == Number.class)
					numberParams++;
			}
			
			int dPos = 0; // Data index
			Expression<?> expr = exprs[0];
			if (expr.getReturnType() != Long.class && expr.getReturnType() != Integer.class && expr.getReturnType() != Number.class) {
				dPos = 1;
				data = exprs[0].getSingle(null);
			}
			
			if (numberParams == 1) // Only speed
				speed = ((Number) exprs[dPos].getSingle(null)).floatValue();
			else if (numberParams == 3) { // Only dX, dY, dZ
				dX = ((Number) exprs[dPos].getSingle(null)).floatValue();
				dY = ((Number) exprs[dPos + 1].getSingle(null)).floatValue();
				dZ = ((Number) exprs[dPos + 2].getSingle(null)).floatValue();
			} else if (numberParams == 4){ // Both present
				dX = ((Number) exprs[dPos].getSingle(null)).floatValue();
				dY = ((Number) exprs[dPos + 1].getSingle(null)).floatValue();
				dZ = ((Number) exprs[dPos + 2].getSingle(null)).floatValue();
				speed = ((Number) exprs[dPos + 3].getSingle(null)).floatValue();
			}
		}
		
		return true;
	}
	
	public boolean isEntityEffect() {
		return type.effect instanceof EntityEffect;
	}
	
	@Nullable
	public final static VisualEffect parse(final String s) {
		final SyntaxElementInfo<VisualEffect> info = VisualEffect.info;
		if (info == null)
			return null;
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(s), new SingleItemIterator<SyntaxElementInfo<VisualEffect>>(info), null);
	}
	
	public void play(final @Nullable Player[] ps, final Location l, final @Nullable Entity e) {
		play(ps, l, e, 0, 32);
	}
	
	@SuppressWarnings({"deprecation"})
	public void play(final @Nullable Player[] ps, final Location l, final @Nullable Entity e, final int count, final int radius) {
		assert e == null || l.equals(e.getLocation());
		if (isEntityEffect()) {
			if (e != null)
				e.playEffect((EntityEffect) type.effect);
		} else {
			if (EFFECT_LIB && ((Effect) type.effect).getType() == Effect.Type.PARTICLE) { // Only particles for now
				ParticlesPlugin<?> plugin = ParticlesPlugin.plugin;
				assert plugin != null;
				plugin.playEffect(ps, l, count, radius, type, data, speed, dX, dY, dZ, color);
			} else {
				if (ps == null) {
					int id = 0;
					int dataId = 0;
					Object pData = type.getData(data, l);
					
					if (pData instanceof Material) {
						id = ((Material) pData).getId();
					} else if (pData instanceof MaterialData) {
						id = ((MaterialData) pData).getItemTypeId();
						dataId = ((MaterialData) pData).getData();
					}
					//Skript.info("dX: " + dX + " dY: " + dY + " dZ: " + dZ);
					
					l.getWorld().spigot().playEffect(l, (Effect) type.effect, id, dataId, dX, dY, dZ, speed, count, radius);
				} else {
					for (final Player p : ps)
						p.playEffect(l, (Effect) type.effect, type.getData(data, l));
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	public String toString(final int flags) {
		return names[type.ordinal()].toString(flags);
	}
	
	public static String getAllNames() {
		return StringUtils.join(names, ", ");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type.hashCode();
		final Object d = data;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VisualEffect))
			return false;
		final VisualEffect other = (VisualEffect) obj;
		if (type != other.type)
			return false;
		final Object d = data;
		if (d == null) {
			if (other.data != null)
				return false;
		} else if (!d.equals(other.data)) {
			return false;
		}
		return true;
	}
	
}
