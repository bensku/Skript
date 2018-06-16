/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.events;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
public class EvtExperienceSpawn extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Experience Spawn", EvtExperienceSpawn.class, ExperienceSpawnEvent.class, "[e]xp[erience] [orb] spawn", "spawn of [a[n]] [e]xp[erience] [orb]")
				.description("Called whenever experience is about to spawn. This is a helper event for easily being able to stop xp from spawning, as all you can currently do is cancel the event.",
						"Please note that it's impossible to detect xp orbs spawned by plugins (including Skript) with Bukkit, thus make sure that you have no such plugins if you don't want any xp orbs to spawn. " +
								"(Many plugins that only <i>change</i> the experience dropped by blocks or entities will be detected without problems though)")
				.examples("on xp spawn:",
						"	world is \"minigame_world\"",
						"	cancel event")
				.since("2.0");
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parseResult) {
		if (!Skript.isRunningMinecraft(1, 4, 5)) {
			Skript.error("The experience spawn event can only be used in Minecraft 1.4.5 and later");
			return false;
		}
		return true;
	}
	
	static Collection<Trigger> triggers = new ArrayList<>();
	
	@Override
	public void register(final Trigger t) {
		triggers.add(t);
		registerExecutor();
	}
	
	@Override
	public void unregister(final Trigger t) {
		triggers.remove(t);
	}
	
	@Override
	public void unregisterAll() {
		triggers.clear();
	}
	
	private static boolean registeredExecutor = false;
	
	@SuppressWarnings("unchecked")
	private static void registerExecutor() {
		if (registeredExecutor)
			return;
		for (final Class<? extends Event> c : new Class[] {BlockExpEvent.class, EntityDeathEvent.class, ExpBottleEvent.class, PlayerFishEvent.class})
			Bukkit.getPluginManager().registerEvent(c, new Listener() {}, SkriptConfig.defaultEventPriority.value(), executor, Skript.getInstance(), true);
	}
	
	private final static EventExecutor executor = new EventExecutor() {
		@SuppressWarnings("null")
		@Override
		public void execute(final @Nullable Listener listener, final @Nullable Event e) throws EventException {
			if (e == null)
				return;
			
			final ExperienceSpawnEvent es;
			if (e instanceof BlockExpEvent) {
				es = new ExperienceSpawnEvent(((BlockExpEvent) e).getExpToDrop(), ((BlockExpEvent) e).getBlock().getLocation().add(0.5, 0.5, 0.5));
			} else if (e instanceof EntityDeathEvent) {
				es = new ExperienceSpawnEvent(((EntityDeathEvent) e).getDroppedExp(), ((EntityDeathEvent) e).getEntity().getLocation());
			} else if (e instanceof ExpBottleEvent) {
				es = new ExperienceSpawnEvent(((ExpBottleEvent) e).getExperience(), ((ExpBottleEvent) e).getEntity().getLocation());
			} else if (e instanceof PlayerFishEvent) {
				es = new ExperienceSpawnEvent(((PlayerFishEvent) e).getExpToDrop(), ((PlayerFishEvent) e).getPlayer().getLocation());
			} else {
				assert false;
				return;
			}
			
			SkriptEventHandler.logEventStart(e);
			for (final Trigger t : triggers) {
				SkriptEventHandler.logTriggerStart(t);
				t.execute(es);
				SkriptEventHandler.logTriggerEnd(t);
			}
			SkriptEventHandler.logEventEnd();
			
			if (es.isCancelled())
				es.setSpawnedXP(0);
			if (e instanceof BlockExpEvent) {
				((BlockExpEvent) e).setExpToDrop(es.getSpawnedXP());
			} else if (e instanceof EntityDeathEvent) {
				((EntityDeathEvent) e).setDroppedExp(es.getSpawnedXP());
			} else if (e instanceof ExpBottleEvent) {
				((ExpBottleEvent) e).setExperience(es.getSpawnedXP());
			} else if (e instanceof PlayerFishEvent) {
				((PlayerFishEvent) e).setExpToDrop(es.getSpawnedXP());
			}
		}
	};
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "experience spawn";
	}
	
}
