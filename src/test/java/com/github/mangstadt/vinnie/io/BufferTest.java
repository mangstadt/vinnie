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

package com.github.mangstadt.vinnie.io;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.mangstadt.vinnie.io.Buffer;

/**
 * @author Michael Angstadt
 */
public class BufferTest {
	private Buffer buffer;

	@Before
	public void before() {
		buffer = new Buffer();
	}

	@Test
	public void clear() {
		assertEquals("", buffer.get());

		buffer.append('a');
		assertEquals("a", buffer.get());

		buffer.clear();
		assertEquals("", buffer.get());
	}

	@Test
	public void getAndClear() {
		buffer.append('a');
		assertEquals("a", buffer.getAndClear());
		assertEquals("", buffer.get());
		assertEquals("", buffer.getAndClear());
		assertEquals("", buffer.get());
	}

	@Test
	public void append() {
		buffer.append('a');
		buffer.append("bcd");
		assertEquals("abcd", buffer.get());
	}

	@Test
	public void chop() {
		buffer.append('a').append('b').chop();
		assertEquals("a", buffer.get());

		buffer.chop();
		assertEquals("", buffer.get());
		assertEquals(0, buffer.size());

		buffer.chop();
		assertEquals("", buffer.get());
		assertEquals(0, buffer.size());
	}

	@Test
	public void size() {
		assertEquals(0, buffer.size());

		buffer.append('a');
		assertEquals(1, buffer.size());

		buffer.clear();
		assertEquals(0, buffer.size());
	}
}
