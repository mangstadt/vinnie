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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains utility methods for parsing and writing vobject property values.
 * @author Michael Angstadt
 */
public final class VObjectPropertyValues {
	/**
	 * The local computer's newline character sequence.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * <p>
	 * Unescapes all escaped characters in a property value. Escaped newlines
	 * are replaced with the local system's newline character sequence.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String value = VObjectPropertyValues.unescape(&quot;one\\,two\\;three\\nfour&quot;);
	 * assertEquals(&quot;one,two;three\nfour&quot;, value);
	 * </pre>
	 * @param value the value to unescape
	 * @return the unescaped value
	 */
	public static String unescape(String value) {
		return unescape(value, 0, value.length());
	}

	/**
	 * Unescapes all escaped characters in a substring.
	 * @param string the entire string
	 * @param start the start index of the substring to unescape
	 * @param end the end index (exclusive) of the substring to unescape
	 * @return the unescaped substring
	 */
	private static String unescape(String string, int start, int end) {
		StringBuilder sb = null;
		boolean escaped = false;
		for (int i = start; i < end; i++) {
			char c = string.charAt(i);

			if (escaped) {
				escaped = false;

				if (sb == null) {
					sb = new StringBuilder(end - start);
					sb.append(string.substring(start, i - 1));
				}

				switch (c) {
				case 'n':
				case 'N':
					sb.append(NEWLINE);
					continue;
				}

				sb.append(c);
				continue;
			}

			switch (c) {
			case '\\':
				escaped = true;
				continue;
			}

			if (sb != null) {
				sb.append(c);
			}
		}

		if (sb != null) {
			return sb.toString();
		}

		/*
		 * The "String#substring" method makes no guarantee that the same String
		 * object will be returned if the entire string length is passed into
		 * the method.
		 */
		if (start == 0 && end == string.length()) {
			return string;
		}

		return string.substring(start, end);
	}

