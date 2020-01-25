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

public interface WhoCalled {
	WhoCalled $ = WhoCalledProvider.getWhoCalled();
	
	/**
	 * Returns the Class of the method which is one level above the current method.
	 *
	 * For example, if you call WhoCalled.$.getCallingClass in the Example.whoCalled(), which was called by Main.main(),
	 * it will return Main.class.
	 *
	 * @return Calling class
	 */
	Class<?> getCallingClass();
	
	/**
	 * Returns the Class of the method which is (depth) levels above the current method.
	 *
	 * For example, if you call WhoCalled.$.getCallingClass(0) in the Example class, it will return
	 * Example.class.
	 *
	 * @param depth Depth of class to get. 0 = current method, 1 = caller, etc.
	 * @return Calling class
	 */
	Class<?> getCallingClass(int depth);
	
	/**
	 * Returns whether the given class is one of the calling classes
	 *
	 * @param clazz Class to search for in callers
	 * @return true if the class clazz is a caller of this class
	 */
	boolean isCalledByClass(Class<?> clazz);
}