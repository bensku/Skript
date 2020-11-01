/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Potion Effects")
@Description({"Represents the active potion effects of entities and itemtypes.",
	"You can clear all potion effects of an entity/itemtype, add a potion effect to an entity/itemtype, and remove a",
	"potion effect or potion effect type from an entity/itemtype. Do note you will not be able to clear the base potion effects ",
	"of a potion item. In that case, just switch the item to a water bottle."})
@Examples({"set {_p::*} to active potion effects of player",
	"clear all the potion effects of player",
	"clear all the potion effects of player's tool",
	"add potion effects of player to potion effects of player's tool",
	"remove speed and night vision from potion effects of player"})
@Since("INSERT VERSION")
public class ExprPotionEffects extends SimpleExpression<PotionEffect> {
	
	static {
		PropertyExpression.register(ExprPotionEffects.class, PotionEffect.class,
			"[(all [[of] the]|the)] [active] potion effects", "livingentities/itemtypes");
	}
	
	@SuppressWarnings("null")
	private Expression<Object> objects;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		objects = (Expression<Object>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected PotionEffect[] get(Event e) {
		List<PotionEffect> effects = new ArrayList<>();
		for (Object object : this.objects.getArray(e)) {
			if (object instanceof LivingEntity)
				effects.addAll(((LivingEntity) object).getActivePotionEffects());
			else if (object instanceof ItemType)
				effects.addAll(PotionEffectUtils.getEffects((ItemType) object));
		}
		return effects.toArray(new PotionEffect[0]);
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
				return CollectionUtils.array(PotionEffect[].class, PotionEffectType[].class);
			case ADD:
			case DELETE:
				return CollectionUtils.array(PotionEffect[].class);
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		for (Object object : this.objects.getArray(e)) {
			switch (mode) {
				case DELETE:
					if (object instanceof LivingEntity)
						PotionEffectUtils.clearAllEffects((LivingEntity) object);
					else if (object instanceof ItemType)
						PotionEffectUtils.clearAllEffects((ItemType) object);
					break;
				case ADD:
					if (delta == null)
						return;
					if (object instanceof LivingEntity)
						PotionEffectUtils.addEffects(((LivingEntity) object), (PotionEffect[]) delta);
					else if (object instanceof ItemType)
						PotionEffectUtils.addEffects(((ItemType) object), (PotionEffect[]) delta);
					
					break;
				case REMOVE:
					if (delta == null)
						return;
					for (Object o : delta) {
						PotionEffectType type = null;
						if (o instanceof PotionEffectType)
							type = ((PotionEffectType) o);
						else if (o instanceof PotionEffect)
							type = ((PotionEffect) o).getType();
						if (type != null)
							if (object instanceof LivingEntity)
								PotionEffectUtils.removeEffect((LivingEntity) object, type);
							else if (object instanceof ItemType)
								PotionEffectUtils.removeEffect((ItemType) object, type);
					}
			}
		}
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends PotionEffect> getReturnType() {
		return PotionEffect.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "active potion effects of " + objects.toString(e, d);
	}
	
}
