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

/**
 * Contains miscellaneous utility methods.
 * @author Michael Angstadt
 */
public final class Utils {
	/**
	 * Trims the whitespace off the left side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String ltrim(String string) {
		int i;
		for (i = 0; i < string.length() && Character.isWhitespace(string.charAt(i)); i++) {
			//do nothing
		}
		return string.substring(i);
	}

	/**
	 * Trims the whitespace off the right side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String rtrim(String string) {
		int i;
		for (i = string.length() - 1; i >= 0 && Character.isWhitespace(string.charAt(i)); i--) {
			//do nothing
		}
		return string.substring(0, i + 1);
	}

	/**
	 * <p>
	 * Escapes all newline sequences in a string with "\n".
	 * </p>
	 * <p>
	 * This method is 3x faster than a regex when the string has newlines to
	 * escape and 6x faster when it doesn't have newlines to escape.
	 * </p>
	 * @param string the string
	 * @return the escaped string
	 */
	public static String escapeNewlines(String string) {
		StringBuilder sb = null;
		char prev = 0;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (c == '\r' || c == '\n') {
				if (sb == null) {
					sb = new StringBuilder(string.length() * 2);
					sb.append(string, 0, i);
				}

				if (c == '\n' && prev == '\r') {
					/*
					 * Do not write a second newline escape sequence if the
					 * newline sequence is "\r\n".
					 */
				} else {
					sb.append("\\n");
				}
			} else if (sb != null) {
				sb.append(c);
			}

			prev = c;
		}
		return (sb == null) ? string : sb.toString();
	}

	private Utils() {
		//hide default constructor
	}
}
