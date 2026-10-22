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

import java.util.Locale;

/**
 * <p>
 * A <b>L</b>ocale-<b>A</b>gnostic, <b>C</b>ase-<b>I</b>nsensitive string. Used
 * for string values that must remain machine-readable under all locales and
 * letter casings (upper/lower).
 * </p>
 * <p>
 * Uses {@link Locale#ROOT} when converting to upper/lower case to avoid issues
 * where some English characters are converted to non-English characters under
 * certain locales. Letter casing of the source string is not retained. All
 * comparisons are case-insensitive.
 * </p>
 * @author Michael Angstadt
 * @see String#toLowerCase(Locale)
 * @see "https://github.com/mangstadt/ez-vcard/pull/166"
 */
public class LaciToken implements CharSequence, Comparable<LaciToken> {
	private final String upper;

	/**
	 * Creates a LaciToken or returns null if the given string is null.
	 * @param string the string to use
	 * @return the LaciString or null if the given string is null
	 */
	public static LaciToken create(String string) {
		return (string == null) ? null : new LaciToken(string);
	}

	/**
	 * Creates a new LaciToken.
	 * @param string the string to use (cannot be null)
	 */
	public LaciToken(String string) {
		upper = string.toUpperCase(Locale.ROOT);
	}

	/**
	 * Converts the string to lower case using {@link Locale#ROOT}.
	 * @return the string in lower case
	 */
	public String toLowerCase() {
		return upper.toLowerCase(Locale.ROOT);
	}

	/**
	 * Converts the string to upper case using {@link Locale#ROOT}.
	 * @return the string in upper case
	 */
	public String toUpperCase() {
		return upper;
	}

	/**
	 * Determines whether the given string matches this string using
	 * case-insensitive comparison.
	 * @param str the string
	 * @return true if it matches, false if not
	 */
	public boolean equalsIgnoreCase(String str) {
		return equals(create(str));
	}

	@Override
	public int hashCode() {
		return upper.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LaciToken other = (LaciToken) obj;
		return upper.equals(other.upper);
	}

	/**
	 * Determines if the string has a length of 0.
	 * @return true if length is 0, false if not
	 */
	public boolean isEmpty() {
		return upper.isEmpty();
	}

	/**
	 * <p>
	 * Returns a string representation of this token.
	 * </p>
	 * <p>
	 * Letter casing not guaranteed to be consistent. This method should not be
	 * used for application logic.
	 * </p>
	 */
	@Override
	public String toString() {
		return upper;
	}

	@Override
	public int length() {
		return upper.length();
	}

	@Override
	public char charAt(int index) {
		return upper.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return upper.subSequence(start, end);
	}

	@Override
	public int compareTo(LaciToken other) {
		return upper.compareTo(other.upper);
	}
}
