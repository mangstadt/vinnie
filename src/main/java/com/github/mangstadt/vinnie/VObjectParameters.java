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
 * A simple multimap implementation for holding the parameters of a
 * {@link VObjectProperty}. Enforces case-insensitivity of parameter names by
 * converting them to uppercase.
 * @author Michael Angstadt
 */
public class VObjectParameters implements Iterable<Map.Entry<String, List<String>>> {
	private final Map<String, List<String>> multimap;

	/**
	 * Creates an empty list of parameters.
	 */
	public VObjectParameters() {
		multimap = new LinkedHashMap<String, List<String>>(); //preserve insertion order of keys
	}

	/**
	 * <p>
	 * Creates a list of parameters backed by the given map. Any changes made to
	 * the given map will effect the parameter list and vice versa.
	 * </p>
	 * <p>
	 * If the given map is not empty, care should be taken to ensure that all of
	 * its keys are in uppercase before passing it into this constructor.
	 * </p>
	 * @param map the map
	 */
	public VObjectParameters(Map<String, List<String>> map) {
		multimap = map;
	}

	/**
	 * Copies an existing list of parameters.
	 * @param original the existing list
	 */
	public VObjectParameters(VObjectParameters original) {
		this();
		for (Map.Entry<String, List<String>> entry : original) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			multimap.put(name, new ArrayList<String>(values));
		}
	}

	/**
	 * Gets the values that are assigned to a key.
	 * @param key the key
	 * @return the values or null if the key does not exist
	 */
	public List<String> get(String key) {
		key = sanitizeKey(key);
		return _get(key);
	}

	/**
	 * @param key assumed to already be in uppercase
	 */
	private List<String> _get(String key) {
		return multimap.get(key);
	}

	/**
	 * Inserts a value.
	 * @param key the key
	 * @param value the value to add
	 */
	public void put(String key, String value) {
		key = sanitizeKey(key);
		_put(key, value);
	}

	/**
	 * @param key assumed to already be in uppercase
	 * @param value the value to add
	 */
	private void _put(String key, String value) {
		List<String> list = _get(key);
		if (list == null) {
			list = new ArrayList<String>();
			multimap.put(key, list);
		}
		list.add(value);
	}

	/**
	 * Inserts multiple values.
	 * @param key the key
	 * @param values the values to add
	 */
	public void putAll(String key, String... values) {
		if (values.length == 0) {
			return;
		}
		key = sanitizeKey(key);
		_putAll(key, values);
	}

	/**
	 * @param key assumed to already be in uppercase
	 * @param values the values to add
	 */
	private void _putAll(String key, String... values) {
		List<String> list = _get(key);
		if (list == null) {
			list = new ArrayList<String>();
			multimap.put(key, list);
		}
		list.addAll(Arrays.asList(values));
	}

	/**
	 * Replaces all the values of the given key with the given value.
	 * @param key the key
	 * @param value the value
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replace(String key, String value) {
		key = sanitizeKey(key);
		List<String> replaced = _removeAll(key);
		_put(key, value);
		return replaced;
	}

	/**
	 * Replaces all the values of the given key with the given values.
	 * @param key the key
	 * @param values the values
	 * @return the replaced values or null if the key didn't exist
	 */
	public List<String> replaceAll(String key, String... values) {
		key = sanitizeKey(key);
		List<String> replaced = _removeAll(key);
		if (values.length > 0) {
			_putAll(key, values);
		}
		return replaced;
	}

	/**
	 * Removes a value.
	 * @param key the key
	 * @param value the value to remove
	 * @return true if the value was found, false if not
	 */
	public boolean remove(String key, String value) {
		List<String> values = get(key);
		return (values == null) ? false : values.remove(value);
	}

	/**
	 * Removes all values associated with a key, along with the key itself.
	 * @param key the key
	 * @return the removed values or null if the key didn't exist
	 */
	public List<String> removeAll(String key) {
		key = sanitizeKey(key);
		return _removeAll(key);
	}

	/**
	 * @param key assumed to already be in uppercase
	 */
	private List<String> _removeAll(String key) {
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
	 * @param key the key
	 * @return the value or null if the given key does not have any values
	 */
	public String first(String key) {
		List<String> values = get(key);
		return (values == null || values.isEmpty()) ? null : values.get(0);
	}

	/**
	 * Determines if a "quoted-printable encoding" parameter exists.
	 * @return true if the parameter exists, false if not
	 */
	public boolean isQuotedPrintable() {
		for (String key : new String[] { "ENCODING", null }) {
			List<String> values = _get(key);
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
	public Map<String, List<String>> getMap() {
		return multimap;
	}

	/**
	 * Creates an iterator over all the parameters (for use in foreach loops).
	 * @return the iterator
	 */
	public Iterator<Entry<String, List<String>>> iterator() {
		return multimap.entrySet().iterator();
	}

	/**
	 * Converts the given key to uppercase. Call this method before passing a
	 * key to the multimap.
	 * @param key the key
	 * @return the sanitized key
	 */
	private String sanitizeKey(String key) {
		return (key == null) ? null : key.toUpperCase();
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
