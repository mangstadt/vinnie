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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueBuilder;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueBuilder;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueIterator;

/**
 * @author Michael Angstadt
 */
public class VObjectPropertyValuesTest {
	private final String NEWLINE = System.getProperty("line.separator");

	@Test
	public void unescape() {
		String actual = VObjectPropertyValues.unescape("\\\\ \\, \\; \\n\\N \\\\\\,");
		String expected = "\\ , ; " + NEWLINE + NEWLINE + " \\,";
		assertEquals(expected, actual);

		String input = "no special characters";
		actual = VObjectPropertyValues.unescape(input);
		assertSame(input, actual);
	}

	@Test
	public void escape() {
		String actual = VObjectPropertyValues.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		String expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);

		String input = "no special characters";
		actual = VObjectPropertyValues.escape(input);
		assertSame(input, actual);
	}

	@Test
	public void parseList() {
		List<String> actual = VObjectPropertyValues.parseList("one,,two\\,three;four");
		List<String> expected = asList("one", "", "two,three;four");
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseList("one");
		expected = asList("one");
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseList("");
		expected = asList();
		assertEquals(expected, actual);
	}

	@Test
	public void writeList() {
		String actual = VObjectPropertyValues.writeList(Arrays.<Object> asList("one", null, "", 2, "three,four;five"));
		String expected = "one,null,,2,three\\,four\\;five";
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.writeList(asList());
		expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseSemiStructured() {
		List<String> actual = VObjectPropertyValues.parseSemiStructured("one;;two\\;three,four");
		List<String> expected = asList("one", "", "two;three,four");
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseSemiStructured("one;two;three", 2);
		expected = asList("one", "two;three");
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseSemiStructured("");
		expected = asList();
		assertEquals(expected, actual);
	}

	@Test
	public void writeSemiStructured() {
		String actual, expected;

		for (boolean includeTrailingSemicolons : new boolean[] { false, true }) {
			actual = VObjectPropertyValues.writeSemiStructured(Arrays.<Object> asList("one", null, "", 2, "three;four,five"), includeTrailingSemicolons);
			expected = "one;null;;2;three\\;four\\,five";
			assertEquals(expected, actual);

			actual = VObjectPropertyValues.writeSemiStructured(asList(), includeTrailingSemicolons);
			expected = "";
			assertEquals(expected, actual);
		}

		{
			List<Object> input = Arrays.<Object> asList("one", "", "two", "", "");

			actual = VObjectPropertyValues.writeSemiStructured(input, false);
			expected = "one;;two";
			assertEquals(expected, actual);

			actual = VObjectPropertyValues.writeSemiStructured(input, true);
			expected = "one;;two;;";
			assertEquals(expected, actual);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void parseStructured() {
		List<List<String>> actual = VObjectPropertyValues.parseStructured("one;two,,three;;four\\,five\\;six");
		//@formatter:off
		List<List<String>> expected = asList(
			asList("one"),
			asList("two", "", "three"),
			Arrays.<String>asList(),
			asList("four,five;six")
		);
		//@formatter:on
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseStructured("");
		expected = asList();
		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void writeStructured() {
		String actual, expected;

		for (boolean includeTrailingSemicolons : new boolean[] { false, true }) {
			//@formatter:off
			actual = VObjectPropertyValues.writeStructured(asList(
				asList("one"),
				asList("two", "", 3, null),
				Arrays.<String>asList(),
				asList("four,five;six")
			), includeTrailingSemicolons);
			//@formatter:on
			expected = "one;two,,3,null;;four\\,five\\;six";
			assertEquals(expected, actual);

			actual = VObjectPropertyValues.writeStructured(new ArrayList<List<String>>(), includeTrailingSemicolons);
			expected = "";
			assertEquals(expected, actual);
		}

		{
			//@formatter:off
			List<List<?>> input = asList(
				asList("one"),
				asList(),
				asList("two"),
				asList(),
				asList()
			);
			//@formatter:on

			actual = VObjectPropertyValues.writeStructured(input, false);
			expected = "one;;two";
			assertEquals(expected, actual);

			actual = VObjectPropertyValues.writeStructured(input, true);
			expected = "one;;two;;";
			assertEquals(expected, actual);
		}
	}

	@Test
	public void parseMultimap() {
		Map<String, List<String>> actual = VObjectPropertyValues.parseMultimap("one=two;;ONE=two;THREE;FOUR=five,six\\,seven\\;eight;NINE=;TEN=eleven=twelve");
		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("ONE", Arrays.asList("two", "two"));
		expected.put("THREE", Arrays.asList(""));
		expected.put("FOUR", Arrays.asList("five", "six,seven;eight"));
		expected.put("NINE", Arrays.asList(""));
		expected.put("TEN", Arrays.asList("eleven=twelve"));
		assertEquals(expected, actual);

		actual = VObjectPropertyValues.parseMultimap("");
		assertTrue(actual.isEmpty());
	}

	@Test
	public void writeMultimap() {
		Map<String, List<String>> input = new LinkedHashMap<String, List<String>>();
		input.put("A", asList("one"));
		input.put("B", asList("two", "three,four;five", null));
		input.put("C", asList(""));
		input.put("d", asList("six=seven"));
		input.put("E", Arrays.<String> asList());

		String expected = "A=one;B=two,three\\,four\\;five,null;C=;D=six=seven;E";
		String actual = VObjectPropertyValues.writeMultimap(input);
		assertEquals(expected, actual);
	}

	@Test
	public void SemiStructuredValueIterator() {
		{
			SemiStructuredValueIterator it = new SemiStructuredValueIterator("one;;two");

			assertTrue(it.hasNext());
			assertEquals("one", it.next());

			assertTrue(it.hasNext());
			assertNull(it.next());

			assertTrue(it.hasNext());
			assertEquals("two", it.next());

			assertFalse(it.hasNext());
			assertNull(it.next());
		}
		{
			SemiStructuredValueIterator it = new SemiStructuredValueIterator("one;two;three", 2);

			assertTrue(it.hasNext());
			assertEquals("one", it.next());

			assertTrue(it.hasNext());
			assertEquals("two;three", it.next());

			assertFalse(it.hasNext());
			assertNull(it.next());
		}
	}

	@Test
	public void SemiStructuredValueBuilder() {
		SemiStructuredValueBuilder builder = new SemiStructuredValueBuilder();
		builder.append("one");
		builder.append(null);
		assertEquals("one;", builder.build());

		builder = new SemiStructuredValueBuilder();
		builder.append("one");
		builder.append(null);
		assertEquals("one", builder.build(false));

		builder = new SemiStructuredValueBuilder();
		builder.append("one");
		builder.append(null);
		assertEquals("one;", builder.build(true));
	}

	@Test
	public void StructuredValueIterator() {
		StructuredValueIterator it = new StructuredValueIterator("one;two;;;three,four");

		assertTrue(it.hasNext());
		assertEquals("one", it.nextValue());

		assertTrue(it.hasNext());
		assertEquals(asList("two"), it.nextComponent());

		assertTrue(it.hasNext());
		assertNull(it.nextValue());

		assertTrue(it.hasNext());
		assertEquals(asList(), it.nextComponent());

		assertTrue(it.hasNext());
		assertEquals(asList("three", "four"), it.nextComponent());

		assertFalse(it.hasNext());
		assertNull(it.nextValue());
		assertEquals(asList(), it.nextComponent());
	}

	@Test
	public void StructuredValueBuilder() {
		StructuredValueBuilder builder = new StructuredValueBuilder();
		builder.append("one");
		builder.append(asList("two", "three"));
		builder.append((Object) null);
		assertEquals("one;two,three;", builder.build());

		builder = new StructuredValueBuilder();
		builder.append("one");
		builder.append(asList("two", "three"));
		builder.append((Object) null);
		assertEquals("one;two,three", builder.build(false));

		builder = new StructuredValueBuilder();
		builder.append("one");
		builder.append(asList("two", "three"));
		builder.append((Object) null);
		assertEquals("one;two,three;", builder.build(true));
	}
}
