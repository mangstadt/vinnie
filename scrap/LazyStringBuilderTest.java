/*
 * MIT License
 * 
 * Copyright (c) 2026 Michael Angstadt
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

package com.github.mangstadt.vinnie.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Random;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class LazyStringBuilderTest {
	@Test
	public void modification() {
		String source = "Hello world!";
		LazyStringBuilder sb = new LazyStringBuilder(source);
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == 'o') {
				sb.fork(i).append('0');
			} else {
				sb.append(c);
			}
		}

		String expected = "Hell0 w0rld!";
		String actual = sb.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void modification_substring() {
		String source = "--Hello world!--";
		int start = 2;
		int end = source.length() - 2;
		LazyStringBuilder sb = new LazyStringBuilder(source, start, end);
		for (int i = start; i < end; i++) {
			char c = source.charAt(i);
			if (c == 'o') {
				sb.fork(i).append('0');
			} else {
				sb.append(c);
			}
		}

		String expected = "Hell0 w0rld!";
		String actual = sb.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void no_modification() {
		String source = "Hello world!";
		LazyStringBuilder sb = new LazyStringBuilder(source);
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == 'B') {
				sb.fork(i).append('8');
			} else {
				sb.append(c);
			}
		}

		assertSame(sb.toString(), source);
	}

	@Test
	public void no_modification_substring() {
		String source = "--Hello world!--";
		int start = 2;
		int end = source.length() - 2;
		LazyStringBuilder sb = new LazyStringBuilder(source, start, end);
		for (int i = start; i < end; i++) {
			char c = source.charAt(i);
			if (c == 'B') {
				sb.fork(i).append('8');
			} else {
				sb.append(c);
			}
		}

		String expected = "Hello world!";
		String actual = sb.toString();
		assertEquals(expected, actual);
	}
}
