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

/*
 * This class is not being used because it's about 6% slower than directly
 * applying the pattern (e.g. see VObjectPropertyValues.unescape()).
 */

/**
 * A StringBuilder that only gets created if the source string is modified. Used
 * when a string needs to be modified in some way, but is likely to end up not
 * needing modification (e.g. unescaping backslash-escaped characters).
 * @author Michael Angstadt
 */
public class LazyStringBuilder {
	private final String source;
	private final int start;
	private final int end;
	private StringBuilder sb;

	/**
	 * Creates a lazy string builder.
	 * @param source the source string
	 */
	public LazyStringBuilder(String source) {
		this(source, 0, source.length());
	}

	/**
	 * Creates a lazy string builder for a substring of a string.
	 * @param source the source string
	 * @param start the start index of the substring
	 * @param end the end index of the substring
	 */
	public LazyStringBuilder(String source, int start, int end) {
		this.source = source;
		this.start = start;
		this.end = end;
	}

	/**
	 * Allocates the StringBuilder object because a change to the source string
	 * is required. Copies all characters from the start of the source string to
	 * the given position. Does nothing if the StringBuilder is already created.
	 * @param pos the index to stop copying characters (exclusive)
	 * @return this
	 */
	public LazyStringBuilder fork(int pos) {
		if (sb == null) {
			sb = new StringBuilder((end - start) * 2);
			sb.append(source, start, pos);
		}
		return this;
	}

	public LazyStringBuilder append(char c) {
		if (sb != null) {
			sb.append(c);
		}
		return this;
	}

	public LazyStringBuilder append(String s) {
		if (sb != null) {
			sb.append(s);
		}
		return this;
	}

	public String toString() {
		if (sb == null) {
			/*
			 * The "String#substring" method makes no guarantee that the same
			 * String object will be returned if the entire string length is
			 * passed into the method.
			 */
			if (start == 0 && end == source.length()) {
				return source;
			}

			return source.substring(start, end);
		}

		return sb.toString();
	}
}