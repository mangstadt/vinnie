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

import com.github.mangstadt.vinnie.io.Context;
import com.github.mangstadt.vinnie.io.VObjectDataListener;
import com.github.mangstadt.vinnie.io.Warning;

/**
 * Used for debugging purposes in the unit tests.
 * @author Michael Angstadt
 */
public class DebugDataListener implements VObjectDataListener {
	public void onComponentBegin(String name, Context context) {
		System.out.println("onComponentBegin(\"" + name + "\", " + context + ")");
	}

	public void onComponentEnd(String name, Context context) {
		System.out.println("onComponentEnd(\"" + name + "\", " + context + ")");
	}

	public void onProperty(VObjectProperty property, Context context) {
		System.out.println("onProperty(" + property + ", " + context + ")");
	}

	public void onVersion(String value, Context context) {
		System.out.println("onVersion(\"" + value + "\", " + context + ")");
	}

	public void onWarning(Warning warning, VObjectProperty property, Exception thrown, Context context) {
		System.out.println("onWarning(" + warning.name() + ", " + property + ", " + (thrown == null ? null : thrown.getMessage()) + ", " + context + ")");
	}
}
