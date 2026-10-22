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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A multimap implementation for holding the parameters of a
 * {@link VObjectProperty}.
 * @author Michael Angstadt
 */
public class VObjectParameters implements Iterable<Map.Entry<LaciToken, List<String>>> {
	private final Map<LaciToken, List<String>> multimap;

	/**
	 * Creates an empty list of parameters.
	 */
	public VObjectParameters() {
		multimap = new LinkedHashMap<LaciToken, List<String>>(); //preserve insertion order of keys
	}

	/**
	 * Creates a list of parameters backed by the given map. Any changes made to
	 * the given map will effect the parameter list and vice versa.
	 * @param map the map
	 */
	public VObjectParameters(Map<LaciToken, List<String>> map) {
		multimap = map;
	}

	/**
	 * Copies an existing list of parameters.
	 * @param original the existing list
	 */
	public VObjectParameters(VObjectParameters original) {
		this();
		for (Map.Entry<LaciToken, List<String>> entry : original) {
			LaciToken name = entry.getKey();
			List<String> values = entry.getValue();
			multimap.put(name, new ArrayList<String>(values));
		}
	}

	/**
	 * Gets the values that are assigned to a key.
	 * @param key the key or null for nameless values
	 * @return the values or null if the key does not exist
	 */
	public List<String> get(String key) {
		return get(LaciToken.create(key));
	}

	/**
	 * Gets the values that are assigned to a key.
	 * @param key the key or null for nameless values
	 * @return the values or null if the key does not exist
	 */
	public List<String> get(LaciToken key) {
		return multimap.get(key);
	}

	/**
	 * Inserts a value.
	 * @param key the key or null for nameless values
	 * @param value the value to add
	 */
	public void put(String key, String value) {
		put(LaciToken.create(key), value);
	}

	/**
	 * Inserts a value.
	 * @param key the key or null for nameless values
	 * @param value the value to add
	 */
	public void put(LaciToken key, String value) {
		List<String> list = get(key);
		if (list == null) {
			list = new ArrayList<String>();
			multimap.put(key, list);
		}
		list.add(value);
	}

	/**
	 * Inserts multiple values.
	 * @param key the key or null for nameless values
	 * @param values the values to add
	 */
	public void putAll(String key, String... values) {
		putAll(LaciToken.create(key), values);
	}

	/**
	 * Inserts multiple values.
	 * @param key the key or null for nameless values
	 * @param values the values to add
	 */
	public void putAll(LaciToken key, String... values) {
		if (values.length == 0) {
			return;
		}

		List<String> list = get(key);
		if (list == null) {
			list = new ArrayList<String>();
			multimap.put(key, list);
		}
		list.addAll(Arrays.asList(values));
	}

	/**
	 * Replaces all the values of the given key with the given value.
	 * @param key the key or null for nameless values
	 * @param value the value
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replace(String key, String value) {
		return replace(LaciToken.create(key), value);
	}

	/**
	 * Replaces all the values of the given key with the given value.
	 * @param key the key or null for nameless values
	 * @param value the value
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replace(LaciToken key, String value) {
		List<String> replaced = removeAll(key);
		put(key, value);
		return replaced;
	}

	/**
	 * Replaces all the values of the given key with the given values.
	 * @param key the key or null for nameless values
	 * @param values the values
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replaceAll(String key, String... values) {
		return replaceAll(LaciToken.create(key), values);
	}

	/**
	 * Replaces all the values of the given key with the given values.
	 * @param key the key or null for nameless values
	 * @param values the values
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replaceAll(LaciToken key, String... values) {
		List<String> replaced = removeAll(key);
		if (values.length > 0) {
			putAll(key, values);
		}
		return replaced;
	}

	/**
	 * Removes a value.
	 * @param key the key or null for nameless values
	 * @param value the value to remove
	 * @return true if the value was found, false if not
	 */
	public boolean remove(String key, String value) {
		return remove(LaciToken.create(key), value);
	}

	/**
	 * Removes a value.
	 * @param key the key or null for nameless values
	 * @param value the value to remove
	 * @return true if the value was found, false if not
	 */
	public boolean remove(LaciToken key, String value) {
		List<String> values = get(key);
		return (values == null) ? false : values.remove(value);
	}

	/**
	 * Removes all values associated with a key, along with the key itself.
	 * @param key the key or null for nameless values
	 * @return the removed values or null if the key didn't exist
	 */
	public List<String> removeAll(String key) {
		return removeAll(LaciToken.create(key));
	}

	/**
	 * Removes all values associated with a key, along with the key itself.
	 * @param key the key or null for nameless values
	 * @return the removed values or null if the key didn't exist
	 */
	public List<String> removeAll(LaciToken key) {
		return multimap.remove(key);
	}

	/**
	 * Clears the multimap.
	 */
	public void clear() {
		multimap.clear();
	}

	/**
	 * Gets the first value assigned to the given key.
	 * @param key the key or null for nameless values
	 * @return the value or null if the given key does not have any values
	 */
	public String first(String key) {
		return first(LaciToken.create(key));
	}

	/**
	 * Gets the first value assigned to the given key.
	 * @param key the key or null for nameless values
	 * @return the value or null if the given key does not have any values
	 */
	public String first(LaciToken key) {
		List<String> values = get(key);
		return (values == null || values.isEmpty()) ? null : values.get(0);
	}

	/**
	 * Determines if a "quoted-printable encoding" parameter exists.
	 * @return true if the parameter exists, false if not
	 */
	public boolean isQuotedPrintable() {
		for (String key : new String[] { "ENCODING", null }) {
			List<String> values = get(key);
			if (values == null) {
				continue;
			}

			for (String value : values) {
				if ("QUOTED-PRINTABLE".equalsIgnoreCase(value)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the CHARSET parameter.
	 * @return the character set or null if a character set is not defined
	 * @throws IllegalCharsetNameException if the character set name contains
	 * illegal characters
	 * @throws UnsupportedCharsetException if the local JVM does not recognized
	 * the character set
	 */
	public Charset getCharset() throws IllegalCharsetNameException, UnsupportedCharsetException {
		String charsetStr = first("CHARSET");
		return (charsetStr == null) ? null : Charset.forName(charsetStr);
	}

	/**
	 * Gets the map that backs this parameters list.
	 * @return the map
	 */
	public Map<LaciToken, List<String>> getMap() {
		return multimap;
	}

	/**
	 * Creates an iterator over all the parameters (for use in foreach loops).
	 * @return the iterator
	 */
	public Iterator<Entry<LaciToken, List<String>>> iterator() {
		return multimap.entrySet().iterator();
	}

	@Override
	public int hashCode() {
		return multimap.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VObjectParameters other = (VObjectParameters) obj;
		return multimap.equals(other.multimap);
	}

	@Override
	public String toString() {
		return multimap.toString();
	}
}
