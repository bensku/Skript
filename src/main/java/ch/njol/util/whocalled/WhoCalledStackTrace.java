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

/**
 * StackTrace is very slow.
 * This class should not actually be used unless the SecurityManager implementation is broken by a future java update.
 * This is extremely unlikely.
 */
class WhoCalledStackTrace implements WhoCalled {
	private static final int OFFSET = 1;
	
	@Override
	public Class<?> getCallingClass() {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[OFFSET + 1].getClassName());
		} catch (ClassNotFoundException e) {
			throw new NoClassDefFoundError(e.getMessage());
		}
	}
	
	@Override
	public Class<?> getCallingClass(int depth) {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[OFFSET + depth].getClassName());
		} catch (ClassNotFoundException e) {
			throw new NoClassDefFoundError(e.getMessage());
		}
	}
	
	@Override
	public boolean isCalledByClass(Class<?> clazz) {
		for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (element.getClassName().equals(clazz.getName())) {
				return true;
			}
		}
		return false;
	}
}