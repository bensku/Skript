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

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Item with Potion Effects")
@Description("Get an item with potion effects. Supports potions, splash potions, lingering potions, tipped arrows, and suspicious stew.")
@Examples({"give player potion with blindness and nausea of tier 3 with duration 10 minutes",
	"set {_i} to splash potion with health", "set player's tool to a tipped arrow with slowness of tier 3 with duration 5 minutes"})
@RequiredPlugins("1.14.4+ for Suspicious Stew")
@Since("INSERT VERSION")
public class ExprItemWithPotionEffect extends SimpleExpression<ItemType> {
	
	static {
		if (Skript.classExists("org.bukkit.inventory.meta.SuspiciousStewMeta")) {
			Skript.registerExpression(ExprItemWithPotionEffect.class, ItemType.class, ExpressionType.PROPERTY,
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] [with duration %-timespan%]",
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with ambient [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] [with duration %-timespan%]",
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] without [any] particles [with duration %-timespan%]",
				"[a] (4¦suspicious) stew with [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] [with duration %-timespan%]");
		} else {
			Skript.registerExpression(ExprItemWithPotionEffect.class, ItemType.class, ExpressionType.PROPERTY,
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] [with duration %-timespan%]",
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with ambient [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] [with duration %-timespan%]",
				"[a] (0¦potion|1¦splash potion|2¦lingering potion|3¦tipped arrow) with [potion [effect]] %potioneffecttypes% [potion [effect]] [[[of] tier] %-number%] without [any] particles [with duration %-timespan%]");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<PotionEffectType> potions;
	@Nullable
	private Expression<Number> tier;
	@Nullable
	private Expression<Timespan> duration;
	private boolean ambient;
	private boolean particles;
	private int potion_type;
	
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		potions = (Expression<PotionEffectType>) exprs[0];
		tier = (Expression<Number>) exprs[1];
		duration = (Expression<Timespan>) exprs[2];
		ambient = matchedPattern == 2;
		particles = matchedPattern != 3;
		potion_type = parseResult.mark;
		return true;
	}
	
	@Nullable
	@Override
	protected ItemType[] get(Event e) {
		ItemType item;
		int a = 0;
		if (tier != null) {
			final Number amp = tier.getSingle(e);
			if (amp != null)
				a = amp.intValue() - 1;
		}
		int ticks = 15 * 20;
		if (duration != null) {
			final Timespan dur = duration.getSingle(e);
			if (dur != null)
				ticks = (int) (dur.getTicks_i() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : dur.getTicks_i());
		}
		String name = null;
		switch (potion_type) {
			case 1:
				item = Aliases.javaItemType("splash potion");
				name = "Splash Potion";
				break;
			case 2:
				item = Aliases.javaItemType("lingering potion");
				name = "Lingering Potion";
				break;
			case 3:
				item = Aliases.javaItemType("tipped arrow");
				name = "Tipped Arrow";
				break;
			case 4:
				item = Aliases.javaItemType("suspicious stew");
				break;
			default:
				item = Aliases.javaItemType("potion");
				name = "Potion";
		}
		ItemMeta meta = item.getItemMeta();
		for (PotionEffectType potionEffectType : potions.getArray(e)) {
			if (potion_type == 4) {
				((SuspiciousStewMeta) meta).addCustomEffect(new PotionEffect(potionEffectType, ticks, 0), true);
			} else {
				((PotionMeta) meta).addCustomEffect(new PotionEffect(potionEffectType, ticks, a, ambient, particles), true);
			}
		}
		if (name != null)
			meta.setDisplayName(ChatColor.RESET + name);
		item.setItemMeta(meta);
		return new ItemType[]{item};
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
	public String toString(@Nullable Event e, boolean d) {
		String type;
		switch (potion_type) {
			case 1:
				type = "Splash Potion";
				break;
			case 2:
				type = "Lingering Potion";
				break;
			case 3:
				type = "Tipped Arrow";
				break;
			case 4:
				type = "Suspicious Stew";
				break;
			default:
				type = "Potion";
		}
		return type + " with " + potions.toString(e, d) + (ambient ? " ambient " : "") +
			(tier != null ? " of tier " + tier.toString(e, d) : "") + (particles ? " without particles " : "") +
			(duration != null ? " with duration " + duration.toString(e, d) : "");
	}
	
}
