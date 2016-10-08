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

import com.github.mangstadt.vinnie.VObjectProperty;

/**
 * Callback interface used by {@link VObjectReader} for handling data as it is
 * parsed off the data stream.
 * @see VObjectReader#parse
 * @author Michael Angstadt
 */
public interface VObjectDataListener {
	/**
	 * Called when a component begins (in other words, when a BEGIN property is
	 * encountered).
	 * @param name the component name (will always be in uppercase)
	 * @param context the parse context
	 */
	void onComponentBegin(String name, Context context);

	/**
	 * <p>
	 * Called when a component ends (in other words, when an END property is
	 * encountered).
	 * </p>
	 * <p>
	 * If a component ends before any of its subcomponents end, then the parser
	 * will generate "onComponentEnd" events for each subcomponent.
	 * </p>
	 * 
	 * <p>
	 * For example, the following data will cause the following sequence of
	 * events to occur. Notice that the outer component, A, ends before its
	 * subcomponents.
	 * </p>
	 * 
	 * <p>
	 * <b>Data:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * BEGIN:A
	 * BEGIN:B
	 * BEGIN:C
	 * END:A
	 * END:C
	 * END:B
	 * </pre>
	 * <p>
	 * <b>Sequence of events:</b>
	 * </p>
	 * <ol>
	 * <li>onComponentBegin(): name="A", parentComponents=[]</li>
	 * <li>onComponentBegin(): name="B", parentComponents=["A"]</li>
	 * <li>onComponentBegin(): name="C", parentComponents=["A", "B"]</li>
	 * <li>onComponentEnd(): name="C", parentComponents=["A", "B"]</li>
	 * <li>onComponentEnd(): name="B", parentComponents=["A"]</li>
	 * <li>onComponentEnd(): name="A", parentComponents=[]</li>
	 * <li>onWarning(): UNMATCHED_END</li>
	 * <li>onWarning(): UNMATCHED_END</li>
	 * </ol>
	 * @param name the component name (will always be in uppercase)
	 * @param context the parse context
	 */
	void onComponentEnd(String name, Context context);

	/**
	 * Called when a property is read.
	 * @param property the property
	 * @param context the parse context
	 */
	void onProperty(VObjectProperty property, Context context);

	/**
	 * Called when a VERSION property is read whose value and position (as
	 * defined in {@link SyntaxRules}) are recognized as valid.
	 * @param value the version string
	 * @param context the parse context
	 */
	void onVersion(String value, Context context);

	/**
	 * Called when a non-fatal error occurs during parsing.
	 * @param warning the warning
	 * @param property the property that the warning is associated with, or null
	 * if the warning is not associated with a property
	 * @param thrown the exception that was thrown or null if no exception was
	 * thrown
	 * @param context the parse context
	 */
	void onWarning(Warning warning, VObjectProperty property, Exception thrown, Context context);
}
