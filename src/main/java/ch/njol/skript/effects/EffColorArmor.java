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

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

@Name("Colour Items")
@Description("Colours items in a given <a href='classes.html#color'>colour</a>. " +
		"You can also use RGB codes if you feel limited with the 16 default colours. " +
		"RGB codes are three numbers from 0 to 255 in the order <code>(red, green, blue)</code>, where <code>(0,0,0)</code> is black and <code>(255,255,255)</code> is white. " +
		"Armor is colourable for all Minecraft versions. With Minecraft 1.11 or newer you can also colour potions and maps. Note that the colours might not look exactly how you'd expect.")
@Examples({"dye player's helmet blue",
		"colour the player's tool red"})
@Since("2.0, 2.2-dev26 (maps and potions)")
public class EffColorArmor extends Effect {

	private static final boolean potionColors = Skript.isRunningMinecraft(1, 11);

	static {
		Skript.registerEffect(EffColorArmor.class,
				"(dye|colo[u]r|paint) %slots/itemstack% %color%",
				"(dye|colo[u]r|paint) %slots/itemstack% \\(%number%, %number%, %number%\\)");
	}

	@SuppressWarnings("null")
	private Expression<?> items;
	@Nullable
	private Expression<Color> color;
	@Nullable
	private Expression<Number>[] rgb;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		items = exprs[0];
		if (!Slot.class.isAssignableFrom(items.getReturnType()) && !ChangerUtils.acceptsChange(items, ChangeMode.SET, ItemStack.class)) {
			Skript.error(items + " cannot be coloured as it cannot be changed at all.");
			return false;
		}
		if (matchedPattern == 0) {
			color = (Expression<Color>) exprs[1];
		} else {
			rgb = new Expression[] {(Expression<Number>) exprs[1], (Expression<Number>) exprs[2], (Expression<Number>) exprs[3]};
		}
		return true;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<Color> color = this.color;
		if (color != null) {
			return "dye " + items.toString(e, debug) + " " + color.toString(e, debug);
		} else {
			final Expression<Number>[] rgb = this.rgb;
			assert rgb != null;
			return "dye " + items.toString(e, debug) + " (" + rgb[0].toString(e, debug) + "," + rgb[1].toString(e, debug) + "," + rgb[2].toString(e, debug) + ")";
		}
	}

	@Override
	protected void execute(Event e) {
		org.bukkit.Color color;
		if (this.color != null) {
			Color colorl = this.color.getSingle(e);
			if (colorl == null)
				return;
			color = colorl.asBukkitColor();
		} else {
			Expression<Number>[] rgb = this.rgb;
			assert rgb != null;
			Number r = rgb[0].getSingle(e), g = rgb[1].getSingle(e), b = rgb[2].getSingle(e);
			if (r == null || g == null || b == null)
				return;
			color = new ColorRGB(Math2.fit(0, r.intValue(), 255), Math2.fit(0, g.intValue(), 255), Math2.fit(0, b.intValue(), 255)).asBukkitColor();
		}
		
		for (Object object : items.getArray(e)) {
			if (object == null)
				continue;
			ItemStack item = object instanceof Slot ? ((Slot) object).getItem() : (ItemStack) object;
			if (item == null)
				continue;
			if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_LEGGINGS) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(color);
				item.setItemMeta(meta);
			} else if (potionColors) {
				if (item.getType() == Material.MAP) {
					MapMeta meta = (MapMeta) item.getItemMeta();
					meta.setColor(color);
					item.setItemMeta(meta);
				} else if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
					PotionMeta meta = (PotionMeta) item.getItemMeta();
					meta.setColor(color);
					item.setItemMeta(meta);
				}
			}
			if (object instanceof Slot) {
				((Slot) object).setItem(item);
			} else {
				items.change(e, new ItemStack[] {item}, ChangeMode.SET);
				return;
			}
		}
	}

}
