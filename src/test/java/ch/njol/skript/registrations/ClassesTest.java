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
package ch.njol.skript.registrations;

import static org.easymock.EasyMock.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.Before;
import org.junit.Test;
import org.objenesis.ObjenesisHelper;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.entity.CreeperData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.HorseData;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.entity.WolfData;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 */
public class ClassesTest {
	
	@SuppressWarnings({"resource", "deprecation"})
	@Before
	public void before() throws Exception {
		
		final File dataDir = new File("build/resources/");
		final File jar = new File("build/", "skript.jar");
		assumeTrue(jar.exists());
		
		final Logger l = Logger.getLogger(getClass().getCanonicalName());
		l.setParent(SkriptLogger.LOGGER);
		l.setLevel(Level.WARNING);
		
		final Server s = createMock(Server.class);
		s.getLogger();
		expectLastCall().andReturn(l).anyTimes();
		s.isPrimaryThread();
		expectLastCall().andReturn(true).anyTimes();
		s.getName();
		expectLastCall().andReturn("Whatever").anyTimes();
		s.getVersion();
		expectLastCall().andReturn("2.0").anyTimes();
		s.getBukkitVersion();
		expectLastCall().andReturn("2.0").anyTimes();
		replay(s);
		
		Bukkit.setServer(s);
		
		final Skript skript = (Skript) ObjenesisHelper.newInstance(Skript.class); // bypass the class loader check
		final Field instance = Skript.class.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(null, skript);
		
		final PluginDescriptionFile pdf = new PluginDescriptionFile(new FileInputStream(new File(dataDir, "plugin.yml")));
		
//	    final void init(PluginLoader loader, Server server, PluginDescriptionFile description, File dataFolder, File file, ClassLoader classLoader) {
		final Method init = JavaPlugin.class.getDeclaredMethod("init", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class);
		init.setAccessible(true);
		init.invoke(skript, new JavaPluginLoader(s), s, pdf, dataDir, jar, getClass().getClassLoader());
		
		Skript.getAddonInstance().loadClasses("ch.njol.skript", "entity");
		new JavaClasses();
		new BukkitClasses();
		new BukkitEventValues();
		new SkriptClasses();
		
		final Field r = Skript.class.getDeclaredField("acceptRegistrations");
		r.setAccessible(true);
		r.set(null, false);
		Classes.onRegistrationsStop();
	}
	
	@Test
	public void test() {
		final Object[] random = {
				// Java
				(byte) 127, (short) 2000, -1600000, 1L << 40, -1.5f, 13.37,
				"String",
				
				// Skript
				SkriptColor.BLACK, StructureType.RED_MUSHROOM, WeatherType.THUNDER,
				new Date(System.currentTimeMillis()), new Timespan(1337), new Time(12000), new Timeperiod(1000, 23000),
				new Experience(15), new Direction(0, Math.PI, 10), new Direction(new double[] {0, 1, 0}),
				new EntityType(new SimpleEntityData(HumanEntity.class), 300),
				new CreeperData(),
				new SimpleEntityData(Snowball.class),
				new HorseData(Variant.SKELETON_HORSE),
				new WolfData(),
				new XpOrbData(50),
				
				// Bukkit - simple classes only
				GameMode.ADVENTURE, InventoryType.CHEST, DamageCause.FALL,
				
				// there is also at least one variable for each class on my test server which are tested whenever the server shuts down.
		};
		
		for (final Object o : random) {
			Classes.serialize(o); // includes a deserialisation test
		}
	}
	
}
