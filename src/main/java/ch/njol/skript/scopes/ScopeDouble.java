package ch.njol.skript.scopes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Created by ARTHUR on 21/03/2017.
 */
@SuppressWarnings("unused")
public class ScopeDouble extends TriggerSection {

    static {
        Skript.registerScope(
                ScopeDouble.class,
                "double[d]"
        );
    }
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event e) {
        walk(walk(e, true), e);
        return getNext();
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return getClass().getName();
    }
}
