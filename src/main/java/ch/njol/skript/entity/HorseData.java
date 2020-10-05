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
@SuppressWarnings("deprecation") // Until 1.12: use old deprecated methods for backwards compatibility
public class HorseData extends EntityData<Horse> {
	static {
		if (Skript.classExists("org.bukkit.entity.Horse")) {
			if (!Skript.isRunningMinecraft(1, 11)) // For 1.11+ see SimpleEntityData
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
	
	public HorseData(final @Nullable Variant variant) {
		this.variant = variant;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		switch (matchedPattern) { // If Variant ordering is changed, will not break
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
	protected boolean init(final @Nullable Class<? extends Horse> c, final @Nullable Horse e) {
		if (e != null) {
			variant = e.getVariant();
			color = e.getColor();
			style = e.getStyle();
		}
		return true;
	}
	
	@Override
	protected boolean match(final Horse entity) {
		return (variant == null || variant == entity.getVariant())
				&& (color == null || color == entity.getColor())
				&& (style == null || style == entity.getStyle());
	}
	
	@Override
	public EntityData getSuperType() {
		return new HorseData(variant);
	}
	
	@Override
	public void set(final Horse entity) {
		if (variant != null)
			entity.setVariant(variant);
		if (color != null)
			entity.setColor(color);
		if (style != null)
			entity.setStyle(style);
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (!(e instanceof HorseData))
			return false;
		final HorseData d = (HorseData) e;
		return (variant == null || variant == d.variant)
				&& (color == null || color == d.color)
				&& (style == null || style == d.style);
	}
	
	@Override
	public Class<? extends Horse> getType() {
		return Horse.class;
	}
	
//		return (variant == null ? "" : variant.name()) + "," + (color == null ? "" : color.name()) + "," + (style == null ? "" : style.name());
	@Override
	protected boolean deserialize(final String s) {
		final String[] split = s.split(",");
		if (split.length != 3)
			return false;
		try {
			variant = split[0].isEmpty() ? null : Variant.valueOf(split[0]);
			color = split[1].isEmpty() ? null : Color.valueOf(split[1]);
			style = split[2].isEmpty() ? null : Style.valueOf(split[2]);
		} catch (final IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		result = prime * result + (style != null ? style.hashCode() : 0);
		result = prime * result + (variant != null ? variant.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof HorseData))
			return false;
		final HorseData other = (HorseData) obj;
		if (color != other.color)
			return false;
		if (style != other.style)
			return false;
		if (variant != other.variant)
			return false;
		return true;
	}
	
}
