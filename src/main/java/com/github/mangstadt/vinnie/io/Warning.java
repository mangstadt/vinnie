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
 * Lists the non-fatal problems that can occur when parsing a vobject data
 * stream.
 * @see VObjectDataListener#onWarning
 * @author Michael Angstadt
 */
public enum Warning {
	/**
	 * When a line cannot be parsed.
	 */
	MALFORMED_LINE("Skipping malformed line (no colon character found)."),

	/**
	 * When a BEGIN property value is empty (it is supposed to contain the name
	 * of a component).
	 */
	EMPTY_BEGIN("Ignoring BEGIN property that does not have a component name."),

	/**
	 * When an END property value is empty (it is supposed to contain the name
	 * of a component).
	 */
	EMPTY_END("Ignoring END property that does not have a component name."),

	/**
	 * When the component in an END property does not match up with any BEGIN
	 * properties.
	 */
	UNMATCHED_END("Ignoring END property that does not match up with any BEGIN properties."),

	/**
	 * When a VERSION property is not recognized by the parser's
	 * {@link SyntaxRules}.
	 */
	UNKNOWN_VERSION("Unknown version number found. Treating it as a regular property."),

	/**
	 * When a property value is encoded in quoted-printable encoding and its
	 * defined character set is not recognized by the JVM.
	 */
	UNKNOWN_CHARSET("The property's character encoding is not supported by this system.  The value will be decoded into the default quoted-printable character encoding."),

	/**
	 * When a property value cannot be decoded from quoted-printable encoding.
	 */
	QUOTED_PRINTABLE_ERROR("Unable to decode the property's quoted-printable value.  Value will be treated as plain-text.");

	private Warning(String message) {
		this.message = message;
	}

	private String message;

	/**
	 * Gets a message describing the warning.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
