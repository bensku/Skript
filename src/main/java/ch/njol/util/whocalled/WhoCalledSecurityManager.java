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

class WhoCalledSecurityManager extends SecurityManager implements WhoCalled {
	private static final int OFFSET = 1;
	
	@Override
	public Class<?> getCallingClass() {
		return getClassContext()[OFFSET + 1];
	}
	
	@Override
	public Class<?> getCallingClass(int depth) {
		return getClassContext()[OFFSET + depth];
	}
	
	@Override
	public boolean isCalledByClass(Class<?> clazz) {
		Class<?>[] classes = getClassContext();
		
		for (int i = OFFSET + 1; i < classes.length; i++) {
			if (classes[i] == clazz) {
				return true;
			}
		}
		
		return false;
	}
}