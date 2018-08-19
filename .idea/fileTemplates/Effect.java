#parse("License Header.java")
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("${Effect_Name}")
@Description("${Description}")
@Examples("")
@Since("2.2-dev37")
public class Eff${NAME} extends Effect {
	
	static {
		Skript.registerEffect(Eff${NAME}.class, "");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
	
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "";
	}
}
