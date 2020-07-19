package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Unknown command message")
@Description({"The message sent to the player in an unknown command event"})
@Examples({"on unknown command:", "\tset unknown command message to \"Error: Command not found!\""})
@Since("INSERT VERSION")
public class ExprUnknownCommandMessage extends SimpleExpression<String> {
	static {
		if (Skript.classExists("org.bukkit.event.command.UnknownCommandEvent") && Skript.methodExists(UnknownCommandEvent.class, "getMessage"))
			Skript.registerExpression(ExprUnknownCommandMessage.class, String.class, ExpressionType.SIMPLE, "[the] (unknown command message|unknown cmd message)");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(UnknownCommandEvent.class)) {
			Skript.error("The 'unknown command message' expression can only be used in a unknown command event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected String[] get(Event e) {
		return new String[]{((UnknownCommandEvent) e).getMessage()};
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		UnknownCommandEvent event = (UnknownCommandEvent) e;
		switch (mode) {
			case SET:
				assert delta != null;
				event.setMessage((String) delta[0]);
				break;
			default:
				assert false;
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET) ? CollectionUtils.array(String.class) : null;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the unknown command message";
	}
	
}
