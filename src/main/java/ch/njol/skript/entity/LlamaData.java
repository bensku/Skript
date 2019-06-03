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
import org.bukkit.entity.TraderLlama;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;

public class LlamaData extends EntityData<Llama> {
	
	private final static boolean TRADER_SUPPORT = Skript.isRunningMinecraft(1, 14);
	static {
		if (TRADER_SUPPORT)
			EntityData.register(LlamaData.class, "llama", Llama.class, 0,
					"llama", "trader llama", "creamy llama",
					"white llama", "brown llama", "gray llama");
		else if (Skript.isRunningMinecraft(1, 11))
			EntityData.register(LlamaData.class, "llama", Llama.class, 0,
					"llama", "creamy llama",
					"white llama", "brown llama", "gray llama");
	}
	
	@Nullable
	private Color color = null;
	private boolean isTrader;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isTrader = (TRADER_SUPPORT && matchedPattern == 1) || Utils.isEither(parseResult.mark, 2, 3, 6, 7);
		
		if (!TRADER_SUPPORT && isTrader)
			return false;
		if (matchedPattern > (TRADER_SUPPORT ? 1 : 0))
			color = Color.values()[matchedPattern - (TRADER_SUPPORT ? 2 : 1)];
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> c, @Nullable Llama llama) {
		if (TRADER_SUPPORT && c != null)
			isTrader = c.isAssignableFrom(TraderLlama.class);
		if (llama != null)
			color = llama.getColor();
		return true;
	}
	
	@Override
	public void set(Llama entity) {
		Color randomColor = color == null ? CollectionUtils.getRandom(Color.values()) : color;
		assert randomColor != null;
		entity.setColor(randomColor);
	}
	
	@Override
	protected boolean match(Llama entity) {
		return color == null || color == entity.getColor() || (TRADER_SUPPORT && isTrader == entity instanceof TraderLlama);
	}
	
	@Override
	public Class<? extends Llama> getType() {
		return isTrader ? Llama.class : TraderLlama.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new LlamaData();
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		result = prime * result + (isTrader ? 1 : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof LlamaData))
			return false;
		LlamaData d = (LlamaData) data;
		return isTrader == d.isTrader && d.color == color;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof LlamaData))
			return false;
		LlamaData d = (LlamaData) data;
		return isTrader != d.isTrader && (color == null || d.color == color);
	}
	
}
