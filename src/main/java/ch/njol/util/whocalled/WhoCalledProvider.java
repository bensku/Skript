/**
 * Copyright (c) 2014 nallar (Ross Allan)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.njol.util.whocalled;

class WhoCalledProvider {
	private static final String[] whoCalledImplementations = {
		"WhoCalledReflection",
		"WhoCalledSecurityManager",
		"WhoCalledStackTrace",
	};
	
	/*
	For performance reasons, we list the first java version an implementation will not work on.
	By avoiding loading more than one implementation of the WhoCalled interface, the jit can produce slightly faster
	devirtualized calls. We never get a virtual call when both classes are loaded, but a very slight overhead is added
	Instead of non-virtual call, we get non-virtual call with check that class matches.

	This is discussed in detail in the following talk:
	JVM Mechanics - Silicon Valley JUG 2015 at https://www.youtube.com/watch?v=E9i9NJeXGmM
	 */
	private static final String[] brokenJavaVersion = {
		"1.7.0.25",
		null,
		null
	};
	
	static WhoCalled getWhoCalled() {
		assert brokenJavaVersion.length == whoCalledImplementations.length;
		
		String javaVersion = System.getProperty("java.version").replace('_', '.');
		
		Throwable lastError = null;
		for (int i = 0; i < whoCalledImplementations.length; i++) {
			String name = whoCalledImplementations[i];
			String brokenVersion = brokenJavaVersion[i];
			
			try {
				if (brokenVersion == null || versionCompare(javaVersion, brokenVersion) < 0)
					return tryWhoCalled(name);
			} catch (Throwable e) {
				lastError = e;
			}
		}
		
		throw new Error("Failed to set up any WhoCalled implementation", lastError);
	}
	
	private static WhoCalled tryWhoCalled(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		String className = WhoCalledProvider.class.getPackage().getName() + '.' + name;
		
		WhoCalled whoCalled = (WhoCalled) Class.forName(className).newInstance();
		
		Class<?> currentClass = whoCalled.getCallingClass(0);
		if (currentClass != WhoCalledProvider.class) {
			StringBuilder sb = new StringBuilder();
			sb.append("Stack:\n");
			for (int i = -2; i < 3; i++) {
				try {
					sb.append(whoCalled.getCallingClass(i).getName()).append(" at ").append(i).append('\n');
				} catch (Throwable ignored) {
				}
			}
			throw new Error("Wrong class returned: " + currentClass + ", expected WhoCalledProvider. " + sb);
		}
		
		return whoCalled;
	}
	
	private static int versionCompare(String firstString, String secondString) {
		String[] first = firstString.split("\\.");
		String[] second = secondString.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < first.length && i < second.length && first[i].equals(second[i])) {
			i++;
		}
		if (i < first.length && i < second.length) {
			int diff = Integer.valueOf(first[i]).compareTo(Integer.valueOf(second[i]));
			return Integer.signum(diff);
		}
		return Integer.signum(first.length - second.length);
	}
}