	/**
	 * <p>
	 * Escapes all special characters within a property value. These characters
	 * are:
	 * </p>
	 * <ul>
	 * <li>backslashes ({@code \})</li>
	 * <li>commas ({@code ,})</li>
	 * <li>semi-colons ({@code ;})</li>
	 * </ul>
	 * <p>
	 * Note: Newlines are not escaped by this method. They are escaped by
	 * {@link VObjectWriter} when the data is serialized.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String value = VObjectPropertyValues.escape(&quot;one,two;three\nfour&quot;);
	 * assertEquals(&quot;one\\,two\\;three\nfour&quot;, value);
	 * </pre>
	 * @param value the value to escape
	 * @return the escaped value
	 */
	public static String escape(String value) {
		StringBuilder sb = null;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\\':
			case ',':
			case ';':
				if (sb == null) {
					sb = new StringBuilder(value.length() * 2);
					sb.append(value.substring(0, i));
				}
				sb.append('\\').append(c);
				break;
			default:
				if (sb != null) {
					sb.append(c);
				}
				break;
			}
		}
		return (sb == null) ? value : sb.toString();
	}

	/**
	 * Escapes all special characters within the given string.
	 * @param string the string to escape
	 * @param sb the buffer on which to append the escaped string
	 */
	private static void escape(String string, StringBuilder sb) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch (c) {
			case '\\':
			case ',':
			case ';':
				sb.append('\\');
			}
			sb.append(c);
		}
	}

	/**
	 * <p>
	 * Parses a "list" property value.
	 * </p>
	 * <p>
	 * List values contain multiple values separated by commas. The order that
	 * the values are in usually doesn't matter.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * List&lt;String&gt; list = VObjectPropertyValues.parseList(&quot;one,two\\,three&quot;);
	 * assertEquals(Arrays.asList(&quot;one&quot;, &quot;two,three&quot;), list);
	 * </pre>
	 * @param value the value to parse
	 * @return the parsed list
	 */
	public static List<String> parseList(String value) {
		return split(value, ',', true, -1);
	}

	/**
	 * <p>
	 * Generates a "list" property value.
	 * </p>
	 * <p>
	 * List values contain multiple values separated by commas. The order that
	 * the values are in usually doesn't matter.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String value = VObjectPropertyValues.writeList(Arrays.asList("one", null, 2, "three,four");
	 * assertEquals("one,null,2,three\\,four", value);
	 * </pre>
	 * @param values the values to write (the {@code toString()} method is
	 * invoked on each object)
	 * @return the list value string
	 */
	public static String writeList(Collection<?> values) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Object value : values) {
			if (!first) {
				sb.append(',');
			}

			if (value == null) {
				sb.append("null");
			} else {
				escape(value.toString(), sb);
			}

			first = false;
		}

		return sb.toString();
	}

	/**
	 * <p>
	 * Parses a "semi-structured" property value.
	 * </p>
	 * <p>
	 * Semi-structured values contain multiple values separate by semicolons.
	 * Unlike structured values, each value cannot have their own comma
	 * separated list of sub-values. The order that the values are in matters.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * List&lt;String&gt; values = VObjectPropertyValues.parseSemiStructured(&quot;one;two\\;three,four&quot;);
	 * assertEquals(Arrays.asList(&quot;one&quot;, &quot;two;three,four&quot;), values);
	 * </pre>
	 * @param value the value to parse
	 * @return the parsed values
	 */
	public static List<String> parseSemiStructured(String value) {
		return parseSemiStructured(value, -1);
	}

	/**
	 * <p>
	 * Parses a "semi-structured" property value.
	 * </p>
	 * <p>
	 * Semi-structured values contain multiple values separate by semicolons.
	 * Unlike structured values, each value cannot have their own comma
	 * separated list of sub-values. The order that the values are in matters.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * List&lt;String&gt; values = VObjectPropertyValues.parseSemiStructured(&quot;one;two;three&quot;, 2);
	 * assertEquals(Arrays.asList(&quot;one&quot;, &quot;two;three&quot;), values);
	 * </pre>
	 * @param value the value to parse
	 * @param limit the max number of items to parse
	 * @return the parsed values
	 */
	public static List<String> parseSemiStructured(String value, int limit) {
		return split(value, ';', true, limit);
	}

	/**
	 * <p>
	 * Writes a "semi-structured" property value.
	 * </p>
	 * <p>
	 * Semi-structured values contain multiple values separate by semicolons.
	 * Unlike structured values, each value cannot have their own comma
	 * separated list of sub-values. The order that the values are in matters.
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String value = VObjectPropertyValues.writeSemiStructured(Arrays.asList(&quot;one&quot;, null, 2, &quot;three;four,five&quot;, &quot;&quot;), false);
	 * assertEquals(&quot;one;null;2;three\\;four\\,five&quot;, value);
	 * </pre>
	 * @param values the values to write
	 * @param includeTrailingSemicolons true to include the semicolon delimiters
	 * for empty values at the end of the values list, false to trim them
	 * @return the semi-structured value string
	 */
	public static String writeSemiStructured(List<?> values, boolean includeTrailingSemicolons) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Object value : values) {
			if (!first) {
				sb.append(';');
			}

			if (value == null) {
				sb.append("null");
			} else {
				escape(value.toString(), sb);
			}

			first = false;
		}

		if (!includeTrailingSemicolons) {
			trimTrailingSemicolons(sb);
		}

		return sb.toString();
	}

	//@formatter:off
	/**
	 * <p>
	 * Parses a "structured" property value.</p>
	 * <p>
	 * Structured values are essentially 2-D arrays. They contain multiple
	 * components separated by semicolons, and each component can have multiple
	 * values separated by commas. The order that the components are in matters,
	 * but the order that each component's list of values are in usually doesn't
	 * matter.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * List&lt;List&lt;String&gt;&gt; values = VObjectPropertyValues.parseStructured("one;two,three;four\\,five\\;six");
	 * assertEquals(Arrays.asList(
	 *   Arrays.asList("one"),
	 *   Arrays.asList("two", "three"),
	 *   Arrays.asList("four,five;six")
	 * ), values);
	 * </pre>
	 * @param value the value to parse
	 * @return the parsed values
	 */
	//@formatter:on
	public static List<List<String>> parseStructured(String value) {
		if (value.length() == 0) {
			return new ArrayList<List<String>>(0); //return a mutable list
		}

		List<List<String>> components = new ArrayList<List<String>>();
		List<String> curComponent = new ArrayList<String>();
		components.add(curComponent);

		boolean escaped = false;
		int cursor = 0;
		for (int i = 0; i < value.length(); i++) {
			if (escaped) {
				escaped = false;
				continue;
			}

			char c = value.charAt(i);
			switch (c) {
			case ';':
				String v = unescape(value, cursor, i);
				if (curComponent.isEmpty() && v.length() == 0) {
					/*
					 * If the component is empty, do not add an empty string to
					 * the list.
					 */
				} else {
					curComponent.add(v);
				}

				curComponent = new ArrayList<String>();
				components.add(curComponent);
				cursor = i + 1;
				continue;
			case ',':
				v = unescape(value, cursor, i);
				curComponent.add(v);
				cursor = i + 1;
				continue;
			case '\\':
				escaped = true;
				continue;
			}
		}

		String v = unescape(value, cursor, value.length());
		if (curComponent.isEmpty() && v.length() == 0) {
			/*
			 * If the component is empty, do not add an empty string to the
			 * list.
			 */
		} else {
			curComponent.add(v);
		}

		return components;
	}

	//@formatter:off
	/**
	 * <p>
	 * Writes a "structured" property value.
	 * </p>
	 * <p>
	 * Structured values are essentially 2-D arrays. They contain multiple
	 * components separated by semicolons, and each component can have multiple
	 * values separated by commas. The order that the components are in matters,
	 * but the order that each component's list of values are in usually doesn't
	 * matter.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String value = VObjectPropertyValues.writeStructured(Arrays.asList(
	 *   Arrays.asList("one"),
	 *   Arrays.asList("two", 3),
	 *   Arrays.asList(null),
	 *   Arrays.asList("four,five;six"),
	 *   Arrays.asList()
	 * ), false);
	 * assertEquals("one;two,3;null;four\\,five\\;six", value);
	 * </pre>
	 * @param components the components to write (each value's {@code toString()}
	 * method will be called)
	 * @param includeTrailingSemicolons true to include the semicolon delimiters
	 * for empty components at the end of the written value, false to trim them
	 * @return the structured value string
	 */
	//@formatter:on
	public static String writeStructured(List<? extends List<?>> components, boolean includeTrailingSemicolons) {
		StringBuilder sb = new StringBuilder();
		boolean firstComponent = true;
		for (List<?> component : components) {
			if (!firstComponent) {
				sb.append(';');
			}

			boolean firstValue = true;
			for (Object value : component) {
				if (!firstValue) {
					sb.append(',');
				}

				if (value == null) {
					sb.append("null");
				} else {
					escape(value.toString(), sb);
				}

				firstValue = false;
			}

			firstComponent = false;
		}

		if (!includeTrailingSemicolons) {
			trimTrailingSemicolons(sb);
		}

		return sb.toString();
	}

	/**
	 * <p>
	 * Parses a "multimap" property value.
	 * </p>
	 * <p>
	 * Multimap values are collections of key/value pairs whose keys can be
	 * multi-valued. Key/value pairs are separated by semicolons. Values are
	 * separated by commas. Keys are converted to uppercase.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * Map&lt;String, List&lt;String&gt;&gt; multimap = VObjectPropertyValues.parseMultimap(&quot;one=two;THREE;FOUR=five,six\\,seven\\;eight&quot;);
	 * Map&lt;String, List&lt;String&gt;&gt; expected = new HashMap&lt;String, List&lt;String&gt;&gt;();
	 * expected.put(&quot;ONE&quot;, Arrays.asList(&quot;two&quot;));
	 * expected.put(&quot;THREE&quot;, Arrays.&lt;String&gt; asList());
	 * expected.put(&quot;FOUR&quot;, Arrays.asList(&quot;five&quot;, &quot;six,seven;eight&quot;));
	 * assertEquals(expected, multimap);
	 * </pre>
	 * @param value the value to parse
	 * @return the parsed values
	 */
	public static Map<String, List<String>> parseMultimap(String value) {
		if (value.length() == 0) {
			return new HashMap<String, List<String>>(0); //return a mutable map
		}

		Map<String, List<String>> multimap = new LinkedHashMap<String, List<String>>();
		String curName = null;
		List<String> curValues = new ArrayList<String>();

		boolean escaped = false;
		int cursor = 0;
		for (int i = 0; i < value.length(); i++) {
			if (escaped) {
				escaped = false;
				continue;
			}

			char c = value.charAt(i);

			switch (c) {
			case ';':
				if (curName == null) {
					curName = unescape(value, cursor, i).toUpperCase();
				} else {
					curValues.add(unescape(value, cursor, i));
				}

				if (curName.length() > 0) {
					if (curValues.isEmpty()) {
						curValues.add("");
					}
					List<String> existing = multimap.get(curName);
					if (existing == null) {
						multimap.put(curName, curValues);
					} else {
						existing.addAll(curValues);
					}
				}

				curName = null;
				curValues = new ArrayList<String>();
				cursor = i + 1;
				break;
			case '=':
				if (curName == null) {
					curName = unescape(value, cursor, i).toUpperCase();
					cursor = i + 1;
				}
				break;
			case ',':
				curValues.add(unescape(value, cursor, i));
				cursor = i + 1;
				break;
			case '\\':
				escaped = true;
				break;
			}
		}

		if (curName == null) {
			curName = unescape(value, cursor, value.length()).toUpperCase();
		} else {
			curValues.add(unescape(value, cursor, value.length()));
		}

		if (curName.length() > 0) {
			if (curValues.isEmpty()) {
				curValues.add("");
			}
			List<String> existing = multimap.get(curName);
			if (existing == null) {
				multimap.put(curName, curValues);
			} else {
				existing.addAll(curValues);
			}
		}

		return multimap;
	}

	/**
	 * <p>
	 * Writes a "multimap" property value.
	 * </p>
	 * <p>
	 * Multimap values are collections of key/value pairs whose keys can be
	 * multi-valued. Key/value pairs are separated by semicolons. Values are
	 * separated by commas. Keys are converted to uppercase.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * Map&lt;String, List&lt;Object&gt;&gt; input = new LinkedHashMap&lt;String, List&lt;Object&gt;&gt;();
	 * input.put(&quot;one&quot;, Arrays.asList(&quot;two&quot;));
	 * input.put(&quot;THREE&quot;, Arrays.&lt;String&gt; asList());
	 * input.put(&quot;FOUR&quot;, Arrays.asList(5, &quot;six,seven;eight&quot;));
	 * 
	 * String value = VObjectPropertyValues.writeMultimap(input);
	 * assertEquals(&quot;ONE=two;THREE;FOUR=5,six\\,seven\\;eight&quot;, value);
	 * </pre>
	 * @param multimap the multimap to write (each value's {@code toString()}
	 * method will be called)
	 * @return the multimap value string
	 */
	public static String writeMultimap(Map<String, ? extends List<?>> multimap) {
		StringBuilder sb = new StringBuilder();
		boolean firstKey = true;
		for (Map.Entry<String, ? extends List<?>> entry : multimap.entrySet()) {
			if (!firstKey) {
				sb.append(';');
			}

			String key = entry.getKey().toUpperCase();
			escape(key, sb);

			List<?> values = entry.getValue();
			if (values.isEmpty()) {
				continue;
			}

			sb.append('=');

			boolean firstValue = true;
			for (Object value : values) {
				if (!firstValue) {
					sb.append(',');
				}

				if (value == null) {
					sb.append("null");
				} else {
					escape(value.toString(), sb);
				}

				firstValue = false;
			}

			firstKey = false;
		}

		return sb.toString();
	}

	/**
	 * Removes trailing semicolon characters from the end of the given buffer.
	 * @param sb the buffer
	 */
	private static void trimTrailingSemicolons(StringBuilder sb) {
		int index = -1;
		for (int i = sb.length() - 1; i >= 0; i--) {
			char c = sb.charAt(i);
			if (c != ';') {
				index = i;
				break;
			}
		}
		sb.setLength(index + 1);
	}

	/**
	 * Splits a string.
	 * @param string the string to split
	 * @param delimiter the delimiter to escape by
	 * @param unescape true to unescape each split value, false not to
	 * @param limit the number of split values to parse or -1 to parse them all
	 * @return the split values
	 */
	private static List<String> split(String string, char delimiter, boolean unescape, int limit) {
		if (string.length() == 0) {
			return new ArrayList<String>(0); //return a mutable list
		}

		List<String> list = new ArrayList<String>();
		boolean escaped = false;
		int cursor = 0;
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);

			if (escaped) {
				escaped = false;
				continue;
			}

			if (ch == delimiter) {
				String value = unescape ? unescape(string, cursor, i) : string.substring(cursor, i);
				list.add(value);

				cursor = i + 1;
				if (limit > 0 && list.size() == limit - 1) {
					break;
				}

				continue;
			}

			switch (ch) {
			case '\\':
				escaped = true;
				continue;
			}
		}

		String value = unescape ? unescape(string, cursor, string.length()) : string.substring(cursor);
		list.add(value);

		return list;
	}

	/**
	 * <p>
	 * Iterates over the values in a "semi-structured" property value.
	 * </p>
	 * <p>
	 * Semi-structured values contain multiple values separate by semicolons.
	 * The order that the values are in matters.
	 * </p>
	 */
	public static class SemiStructuredValueIterator {
		private final Iterator<String> it;

		/**
		 * Constructs a new semi-structured value iterator.
		 * @param value the value to parse
		 */
		public SemiStructuredValueIterator(String value) {
			this(value, -1);
		}

		/**
		 * Constructs a new semi-structured value iterator.
		 * @param value the value to parse
		 * @param limit the number of values to parse, or -1 to parse all values
		 */
		public SemiStructuredValueIterator(String value, int limit) {
			it = parseSemiStructured(value, limit).iterator();
		}

		/**
		 * Gets the next value.
		 * @return the next value or null if the value is empty or null if there
		 * are no more values
		 */
		public String next() {
			if (!hasNext()) {
				return null;
			}

			String next = it.next();
			return (next.length() == 0) ? null : next;
		}

		/**
		 * Determines if there are any more values left.
		 * @return true if there are more values, false if not
		 */
		public boolean hasNext() {
			return it.hasNext();
		}
	}

	/**
	 * <p>
	 * Builds a "semi-structured" property value.
	 * </p>
	 * <p>
	 * Semi-structured values contain multiple values separate by semicolons.
	 * The order that the values are in matters.
	 * </p>
	 */
	public static class SemiStructuredValueBuilder {
		private final List<Object> values = new ArrayList<Object>();

		/**
		 * Appends a value. If the value is null, an empty value will be
		 * appended.
		 * @param value the value
		 * @return this
		 */
		public SemiStructuredValueBuilder append(Object value) {
			if (value == null) {
				value = "";
			}
			values.add(value);
			return this;
		}

		/**
		 * Builds the semi-structured value string.
		 * @return the semi-structured value string
		 */
		public String build() {
			return build(true);
		}

		/**
		 * Builds the semi-structured value string.
		 * @param includeTrailingSemicolons true to include the semicolon
		 * delimiters of empty values at the end of the value string, false to
		 * trim them
		 * @return the semi-structured value string
		 */
		public String build(boolean includeTrailingSemicolons) {
			return writeSemiStructured(values, includeTrailingSemicolons);
		}
	}

	/**
	 * <p>
	 * Iterates over the values in a "structured" property value.
	 * </p>
	 * <p>
	 * Structured values are essentially 2-D arrays. They contain multiple
	 * components separated by semicolons, and each component can have multiple
	 * values separated by commas. The order that the components are in matters,
	 * but the order that each component's list of values are in usually doesn't
	 * matter.
	 * </p>
	 */
	public static class StructuredValueIterator {
		private final Iterator<List<String>> it;

		/**
		 * Constructs a new structured value iterator.
		 * @param string the structured value to parse
		 */
		public StructuredValueIterator(String string) {
			this(parseStructured(string));
		}

		/**
		 * Constructs a new structured value iterator.
		 * @param components the components to iterator over
		 */
		public StructuredValueIterator(List<List<String>> components) {
			it = components.iterator();
		}

		/**
		 * Gets the first value of the next component.
		 * @return the value or null if the component is empty or null if there
		 * are no more components
		 */
		public String nextValue() {
			if (!hasNext()) {
				return null;
			}

			List<String> list = it.next();
			return list.isEmpty() ? null : list.get(0);
		}

		/**
		 * Gets the next component.
		 * @return the next component or an empty list of there are no more
		 * components
		 */
		public List<String> nextComponent() {
			if (!hasNext()) {
				return new ArrayList<String>(0); //should be mutable
			}

			return it.next();
		}

		public boolean hasNext() {
			return it.hasNext();
		}
	}

	/**
	 * <p>
	 * Builds "structured" property values.
	 * </p>
	 * <p>
	 * Structured values are essentially 2-D arrays. They contain multiple
	 * components separated by semicolons, and each component can have multiple
	 * values separated by commas. The order that the components are in matters,
	 * but the order that each component's list of values are in usually doesn't
	 * matter.
	 * </p>
	 */
	public static class StructuredValueBuilder {
		private final List<List<?>> components = new ArrayList<List<?>>();

		/**
		 * Appends a single-valued component if the value is non-null, and
		 * appends an empty component if the value is null.
		 * @param value the value
		 * @return this
		 */
		public StructuredValueBuilder append(Object value) {
			List<Object> component = (value == null) ? Arrays.<Object> asList() : Arrays.asList(value);
			return append(component);
		}

		/**
		 * Appends a component.
		 * @param component the component
		 * @return this
		 */
		public StructuredValueBuilder append(List<? extends Object> component) {
			components.add(component);
			return this;
		}

		/**
		 * Builds the structured value string.
		 * @return the structured value string
		 */
		public String build() {
			return build(true);
		}

		/**
		 * Builds the structured value string.
		 * @param includeTrailingSemicolons true to include the semicolon
		 * delimiters for empty components at the end of the value string, false
		 * to trim them
		 * @return the structured value string
		 */
		public String build(boolean includeTrailingSemicolons) {
			return writeStructured(components, includeTrailingSemicolons);
		}
	}

	private VObjectPropertyValues() {
		//hide
	}
}
