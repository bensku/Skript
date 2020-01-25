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

// Deprecated method call - acceptable as this implementation is only used after it is tested by WhoCalledProvider
@SuppressWarnings("deprecation")
class WhoCalledReflection extends SecurityManager implements WhoCalled {
	private static final int OFFSET = 2;
	
	@Override
	public Class<?> getCallingClass() {
		return sun.reflect.Reflection.getCallerClass(OFFSET + 1);
	}
	
	@Override
	public Class<?> getCallingClass(int depth) {
		return sun.reflect.Reflection.getCallerClass(OFFSET + depth);
	}
	
	@Override
	public boolean isCalledByClass(Class<?> clazz) {
		// Using SecurityManager is better performing than Reflection for isCalledByClass unless the stack depth is very small
		// So WhoCalledReflection uses SecurityManager for this method
		Class<?>[] classes = getClassContext();
		
		for (int i = OFFSET; i < classes.length; i++) {
			if (classes[i] == clazz) {
				return true;
			}
		}
		
		return false;
	}
	
	boolean isCalledByClassReflection(Class<?> clazz) {
		for (int i = OFFSET + 1; ; i++) {
			Class<?> caller = sun.reflect.Reflection.getCallerClass(i);
			
			if (caller == null)
				return false;
			
			if (caller == clazz)
				return true;
		}
	}
}