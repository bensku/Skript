package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ExprBossBarWithId extends SimpleExpression<BossBar> {
	
	static {
		if (Skript.classExists("org.bukkit.boss.KeyedBossBar"))
			Skript.registerExpression(ExprBossBarWithId.class, BossBar.class, ExpressionType.COMBINED, "[boss[ ]]bar (from|with) id %string%");
	}
	@SuppressWarnings("null")
	private Expression<String> id;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		id = (Expression<String>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected KeyedBossBar[] get(Event e) {
		String id = this.id.getSingle(e);
		if (id != null)
			return CollectionUtils.array(Bukkit.getBossBar(NamespacedKey.minecraft(id)));
		else
			return null;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends BossBar> getReturnType() {
		return KeyedBossBar.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "bossbar with id " + id.toString(e, debug);
	}
	
}
