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

import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;

public class LlamaData extends EntityData<Llama> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Llama")) {
			EntityData.register(LlamaData.class, "llama", Llama.class, 0,
					"llama", "white llama", "brown llama", "creamy llama", "gray llama");
			Variables.yggdrasil.registerSingleClass(Color.class, "Llama.Color");
		}
	}
	
	@Nullable
	private Color color;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color) {
		this.color = color;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		switch (matchedPattern) {
			case 0:
				break;
			case 1:
				color = Color.WHITE;
				break;
			case 2:
				color = Color.BROWN;
				break;
			case 3:
				color = Color.CREAMY;
				break;
			case 4:
				color = Color.GRAY;
				break;
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> clazz, @Nullable Llama entity) {
		if (entity != null)
			color = entity.getColor();
		return true;
	}
	
	@Override
	protected boolean match(Llama entity) {
		return (color == null || color == entity.getColor());
	}
	
	@Override
	public EntityData getSuperType() {
		return new LlamaData(color);
	}
	
	@Override
	public void set(Llama entity) {
		if (color != null)
			entity.setColor(color);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		return equals_i(data);
	}
	
	@Override
	public Class<? extends Llama> getType() {
		return Llama.class;
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof LlamaData))
			return false;
		LlamaData copy = (LlamaData) obj;
		if (color != copy.color)
			return false;
		return true;
	}
	
}
