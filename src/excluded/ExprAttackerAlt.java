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

package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Converter.ConverterUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.DefaultChangers;
import ch.njol.skript.expressions.ExprAttackerAlt.Attacker;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ItemType;

/**
 * 
 * @author Peter Güttinger
 */
public class ExprAttackerAlt extends SimpleExpression<Attacker> {
	
	public static final class Attacker {
		private final Object attacker;
		
		public Attacker(final Object attacker) {
			this.attacker = attacker;
		}
		
		public Object getAttacker() {
			return attacker;
		}
		
		@Override
		public String toString() {
			return Skript.toString(attacker);
		}
		
		public Inventory getInventory() {
			if (getAttacker() instanceof Block) {
				final BlockState state = ((Block) getAttacker()).getState();
				if (state instanceof InventoryHolder)
					return ((InventoryHolder) state).getInventory();
			} else if (getAttacker() instanceof InventoryHolder) {
				return ((InventoryHolder) getAttacker()).getInventory();
			}
			return null;
		}
	}
	
	static {
		Skript.registerExpression(ExprAttackerAlt.class, Attacker.class, ExpressionType.SIMPLE, "[the] (attacker|damager)");
		Skript.registerClass(new ClassInfo<Attacker>(Attacker.class, "attacker", "attacker")
				.changer(new Changer<Attacker, ItemType[]>() {
					@Override
					public void change(final Attacker[] what, final ItemType[] delta, final ChangeMode mode) {
						DefaultChangers.inventoryChanger.change(ConverterUtils.convert(what, new Converter<Attacker, Inventory>() {
							@Override
							public Inventory convert(final Attacker a) {
								return a.getInventory();
							}
						}, Inventory.class), delta, mode);
					}
					
					@Override
					public Class<? extends ItemType[]> acceptChange(final ChangeMode mode) {
						return DefaultChangers.inventoryChanger.acceptChange(mode);
					}
				}));
		Skript.registerConverter(Attacker.class, Entity.class, new Converter<Attacker, Entity>() {
			@Override
			public Entity convert(final Attacker a) {
				if (a.getAttacker() instanceof Entity)
					return (Entity) a.getAttacker();
				return null;
			}
		});
		Skript.registerConverter(Attacker.class, Block.class, new Converter<Attacker, Block>() {
			@Override
			public Block convert(final Attacker a) {
				if (a.getAttacker() instanceof Block)
					return (Block) a.getAttacker();
				return null;
			}
		});
		Skript.registerConverter(Attacker.class, Inventory.class, new Converter<Attacker, Inventory>() {
			@Override
			public Inventory convert(final Attacker a) {
				return a.getInventory();
			}
		});
		Skript.registerConverter(Attacker.class, Location.class, new Converter<Attacker, Location>() {
			@Override
			public Location convert(final Attacker a) {
				if (a.getAttacker() instanceof Block)
					return ((Block) a.getAttacker()).getLocation().add(0.5, 0.5, 0.5);
				return ((Entity) a.getAttacker()).getLocation();
			}
		});
	}
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	protected Attacker[] get(final Event e) {
		return new Attacker[] {new Attacker(getAttacker(e))};
	}
	
	private static Object getAttacker(final Event e) {
		if (e instanceof EntityDamageByEntityEvent) {
			if (((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile) {
				return ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
			}
			return ((EntityDamageByEntityEvent) e).getDamager();
		} else if (e instanceof EntityDamageByBlockEvent) {
			return ((EntityDamageByBlockEvent) e).getDamager();
		} else if (e instanceof EntityDeathEvent) {
			return getAttacker(((EntityDeathEvent) e).getEntity().getLastDamageCause());
		} else if (e instanceof VehicleDamageEvent) {
			return ((VehicleDamageEvent) e).getAttacker();
		} else if (e instanceof VehicleDestroyEvent) {
			return ((VehicleDestroyEvent) e).getAttacker();
		}
		return null;
	}
	
	@Override
	public Class<? extends Attacker> getReturnType() {
		return Attacker.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the attacker";
		return Skript.getDebugMessage(getSingle(e) == null ? null : getSingle(e).attacker);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
