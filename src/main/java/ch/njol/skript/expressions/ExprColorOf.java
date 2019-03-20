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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Colour of")
@Description("The <a href='../classes.html#color'>colour</a> of an item, can also be used to colour chat messages with \"&lt;%colour of ...%&gt;this text is coloured!\".")
@Examples({"on click on wool:",
		"	message \"This wool block is <%colour of block%>%colour of block%<reset>!\"",
		"	set the colour of the block to black"})
@Since("1.2")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	static {
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "itemstacks/entities/fireworkeffects");
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Color[] get(Event e, Object[] source) {
		if (source instanceof FireworkEffect[]) {
			List<Color> colors = new ArrayList<>();
			for (FireworkEffect effect : (FireworkEffect[])source) {
				effect.getColors().stream()
						.map(color -> SkriptColor.fromDyeColor(DyeColor.getByColor(color)))//TODO: Skript's color only supports 16 colors, not a plethora of RGB from fireworks.
						.filter(optional -> optional.isPresent())
						.forEach(colour -> colors.add(colour.get()));
			}
			if (colors.size() == 0)
				return null;
			return colors.toArray(new Color[colors.size()]);
		}
		return get(source, new Getter<Color, Object>() {
			@Override
			@Nullable
			public Color get(Object o) {
				if (o instanceof ItemStack || o instanceof Item) {
					final ItemStack is = o instanceof ItemStack ? (ItemStack) o : ((Item) o).getItemStack();
					final MaterialData d = is.getData();
					if (d instanceof Colorable) {
						Optional<SkriptColor> color = SkriptColor.fromDyeColor(((Colorable) d).getColor());
						if (!color.isPresent())
							return null;
						return color.get();
					}
				} else if (o instanceof Colorable) { // Sheep
					Optional<SkriptColor> color = SkriptColor.fromDyeColor(((Colorable) o).getColor());
					if (!color.isPresent())
						return null;
					return color.get();
				}
				return null;
			}
		});
	}
	
	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "colour of " + getExpr().toString(e, debug);
	}
	
	boolean changeItemStack;

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (FireworkEffect.class.isAssignableFrom(getExpr().getReturnType()))
			return CollectionUtils.array(Color[].class);
		if (mode != ChangeMode.SET)
			return null;
		if (Entity.class.isAssignableFrom(getExpr().getReturnType()))
			return CollectionUtils.array(Color.class);
		if (!getExpr().isSingle())
			return null;
		if (ChangerUtils.acceptsChange(getExpr(), mode, ItemStack.class, ItemType.class)) {
			changeItemStack = ChangerUtils.acceptsChange(getExpr(), mode, ItemStack.class);
			return CollectionUtils.array(Color.class);
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		
		Color c = (Color) delta[0];
		for (Object o : getExpr().getArray(e)) {
			if (o instanceof ItemStack || o instanceof Item) {
				ItemStack is = o instanceof ItemStack ? (ItemStack) o : ((Item) o).getItemStack();
				MaterialData d = is.getData();
				if (d instanceof Colorable)
					((Colorable) d).setColor(c.asDyeColor());
				else
					continue;
				
				if (o instanceof ItemStack) {
					if (changeItemStack)
						getExpr().change(e, new ItemStack[] {is}, mode);
					else
						getExpr().change(e, new ItemType[] {new ItemType(is)}, mode);
				} else {
					((Item) o).setItemStack(is);
				}
			} else if (o instanceof Colorable) {
				((Colorable) o).setColor(c.asDyeColor());
			} else if (o instanceof FireworkEffect) {
				Color[] input = (Color[])delta;
				FireworkEffect effect = ((FireworkEffect) o);
				switch (mode) {
					case ADD:
						for (Color color : input)
							effect.getColors().add(color.asBukkitColor());
						break;
					case REMOVE:
					case REMOVE_ALL:
						for (Color color : input)
							effect.getColors().remove(color.asBukkitColor());
						break;
					case DELETE:
					case RESET:
						effect.getColors().clear();
						break;
					case SET:
						effect.getColors().clear();
						for (Color color : input)
							effect.getColors().add(color.asBukkitColor());
						break;
					default:
						break;
				}
			}
		}
	}

}
