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
package ch.njol.skript.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Damage/Heal/Repair")
@Description("Damage/Heal/Repair an entity, or item.")
@Examples({"damage player by 5 hearts",
		"heal the player",
		"repair tool of player"})
@Since("1.0")
public class EffHealth extends Effect {
	
	static {
		Skript.registerEffect(EffHealth.class,
				"damage %livingentities/itemtypes% by %number% [heart[s]][ with fake cause %-damagecause%]",
				"heal %livingentities% [by %-number% [heart[s]]]",
				"repair %itemtypes% [by %-number%]");
	}
	
	@SuppressWarnings("null")
	private Expression<?> damageables;
	@Nullable
	private Expression<Number> damage;
	private boolean heal = false;
	@Nullable
	private Expression<DamageCause> dmgCause;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		damageables = vars[0];
		if (ItemStack.class.isAssignableFrom(damageables.getReturnType())) {
			if (!ChangerUtils.acceptsChange(damageables, ChangeMode.SET, ItemType.class)) {
				Skript.error(damageables + " cannot be changed, thus it cannot be damaged or repaired.");
				return false;
			}
		}
		damage = (Expression<Number>) vars[1];
		heal = (matchedPattern >= 1);
		
		if (vars.length >= 3) dmgCause = (Expression<DamageCause>) vars[2];
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		double damage = 0;
		if (this.damage != null) {
			final Number n = this.damage.getSingle(e);
			if (n == null)
				return;
			damage = n.doubleValue();
		}
		Object[] arr = damageables.getArray(e);
		if(arr.length > 0 && arr[0] instanceof ItemType) {
			ItemType[] newarr = new ItemType[arr.length];
			for (int i = 0; i < arr.length; i++) {
				ItemStack is = ((ItemType) arr[i]).getRandom();
				assert is != null;
				if (this.damage == null) {
					ItemUtils.setDamage(is, 0);
				} else {
					ItemUtils.setDamage(is, (int) Math2.fit(0, ItemUtils.getDamage(is) + (heal ? -damage : damage), is.getType().getMaxDurability()));
				}
				newarr[i] = new ItemType(is);
			}
			
			// Set changed item back to source
			// We KNOW this is supported, but have to check anyway to prepare SimpleExpression for change
			damageables.acceptChange(ChangeMode.SET);
			damageables.change(e, newarr, ChangeMode.SET);
		} else {
			for (final Object damageable : arr) {
				LivingEntity entity = (LivingEntity) damageable;
				assert entity != null;
				if (this.damage == null) {
					HealthUtils.setHealth(entity, HealthUtils.getMaxHealth(entity));
				} else {
					HealthUtils.heal(entity, (heal ? 1 : -1) * damage);
					if (!heal) {
						DamageCause cause = DamageCause.CUSTOM;
						if (dmgCause != null) cause = dmgCause.getSingle(e);
						assert cause != null;
						HealthUtils.setDamageCause(entity, cause);
					}
				}
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (heal ? "heal " : "damage ") + damageables.toString(e, debug) + (damage != null ? " by " + damage.toString(e, debug) : "");
	}
	
}
