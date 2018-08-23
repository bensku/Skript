package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

@Name("Leash entities")
@Description("Leash living entities to other entities")
@Examples("leash the player to target entity")
@Since("INSERT VERSION")
public class EffLeash extends Effect {

	static {
		Skript.registerEffect(EffLeash.class, "(leash|lead) %livingentities% to %entities%", "make %entities% (leash|lead) %livingentities%");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> holders;
	@SuppressWarnings("null")
	private Expression<LivingEntity> targets;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		holders = (Expression<Entity>) expressions[1 - matchedPattern];
		targets = (Expression<LivingEntity>) expressions[matchedPattern];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (Entity holder : holders.getArray(event)) {
			for (LivingEntity target : targets.getArray(event)) {
				target.setLeashHolder(holder);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "leash " + targets.toString(event, debug) + " to " + holders.toString(event, debug);
	}

}
