package ch.njol.skript.expressions;


import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.util.Kleenean;

@Name("Fertilized blocks")
@Description("The blocks fertilized in block fertilize events.")
@Events("block fertilize")
@Examples("the fertilized blocks")
@Since("2.5")
public class ExprFertilizedBlocks extends SimpleExpression<BlockStateBlock> {
	
	static {
		if (Skript.classExists("org.bukkit.event.block.BlockFertilizeEvent"))
			Skript.registerExpression(ExprFertilizedBlocks.class, BlockStateBlock.class, ExpressionType.SIMPLE, "[all] [the] fertilized blocks");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(BlockFertilizeEvent.class)) {
			Skript.error("The 'fertilized blocks' are only usable in block fertilize events");
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected BlockStateBlock[] get(Event e) {
		return
			((BlockFertilizeEvent) e).getBlocks().stream().map(BlockStateBlock::new).toArray(BlockStateBlock[]::new);
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends BlockStateBlock> getReturnType() {
		return BlockStateBlock.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "fertilized blocks";
	}
	
}
