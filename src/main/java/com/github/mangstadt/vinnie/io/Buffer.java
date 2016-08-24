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

/**
 * Wraps a {@link StringBuilder} object, providing utility methods for getting
 * and clearing its value.
 * @author Michael Angstadt
 */
class Buffer {
	private final StringBuilder sb = new StringBuilder(1024);

	/**
	 * Clears the buffer.
	 * @return this
	 */
	public Buffer clear() {
		sb.setLength(0);
		return this;
	}

	/**
	 * Gets the buffer's contents.
	 * @return the buffer's contents
	 */
	public String get() {
		return sb.toString();
	}

	/**
	 * Gets the buffer's contents, then clears it.
	 * @return the buffer's contents
	 */
	public String getAndClear() {
		String string = get();
		clear();
		return string;
	}

	/**
	 * Appends a character to the buffer.
	 * @param ch the character to append
	 * @return this
	 */
	public Buffer append(char ch) {
		sb.append(ch);
		return this;
	}

	/**
	 * Appends a character sequence to the buffer.
	 * @param string the character sequence to append
	 * @return this
	 */
	public Buffer append(CharSequence string) {
		sb.append(string);
		return this;
	}

	/**
	 * Removes the last character from the buffer (does nothing if the buffer is
	 * empty).
	 * @return this
	 */
	public Buffer chop() {
		if (size() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return this;
	}

	/**
	 * Gets the number of characters in the buffer.
	 * @return the buffer's length
	 */
	public int size() {
		return sb.length();
	}
}
