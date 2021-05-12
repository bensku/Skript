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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.tests.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

import ch.njol.skript.tests.TestResults;

/**
 * Test environment information.
 */
public class Environment {
	
	/**
	 * Name of this environment. For example, spigot-1.14.
	 */
	private final String name;
	
	/**
	 * Resource that needs to be downloaded for the environment.
	 */
	public static class Resource {
		
		/**
		 * Where to get this resource.
		 */
		private String source;
		
		/**
		 * Path under platform root where it should be placed.
		 * Directories created as needed.
		 */
		private String target;

		public Resource(String url, String target) {
			this.source = url;
			this.target = target;
		}
		
		public String getSource() {
			return source;
		}

		
		public String getTarget() {
			return target;
		}
		
	}
	
	/**
	 * Resources that need to be copied.
	 */
	private final List<Resource> resources;
	
	/**
	 * Resources that need to be downloaded.
	 */
	private final List<Resource> downloads;
	
	/**
	 * Where Skript should be placed under platform root.
	 * Directories created as needed.
	 */
	private final String skriptTarget;
	
	/**
	 * Added after platform's own JVM flags.
	 */
	private final String[] commandLine;
	
	public Environment(String name, List<Resource> resources, List<Resource> downloads, String skriptTarget, String... commandLine) {
		this.name = name;
		this.resources = resources;
		this.downloads = downloads;
		this.skriptTarget = skriptTarget;
		this.commandLine = commandLine;
	}
	
	public String getName() {
		return name;
	}

	public void initialize(Path dataRoot, Path runnerRoot, boolean remake) throws IOException {
		Path env = runnerRoot.resolve(name);
		boolean onlyCopySkript = Files.exists(env) && !remake;
		
		// Copy Skript to platform
		Path skript = env.resolve(skriptTarget);
		Files.createDirectories(skript.getParent());
		try {
			Files.copy(new File(getClass().getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).toPath(), skript, StandardCopyOption.REPLACE_EXISTING);
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
		
		if (onlyCopySkript) {
			return;
		}
		
		// Copy resources
		for (Resource resource : resources) {
			Path source = dataRoot.resolve(resource.getSource());
			Path target = env.resolve(resource.getTarget());
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
		
		// Download additional resources
		for (Resource resource : downloads) {
			assert resource != null;
			URL url = new URL(resource.getSource());
			Path target = env.resolve(resource.getTarget());
			Files.createDirectories(target.getParent());
			try (InputStream is = url.openStream()) {
				Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
	
	public TestResults runTests(Path runnerRoot, Path testsRoot, boolean devMode, String... jvmArgs) throws IOException, InterruptedException {
		Path env = runnerRoot.resolve(name);
		List<String> args = new ArrayList<>();
		args.add("java");
		args.add("-ea");
		args.add("-Dskript.testing.enabled=true");
		args.add("-Dskript.testing.dir=" + testsRoot);
		args.add("-Dskript.testing.devMode=" + devMode);
		args.add("-Dskript.testing.results=test_results.json");
		args.addAll(Arrays.asList(jvmArgs));
		args.addAll(Arrays.asList(commandLine));
		
		Process process = new ProcessBuilder(args)
				.directory(env.toFile())
				.redirectOutput(Redirect.INHERIT)
				.redirectError(Redirect.INHERIT)
				.redirectInput(Redirect.INHERIT)
				.start();
		
		// When we exit, try to make them exit too
		Runtime.getRuntime().addShutdownHook(new Thread(() ->  {
			if (process.isAlive()) {
				process.destroy();
			}
		}));
		
		// Catch tests running for abnormally long time
		if (!devMode) {
			new Timer("runner watchdog", true).schedule(new TimerTask() {
				
				@Override
				public void run() {
					if (process.isAlive()) {
						System.err.println("Test environment is taking too long, failing...");
						System.exit(1);
					}
				}
			}, 8 * 60_000);
		}
		
		int code = process.waitFor();
		if (code != 0) {
			throw new IOException("environment returned with code " + code);
		}
		
		// Read test results
		TestResults results = new Gson().fromJson(new String(Files.readAllBytes(env.resolve("test_results.json"))), TestResults.class);
		assert results != null;
		return results;
	}
	
}
