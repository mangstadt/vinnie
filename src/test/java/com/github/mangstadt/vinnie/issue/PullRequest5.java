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

package com.github.mangstadt.vinnie.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Test;

import com.github.mangstadt.vinnie.DefaultLocaleRule;
import com.github.mangstadt.vinnie.LaciToken;
import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.io.Context;
import com.github.mangstadt.vinnie.io.SyntaxRules;
import com.github.mangstadt.vinnie.io.VObjectDataAdapter;
import com.github.mangstadt.vinnie.io.VObjectDataListener;
import com.github.mangstadt.vinnie.io.VObjectReader;
import com.github.mangstadt.vinnie.io.VObjectWriter;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/vinnie/pull/5"
 */
public class PullRequest5 {
	@ClassRule
	public static final DefaultLocaleRule localeRule = new DefaultLocaleRule(new Locale("tr", "TR"));

	@Test
	public void component_names_with_i() throws Exception {
		String input = "begin:x-item\r\nend:x-item\r\n";
		VObjectReader reader = new VObjectReader(new StringReader(input), SyntaxRules.vcard());
		VObjectDataListener listener = mock(VObjectDataListener.class);
		reader.parse(listener);
		reader.close();

		verify(listener).onComponentBegin(eq(new LaciToken("X-ITEM")), any(Context.class));
		verify(listener).onComponentEnd(eq(new LaciToken("X-ITEM")), any(Context.class));
	}

	@Test
	public void lowercase_encoding_parameter_is_encoded() throws Exception {
		StringWriter sw = new StringWriter();
		VObjectWriter writer = new VObjectWriter(sw, SyntaxStyle.NEW);

		VObjectParameters parameters = new VObjectParameters();
		parameters.put("encoding", "QUOTED-PRINTABLE");
		assertTrue(parameters.isQuotedPrintable());

		writer.writeProperty(null, "NAME", parameters, "¡Hola, mundo!");
		writer.close();

		String expected = "NAME;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!\r\n";
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void lowercase_encoding_parameter_is_decoded() throws Exception {
		String input = "NOTE;encoding=QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!\r\n";
		VObjectReader reader = new VObjectReader(new StringReader(input), SyntaxRules.vcard());
		VObjectDataAdapter adapter = spy(new VObjectDataAdapter() {
			@Override
			public void onProperty(VObjectProperty property, Context context) {
				assertTrue(property.getParameters().isQuotedPrintable());
				assertEquals("¡Hola, mundo!", property.getValue());
			}
		});
		reader.parse(adapter);
		reader.close();

		verify(adapter).onProperty(any(VObjectProperty.class), any(Context.class));
	}
}
