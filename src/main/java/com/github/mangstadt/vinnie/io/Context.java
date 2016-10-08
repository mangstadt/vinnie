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

import java.util.Collections;
import java.util.List;

/**
 * Contains information related to the status of a parse operation, such as the
 * parent component hierarchy and line number.
 * @author Michael Angstadt
 * @see VObjectDataListener
 */
public class Context {
	final List<String> parentComponents;
	final Buffer unfoldedLine = new Buffer();
	int lineNumber = 1;
	boolean stop = false;

	Context(List<String> parentComponents) {
		this.parentComponents = Collections.unmodifiableList(parentComponents);
	}

	/**
	 * Gets the hierarchy of parent components the parser is currently inside
	 * of, starting with the outer-most component.
	 * @return the component names (in uppercase; this list is immutable)
	 */
	public List<String> getParentComponents() {
		return parentComponents;
	}

	/**
	 * Gets the raw, unfolded line that was parsed.
	 * @return the raw, unfolded line
	 */
	public String getUnfoldedLine() {
		return unfoldedLine.get();
	}

	/**
	 * Gets the line number of the parsed line. If the line was folded, this
	 * will be the line number of the first line.
	 * @return the line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Instructs the parser to stop parsing and return from the call to
	 * {@link VObjectReader#parse}.
	 */
	public void stop() {
		stop = true;
	}

	@Override
	public String toString() {
		return "Context [parentComponents=" + parentComponents + ", unfoldedLine=" + unfoldedLine.get() + ", lineNumber=" + lineNumber + ", stop=" + stop + "]";
	}
}
