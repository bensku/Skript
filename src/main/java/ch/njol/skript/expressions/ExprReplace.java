package tk.deltarays.skriptaddontest.elements;

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
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.regex.Pattern;

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text and returns the replaced text.")
@Examples({"on chat:", "\tsend replace all \"look\",\"up\" with \"watch\" in \"You should look up\" #You should watch watch"})
@Since("INSERT VERSION")
public class ExprReplace extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprReplace.class, String.class, ExpressionType.COMBINED, "replace (1¦first|0¦all|every|) %strings% with %string% in %string% [with case sensitivity]");
	}

	Expression<String> oldStrs;
	Expression<String> newStr;
	Expression<String> replaceStr;
	boolean caseSensitive = false;
	String type = "ALL";
	@Override
	public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		oldStrs = (Expression<String>) expressions[0];
		newStr = (Expression<String>) expressions[1];
		replaceStr = (Expression<String>) expressions[2];
		if (parseResult.mark == 1) type = "FIRST";
		if(parseResult.expr.endsWith("with case sensitivity")) caseSensitive = true;
		return true;
	}

	@Override
	protected String[] get(Event event) {
		Object[] oldText = Arrays.stream(oldStrs.getAll(event)).map((String he) -> (caseSensitive ? "": "(?i)") +Pattern.quote(he)).toArray();
		String newText = newStr.getSingle(event);
		String newtxt = replaceStr.getSingle(event);
		if (type == "FIRST") {
			for (Object s : oldText) {
				newtxt = newtxt.replaceFirst(s.toString(), newText);
			}
		} else if (type == "ALL") {
			for (Object s : oldText) {
				newtxt = newtxt.replaceAll(s.toString(), newText);
			}
		}
		return new String[]{newtxt};
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
	public String toString(Event event, boolean b) {
		return "";
	}


}
