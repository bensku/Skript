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
package ch.njol.skript.entity;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class HorseData extends EntityData<Horse> {
	
	static {
		//In 1.11 all these entities were arranged into their own classes rather than under Horse. See SimpleEntityData for those.
		if (Skript.classExists("org.bukkit.entity.Horse") && !Skript.isRunningMinecraft(1, 11)) {
			EntityData.register(HorseData.class, "horse", Horse.class, 0,
					"horse", "donkey", "mule", "undead horse", "skeleton horse");
			Variables.yggdrasil.registerSingleClass(Variant.class, "Horse.Variant");
			Variables.yggdrasil.registerSingleClass(Color.class, "Horse.Color");
			Variables.yggdrasil.registerSingleClass(Style.class, "Horse.Style");
		}
	}
	
	@Nullable
	private Variant variant;
	@Nullable
	private Color color;
	@Nullable
	private Style style;
	
	public HorseData() {}
	
	public HorseData(@Nullable Variant variant) {
		this.variant = variant;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		switch (matchedPattern) {
			case 0:
				variant = Variant.HORSE;
				break;
			case 1:
				variant = Variant.DONKEY;
				break;
			case 2:
				variant = Variant.MULE;
				break;
			case 3:
				variant = Variant.UNDEAD_HORSE;
				break;
			case 4:
				variant = Variant.SKELETON_HORSE;
				break;
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Horse> c, @Nullable Horse e) {
		if (e != null) {
			variant = e.getVariant();
			color = e.getColor();
			style = e.getStyle();
		}
		return true;
	}
	
	@Override
	protected boolean match(Horse entity) {
		return (variant == null || variant == entity.getVariant())
				&& (color == null || color == entity.getColor())
				&& (style == null || style == entity.getStyle());
	}
	
	@Override
	public EntityData getSuperType() {
		return new HorseData(variant);
	}
	
	@Override
	public void set(Horse entity) {
		if (variant != null)
			entity.setVariant(variant);
		if (color != null)
			entity.setColor(color);
		if (style != null)
			entity.setStyle(style);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof HorseData))
			return false;
		HorseData copy = (HorseData) e;
		return (variant == null || variant == copy.variant)
				&& (color == null || color == copy.color)
				&& (style == null || style == copy.style);
	}
	
	@Override
	public Class<? extends Horse> getType() {
		return Horse.class;
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		result = prime * result + (style != null ? style.hashCode() : 0);
		result = prime * result + (variant != null ? variant.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof HorseData))
			return false;
		HorseData copy = (HorseData) obj;
		if (color != copy.color)
			return false;
		if (style != copy.style)
			return false;
		if (variant != copy.variant)
			return false;
		return true;
	}
	
}
