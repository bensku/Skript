package ch.njol.skript.conditions;

import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
@Name("Projectile Is Critical")
@Description("Whether or not a projectile is critical. The only currently accepted projectiles are arrows and tridents.")
@Examples({"on shoot:",
	"\tprojectile is an arrow",
	"\tsend \"Critical!\" if projectile is critical"})
public class CondProjectileIsCritical extends Condition {
	
	static {
		Skript.registerCondition(CondProjectileIsCritical.class, "%projectiles (is|are)(1¦(n't| not))");
	}
	Expression<Projectile> projectiles;
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		projectiles = (Expression<Projectile>) exprs[0];
		setNegated(parseResult.mark != 1);
		return false;
	}
	
	@Override
	public boolean check(Event e) {
		return false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return null;
	}
	
}
