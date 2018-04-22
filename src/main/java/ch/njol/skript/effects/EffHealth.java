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
import ch.njol.skript.bukkitutil.HealthUtils;
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

/**
 * @author Peter Güttinger
 */
@Name("Damage/Heal/Repair")
@Description("Damage/Heal/Repair an entity, or item stack.")
@Examples({"damage player by 5 hearts",
		"heal the player",
		"repair tool of player"})
@Since("1.0")
public class EffHealth extends Effect {
	
	static {
		Skript.registerEffect(EffHealth.class,
				"damage %slots/livingentities/itemstack% by %number% [heart[s]][ with fake cause %-damagecause%]",
				"heal %livingentities% [by %-number% [heart[s]]]",
				"repair %slots/itemstack% [by %-number%]");
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
			if (!ChangerUtils.acceptsChange(damageables, ChangeMode.SET, ItemStack.class)) {
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
		if (ItemStack.class.isAssignableFrom(damageables.getReturnType())) {
			ItemStack i = (ItemStack) damageables.getSingle(e);
			if (i == null)
				return;
			if (this.damage == null) {
				i.setDurability((short) 0);
			} else {
				i.setDurability((short) Math.max(0, i.getDurability() + (heal ? -damage : damage)));
				if (i.getDurability() >= i.getType().getMaxDurability())
					i = null;
			}
			damageables.change(e, new ItemStack[] {i}, ChangeMode.SET);
			return;
		}
		for (final Object damageable : damageables.getArray(e)) {
			if (damageable instanceof Slot) {
				ItemStack is = ((Slot) damageable).getItem();
				if (is == null)
					continue;
				if (this.damage == null) {
					is.setDurability((short) 0);
				} else {
					is.setDurability((short) Math.max(0, is.getDurability() + (heal ? -damage : damage)));
					if (is.getDurability() >= is.getType().getMaxDurability())
						is = null;
				}
				((Slot) damageable).setItem(is);
			} else if (damageable instanceof LivingEntity) {
				if (this.damage == null) {
					HealthUtils.setHealth((LivingEntity) damageable, HealthUtils.getMaxHealth((LivingEntity) damageable));
				} else {
					HealthUtils.heal((LivingEntity) damageable, (heal ? 1 : -1) * damage);
					
					if (!heal) {
						DamageCause cause = DamageCause.CUSTOM;
						if (dmgCause != null) cause = dmgCause.getSingle(e);
						assert cause != null;
						HealthUtils.setDamageCause((LivingEntity) damageable, cause);
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
