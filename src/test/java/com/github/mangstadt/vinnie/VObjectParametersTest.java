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

import static com.github.mangstadt.vinnie.TestUtils.assertEqualsAndHash;
import static com.github.mangstadt.vinnie.TestUtils.assertEqualsMethodEssentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class VObjectParametersTest {
	@Test
	public void convert_keys_to_uppercase() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.put("name", "value1");
		parameters.put("Name", "value2");

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("NAME", Arrays.asList("value1", "value2"));
		assertEquals(expected, parameters.getMap());

		assertEquals(Arrays.asList("value1", "value2"), parameters.get("Name"));

		assertTrue(parameters.remove("nAme", "value1"));
		assertEquals(Arrays.asList("value2"), parameters.removeAll("naMe"));
	}

	@Test
	public void multivalued_keys() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.putAll("name", "value1", "value1", "value2");
		parameters.putAll("name2");

		assertEquals(Arrays.asList("value1", "value1", "value2"), parameters.get("name"));
		assertNull(parameters.get("name2"));
		assertNull(parameters.get("name3"));
	}

	@Test
	public void replace() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.put("name1", "value1");

		assertEquals(Arrays.asList("value1"), parameters.replace("name1", "value2"));
		assertNull(parameters.replace("name2", "value3"));

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("NAME1", Arrays.asList("value2"));
		expected.put("NAME2", Arrays.asList("value3"));
		assertEquals(expected, parameters.getMap());
	}

	@Test
	public void replaceAll() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.put("name1", "value1");

		assertEquals(Arrays.asList("value1"), parameters.replaceAll("name1", "value2", "value3"));
		assertNull(parameters.replaceAll("name2", "value4", "value5"));

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("NAME1", Arrays.asList("value2", "value3"));
		expected.put("NAME2", Arrays.asList("value4", "value5"));
		assertEquals(expected, parameters.getMap());

		assertEquals(Arrays.asList("value2", "value3"), parameters.replaceAll("name1"));

		expected = new HashMap<String, List<String>>();
		expected.put("NAME2", Arrays.asList("value4", "value5"));
		assertEquals(expected, parameters.getMap());
	}

	@Test
	public void remove() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.put("name1", "value1");
		parameters.put("name2", "value1");
		parameters.put("name2", "value2");

		assertTrue(parameters.remove("name1", "value1"));
		assertFalse(parameters.remove("name1", "value3"));
		assertFalse(parameters.remove("name3", "value1"));
		assertTrue(parameters.remove("name2", "value1"));

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("NAME1", Arrays.<String> asList());
		expected.put("NAME2", Arrays.asList("value2"));
		assertEquals(expected, parameters.getMap());
	}

	@Test
	public void removeAll() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.putAll("name1", "value1", "value2");

		assertEquals(Arrays.asList("value1", "value2"), parameters.removeAll("name1"));
		assertNull(parameters.removeAll("name1"));

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		assertEquals(expected, parameters.getMap());
	}

	@Test
	public void first() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.putAll("name", "value1", "value2");

		assertEquals("value1", parameters.first("name"));

		parameters.get("name").clear();
		assertNull(parameters.first("name"));

		assertNull(parameters.first("name2"));
	}

	@Test
	public void isQuotedPrintable() {
		VObjectParameters parameters = new VObjectParameters();
		assertFalse(parameters.isQuotedPrintable());

		parameters.put("encoding", "foo");
		assertFalse(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.put(null, "QUOTED-PRINTABLE");
		assertTrue(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.put(null, "quoted-printable");
		assertTrue(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.put("encoding", "quoted-printable");
		assertTrue(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.put("encoding", "QUOTED-PRINTABLE");
		assertTrue(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.putAll(null, "foo", "QUOTED-PRINTABLE");
		assertTrue(parameters.isQuotedPrintable());

		parameters.clear();
		parameters.putAll("encoding", "foo", "QUOTED-PRINTABLE");
		assertTrue(parameters.isQuotedPrintable());
	}

	@Test
	public void getCharset() {
		VObjectParameters parameters = new VObjectParameters();
		assertNull(parameters.getCharset());

		parameters.put("CHARSET", "unknown");
		try {
			parameters.getCharset();
			fail();
		} catch (UnsupportedCharsetException e) {
			//expected
		}

		parameters.clear();
		parameters.put("CHARSET", "illegal name");
		try {
			parameters.getCharset();
			fail();
		} catch (IllegalCharsetNameException e) {
			//expected
		}

		parameters.clear();
		parameters.put("CHARSET", "utf-8");
		assertEquals("UTF-8", parameters.getCharset().name());
	}

	@Test
	public void copy() {
		VObjectParameters orig = new VObjectParameters();
		orig.put("name", "value");
		VObjectParameters copy = new VObjectParameters(orig);
		assertEquals(orig, copy);

		orig.put("name", "value2");
		assertEquals(Arrays.asList("value"), copy.get("name"));
	}

	@Test
	public void equals_and_hash() {
		VObjectParameters one = new VObjectParameters();
		assertEqualsMethodEssentials(one);

		one.put("name", "value");
		VObjectParameters two = new VObjectParameters();
		two.put("name", "value");
		assertEqualsAndHash(one, two);
	}
}
