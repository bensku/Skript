package ch.njol.skript.conditions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Server Is Whitelisted")
@Description("Whether or not the server is whitelisted")
@Examples("if server is whitelisted")
@Since("INSERT VERSION")
public class CondServerIsWhitelisted extends Condition {
	
	static {
		Skript.registerCondition(CondServerIsWhitelisted.class, "server (is|1¦is(n't| not)) white[ ]listed");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.mark != 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return Bukkit.hasWhitelist() == isNegated();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "server is whitelisted";
	}
	
}
