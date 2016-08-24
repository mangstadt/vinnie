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
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class UtilsTest {
	@Test
	public void ltrim() {
		String actual, expected;

		actual = Utils.ltrim("One two three");
		expected = "One two three";
		assertEquals(actual, expected);

		actual = Utils.ltrim("\n \t One two three \t \n ");
		expected = "One two three \t \n ";
		assertEquals(actual, expected);

		actual = Utils.ltrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		actual = Utils.ltrim("");
		expected = "";
		assertEquals(actual, expected);
	}

	@Test
	public void rtrim() {
		String actual, expected;

		actual = Utils.rtrim("One two three");
		expected = "One two three";
		assertEquals(actual, expected);

		actual = Utils.rtrim("\n \t One two three \t \n ");
		expected = "\n \t One two three";
		assertEquals(actual, expected);

		actual = Utils.rtrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		actual = Utils.rtrim("");
		expected = "";
		assertEquals(actual, expected);
	}

	@Test
	public void escapeNewlines() {
		String input, actual, expected;

		input = "no newlines";
		actual = Utils.escapeNewlines(input);
		assertSame(input, actual);

		input = "one\r\ntwo\nthree\r";
		actual = Utils.escapeNewlines(input);
		expected = "one\\ntwo\\nthree\\n";
		assertEquals(expected, actual);

		input = "";
		actual = Utils.escapeNewlines(input);
		assertSame(input, actual);
	}
}
