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

package com.github.mangstadt.vinnie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class LaciTokenTest {
	@ClassRule
	public static final DefaultLocaleRule tzRule = new DefaultLocaleRule(new Locale("tr", "TR"));

	@Test
	public void toLowerCase() {
		String string = "TITLE";
		assertEquals("tıtle", string.toLowerCase());

		LaciToken laci = new LaciToken(string);
		assertEquals("title", laci.toLowerCase());
	}

	@Test
	public void toUpperCase() {
		String string = "title";
		assertEquals("TİTLE", string.toUpperCase());

		LaciToken laci = new LaciToken(string);
		assertEquals("TITLE", laci.toUpperCase());
	}

	@Test
	public void equals_hashCode() {
		LaciToken a = new LaciToken("title");
		LaciToken b = new LaciToken("TITLE");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void equalsIgnoreCase() {
		assertTrue(new LaciToken("TITLE").equalsIgnoreCase("title"));
	}

	@Test(expected = NullPointerException.class)
	public void nulls_not_allowed() {
		new LaciToken(null);
	}

	@Test
	public void create() {
		assertNull(LaciToken.create(null));
		assertEquals(new LaciToken("foo"), LaciToken.create("foo"));
	}
}
