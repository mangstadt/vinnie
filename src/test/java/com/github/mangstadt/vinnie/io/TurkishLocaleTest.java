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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.VObjectProperty;

public class TurkishLocaleTest {
	private Locale originalLocale;

	@Before
	public void setLocale() {
		originalLocale = Locale.getDefault();
		Locale.setDefault(new Locale("tr", "TR"));
	}

	@After
	public void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	@Test
	public void parameter_names_are_case_insensitive() {
		VObjectParameters parameters = new VObjectParameters();
		parameters.put("encoding", "QUOTED-PRINTABLE");
		assertEquals("QUOTED-PRINTABLE", parameters.first("ENCODING"));
		assertTrue(parameters.isQuotedPrintable());
		assertEquals(Arrays.asList("QUOTED-PRINTABLE"), parameters.removeAll("Encoding"));
	}

	@Test
	public void lowercase_encoding_parameter_is_decoded() throws Exception {
		VObjectReader reader = new VObjectReader(new StringReader("NOTE;encoding=QUOTED-PRINTABLE:hello=20world\r\n"), SyntaxRules.vcard());
		final int[] properties = { 0 };
		reader.parse(new VObjectDataAdapter() {
			@Override
			public void onProperty(VObjectProperty property, Context context) {
				assertEquals("hello world", property.getValue());
				properties[0]++;
			}
		});
		reader.close();
		assertEquals(1, properties[0]);
	}

	@Test
	public void component_names_are_locale_independent() throws Exception {
		VObjectReader reader = new VObjectReader(new StringReader("BEGIN:x-item\r\nEND:x-item\r\n"), SyntaxRules.vcard());
		final StringBuilder components = new StringBuilder();
		reader.parse(new VObjectDataAdapter() {
			@Override
			public void onComponentBegin(String name, Context context) {
				components.append(name).append(';');
			}

			@Override
			public void onComponentEnd(String name, Context context) {
				components.append(name);
			}
		});
		reader.close();
		assertEquals("X-ITEM;X-ITEM", components.toString());
	}

	@Test
	public void syntax_rules_are_case_insensitive() {
		SyntaxRules rules = new SyntaxRules(SyntaxStyle.OLD);
		rules.addRule("x-item", "1.0", SyntaxStyle.NEW);
		assertTrue(rules.hasSyntaxRules("X-ITEM"));
		assertEquals(SyntaxStyle.NEW, rules.getSyntaxStyle("X-ITEM", "1.0"));
		rules.addRule("X-ITEM", "2.0", SyntaxStyle.NEW);
		assertTrue(rules.hasSyntaxRules("x-item"));
		assertEquals(SyntaxStyle.NEW, rules.getSyntaxStyle("x-item", "2.0"));
	}

	@Test
	public void multimap_keys_are_case_insensitive() {
		Map<String, List<String>> expected = new LinkedHashMap<String, List<String>>();
		expected.put("INTERVAL", Arrays.asList("2"));
		expected.put("X-FIRST", Arrays.asList(""));
		expected.put("X-FINAL", Arrays.asList(""));
		assertEquals(expected, VObjectPropertyValues.parseMultimap("interval=2;x-first;x-final"));

		Map<String, List<String>> input = new LinkedHashMap<String, List<String>>();
		input.put("interval", Arrays.asList("2"));
		assertEquals("INTERVAL=2", VObjectPropertyValues.writeMultimap(input));
	}
}
