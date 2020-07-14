package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
@Name("Repeated")
@Description("A string repeated a number times")
@Examples({"on chat:", "\tset {_t} to message repeated 3 times", "\tsend {_t}"})
@Since("INSERT VERSION")
public class ExprRepeated extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprRepeated.class, String.class, ExpressionType.COMBINED, "%string% repeat[ed] %number% time[s]", "repeat[ed] %string% %number% time[s]");
	}
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		text = (Expression<String>) exprs[0];
		times = (Expression<Long>) exprs[1];
		return true;
	}
	@SuppressWarnings("null")
	private Expression<String> text;
	@SuppressWarnings("null")
	private Expression<Long> times;
	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event e) {
		assert times != null && text != null;
		String string = text.getSingle(e);
		StringBuilder builder = new StringBuilder();
		for(int x = 0; x < times.getSingle(e); x++){
			builder.append(string);
		}
		return new String[]{builder.toString()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "String \""+text+"\" repeated "+times+" times";
	}

}