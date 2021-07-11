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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.eclipse.jdt.annotation.Nullable;

@Name("Potion Effects")
@Description("Apply potion effects to/from entities.")
@Examples({
		"apply swiftness 2 to the player",
		"on join:",
		"\tapply potion of strength of tier {strength.%player%} to the player for 999 days",
		"apply potion effects of player's tool to player"
})
@Since("2.0, 2.2-dev27 (ambient and particle-less potion effects), 2.5 (replacing existing effect), 2.5.2 (potion effects), INSERT VERSION (total rework)")
public class EffPotion extends Effect {

	static {
		// While allowing the user to specify the timespan here is repetitive as you can do it in ExprPotionEffect,
		// it allows syntax like "apply haste 3 to the player for 5 seconds" to work
		Skript.registerEffect(EffPotion.class,
				"apply %potioneffects% to %livingentities% [for %-timespan%]",
				"effect %livingentities% with %potioneffects% [for %-timespan%]"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<PotionEffect> potionEffects;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	@Nullable
	private Expression<Timespan> duration;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean first = matchedPattern == 0;
		potionEffects = (Expression<PotionEffect>) exprs[first ? 0 : 1];
		entities = (Expression<LivingEntity>) exprs[first ? 1 : 0];
		duration = (Expression<Timespan>) exprs[2];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		PotionEffect[] potionEffects = this.potionEffects.getArray(e);

		// Change duration for some backwards compatibility with older Skript versions
		if (duration != null) {
			Timespan timespan = duration.getSingle(e);
			if (timespan != null) {
				int ticks = (int) timespan.getTicks_i();
				for (int i = 0; i < potionEffects.length; i++)
					potionEffects[i] = potionEffects[i].withDuration(ticks);
			}
		}

		for (LivingEntity livingEntity : entities.getArray(e)) {
			PotionEffectUtils.addEffects(livingEntity, potionEffects);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "apply " + potionEffects.toString(e, debug) + " to " + entities.toString(e, debug);
	}
	
}
