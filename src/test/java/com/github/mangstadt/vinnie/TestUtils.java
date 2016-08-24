/*
 * MIT License
 * 
 * Copyright (c) 2016 Michael Angstadt
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mangstadt.vinnie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Contains miscellaneous unit test utility methods.
 * @author Michael Angstadt
 */
public class TestUtils {
	/**
	 * <p>
	 * Asserts some of the basic rules for the equals() method:
	 * </p>
	 * <ul>
	 * <li>The same object instance is equal to itself.</li>
	 * <li>Passing {@code null} into the method returns false.</li>
	 * <li>Passing an instance of a different class into the method returns
	 * false.</li>
	 * </ul>
	 * @param object an instance of the class to test.
	 */
	public static void assertEqualsMethodEssentials(Object object) {
		assertTrue(object.equals(object));
		assertFalse(object.equals(null));
		assertFalse(object.equals("other class"));
	}

	/**
	 * Asserts that two objects are equal according to their equals() method.
	 * Also asserts that their hash codes are the same.
	 * @param one the first object
	 * @param two the second object
	 */
	public static void assertEqualsAndHash(Object one, Object two) {
		assertEquals(one, two);
		assertEquals(two, one);
		assertEquals(one.hashCode(), two.hashCode());
	}
}
