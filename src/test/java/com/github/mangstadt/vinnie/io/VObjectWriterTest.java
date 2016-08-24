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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.VObjectProperty;

/**
 * @author Michael Angstadt
 */
@SuppressWarnings("resource")
public class VObjectWriterTest {
	@Test
	public void writeBeginComponent() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			writer.writeBeginComponent("COMP");
			writer.writeBeginComponent(" ");
			try {
				writer.writeBeginComponent("");
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}
			try {
				writer.writeBeginComponent(null);
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"BEGIN:COMP\r\n" +
			"BEGIN: \r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void writeEndComponent() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			writer.writeEndComponent("COMP");
			writer.writeEndComponent(" ");
			try {
				writer.writeEndComponent("");
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}
			try {
				writer.writeEndComponent(null);
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"END:COMP\r\n" +
			"END: \r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void writeVersion() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			writer.writeVersion("1");
			writer.writeVersion(" ");
			try {
				writer.writeVersion("");
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}
			try {
				writer.writeVersion(null);
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"VERSION:1\r\n" +
			"VERSION: \r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void write_VObjectProperty() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			VObjectProperty property = new VObjectProperty();
			property.setGroup("group");
			property.setName("PROP");
			property.getParameters().put("PARAM", "pvalue");
			property.setValue("value");

			writer.writeProperty(property);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"group.PROP;PARAM=pvalue:value\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void setCaretDecoding() {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			assertFalse(writer.isCaretEncodingEnabled());
			writer.setCaretEncodingEnabled(true);
			assertTrue(writer.isCaretEncodingEnabled());
		}
	}

	@Test
	public void setSyntaxStyle() {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			assertEquals(style, writer.getSyntaxStyle());
			for (SyntaxStyle style2 : SyntaxStyle.values()) {
				writer.setSyntaxStyle(style2);
				assertEquals(style2, writer.getSyntaxStyle());
			}
		}
	}

	@Test
	public void close() throws Exception {
		Writer w = mock(Writer.class);
		VObjectWriter writer = new VObjectWriter(w, SyntaxStyle.OLD);
		writer.close();

		verify(w).close();
	}

	@Test
	public void flush() throws Exception {
		Writer w = mock(Writer.class);
		VObjectWriter writer = new VObjectWriter(w, SyntaxStyle.OLD);
		writer.flush();

		verify(w).flush();
	}

	@Test
	public void group() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			writer.writeProperty("group", "PROP", new VObjectParameters(), "value");
			writer.writeProperty("", "PROP", new VObjectParameters(), "value");
			writer.writeProperty(null, "PROP", new VObjectParameters(), "value");

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"group.PROP:value\r\n" +
			"PROP:value\r\n" +
			"PROP:value\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void group_invalid_characters() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			for (char c : ".;:\n\r".toCharArray()) {
				try {
					writer.writeProperty(c + "", "PROP", new VObjectParameters(), "");
					fail("IllegalArgumentException expected when group name contains character " + ch(c) + " and style is " + style.name());
				} catch (IllegalArgumentException e) {
					//expected
				}
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void group_starts_with_whitespace() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			for (char c : " \t".toCharArray()) {
				try {
					writer.writeProperty(c + "", "PROP", new VObjectParameters(), "");
					fail("IllegalArgumentException expected when group name starts with character " + ch(c) + " and style is " + style.name());
				} catch (IllegalArgumentException e) {
					//expected
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void property_name() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);

			writer.writeProperty(null, "PROP", new VObjectParameters(), "");

			try {
				writer.writeProperty(null, "", new VObjectParameters(), "");
				fail("IllegalArgumentException expected when property name is empty and style is " + style.name());
			} catch (IllegalArgumentException e) {
				//expected
			}

			try {
				writer.writeProperty(null, null, new VObjectParameters(), "");
				fail("NullPointerException expected when property name is null and style is " + style.name());
			} catch (NullPointerException e) {
				//expected
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP:\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void property_name_invalid_characters() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			for (char c : ".;:\n\r".toCharArray()) {
				try {
					writer.writeProperty(null, c + "", new VObjectParameters(), "");
					fail("IllegalArgumentException expected when property name contains character " + ch(c) + " and style is " + style.name());
				} catch (IllegalArgumentException e) {
					//expected
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void property_name_starts_with_whitespace() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			for (char c : " \t".toCharArray()) {
				try {
					writer.writeProperty(null, c + "", new VObjectParameters(), "");
					fail("IllegalArgumentException expected when property name starts with character " + ch(c) + " and style is " + style.name());
				} catch (IllegalArgumentException e) {
					//expected
				}

				writer.writeProperty(null, "PROP" + c, new VObjectParameters(), "");
			}

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP :\r\n" +
			"PROP	:\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	/**
	 * When the parameters multimap has a key with an empty list, the parameter
	 * should not be written. This should never happen if the user sticks to the
	 * API of the VObjectParameters class and does not modify the backing map
	 * manually.
	 */
	@Test
	public void parameters_multivalued_empty_values() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);

				VObjectParameters parameters = new VObjectParameters();
				parameters.getMap().put("PARAM", new ArrayList<String>());
				writer.writeProperty(null, "PROP", parameters, "");

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP:\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void parameters_multivalued() throws Exception {
		List<VObjectParameters> list = new ArrayList<VObjectParameters>();

		VObjectParameters parameters = new VObjectParameters();
		parameters.put("SINGLE", "one");
		list.add(parameters);

		parameters = new VObjectParameters();
		parameters.put("MULTIPLE", "one");
		parameters.put("MULTIPLE", "two");
		list.add(parameters);

		parameters = new VObjectParameters();
		parameters.put("SINGLE", "one");
		parameters.put("MULTIPLE", "one");
		parameters.put("MULTIPLE", "two");
		list.add(parameters);

		SyntaxStyle style = SyntaxStyle.OLD;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);
				for (VObjectParameters p : list) {
					writer.writeProperty(null, "PROP", p, "");
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;SINGLE=one:\r\n" +
				"PROP;MULTIPLE=one;MULTIPLE=two:\r\n" +
				"PROP;SINGLE=one;MULTIPLE=one;MULTIPLE=two:\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}

		style = SyntaxStyle.NEW;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);
				for (VObjectParameters p : list) {
					writer.writeProperty(null, "PROP", p, "");
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;SINGLE=one:\r\n" +
				"PROP;MULTIPLE=one,two:\r\n" +
				"PROP;SINGLE=one;MULTIPLE=one,two:\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void parameters_nameless() throws Exception {
		List<VObjectParameters> list = new ArrayList<VObjectParameters>();

		VObjectParameters parameters = new VObjectParameters();
		parameters.put(null, "one");
		list.add(parameters);

		parameters = new VObjectParameters();
		parameters.put(null, "one");
		parameters.put(null, "two");
		list.add(parameters);

		SyntaxStyle style = SyntaxStyle.OLD;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);
				for (VObjectParameters p : list) {
					writer.writeProperty(null, "PROP", p, "");
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;one:\r\n" +
				"PROP;one;two:\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}

		style = SyntaxStyle.NEW;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);
				for (VObjectParameters p : list) {
					try {
						writer.writeProperty(null, "PROP", p, "");
						fail("IllegalArgumentException expected when property name contains nameless parameter and style is " + style.name());
					} catch (IllegalArgumentException e) {
						//expected
					}
				}

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void parameters_invalid_characters_in_name() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.setCaretEncodingEnabled(caretEncoding);
				for (char c : ";:=\n\r".toCharArray()) {
					VObjectParameters parameters = new VObjectParameters();
					parameters.put(c + "", "");
					try {
						writer.writeProperty(null, "PROP", parameters, "");
						fail("IllegalArgumentException expected when parameter name contains character " + ch(c) + " and style is " + style.name());
					} catch (IllegalArgumentException e) {
						//expected
					}

					String actual = sw.toString();

					//@formatter:off
					String expected =
					"";
					//@formatter:on

					assertEquals(expected, actual);
				}
			}
		}
	}

	/**
	 * When there are invalid characters in a parameter value.
	 */
	@Test
	public void parameters_invalid_characters_in_value() throws Exception {
		SyntaxStyle style = SyntaxStyle.OLD;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				for (char c : ":\n\r".toCharArray()) {
					StringWriter sw = new StringWriter();
					VObjectWriter writer = new VObjectWriter(sw, style);
					writer.setCaretEncodingEnabled(caretEncoding);

					VObjectParameters parameters = new VObjectParameters();
					parameters.put("PARAM", c + "");

					try {
						writer.writeProperty(null, "PROP", parameters, "");
						fail("IllegalArgumentException expected when parameter value contains character " + ch(c) + " and caret encoding is " + caretEncoding + " and style is " + style.name());
					} catch (IllegalArgumentException e) {
						//expected
					}

					String actual = sw.toString();

					//@formatter:off
					String expected =
					"";
					//@formatter:on

					assertEquals(expected, actual);
				}
			}
		}

		style = SyntaxStyle.NEW;
		{
			boolean caretEncoding = false;
			{
				for (char c : "\"\n\r".toCharArray()) {
					StringWriter sw = new StringWriter();
					VObjectWriter writer = new VObjectWriter(sw, style);
					writer.setCaretEncodingEnabled(caretEncoding);

					VObjectParameters parameters = new VObjectParameters();
					parameters.put("PARAM", c + "");
					try {
						writer.writeProperty(null, "PROP", parameters, "");
						fail("IllegalArgumentException expected when parameter value contains character " + ch(c) + " and caret encoding is " + caretEncoding + " and style is " + style.name());
					} catch (IllegalArgumentException e) {
						//expected
					}

					String actual = sw.toString();

					//@formatter:off
					String expected =
					"";
					//@formatter:on

					assertEquals(expected, actual);
				}
			}

			caretEncoding = true;
			{
				//no characters are disallowed
			}
		}
	}

	/**
	 * When escapable characters exist in a parameter value.
	 */
	@Test
	public void parameters_escape_special_characters_in_value() throws Exception {
		//Old style:
		//Replaces \ with \\
		//Replaces ; with \;
		SyntaxStyle style = SyntaxStyle.OLD;
		{
			for (boolean caretEncoding : new boolean[] { false, true }) {
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.getFoldedLineWriter().setLineLength(null);
				writer.setCaretEncodingEnabled(caretEncoding);

				String input = testString(":\r\n");
				String expectedOutput = input.replace("\\", "\\\\").replace(";", "\\;");

				VObjectParameters parameters = new VObjectParameters();
				parameters.put("PARAM", input);
				writer.writeProperty(null, "PROP", parameters, "");

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;PARAM=" + expectedOutput + ":\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}

		style = SyntaxStyle.NEW;
		{
			//New style without caret escaping
			//surrounds in double quotes, since it contains , ; or :
			boolean caretEncoding = false;
			{
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.getFoldedLineWriter().setLineLength(null);
				writer.setCaretEncodingEnabled(caretEncoding);

				String input = testString("\"\r\n");
				String expectedOutput = input;

				VObjectParameters parameters = new VObjectParameters();
				parameters.put("PARAM", input);
				writer.writeProperty(null, "PROP", parameters, "");

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;PARAM=\"" + expectedOutput + "\":\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}

			//New style with caret escaping
			//replaces ^ with ^^
			//replaces newline with ^n
			//replaces " with ^'
			//surrounds in double quotes, since it contains , ; or :
			caretEncoding = true;
			{
				StringWriter sw = new StringWriter();
				VObjectWriter writer = new VObjectWriter(sw, style);
				writer.getFoldedLineWriter().setLineLength(null);
				writer.setCaretEncodingEnabled(caretEncoding);

				String input = testString("\r\n") + "\r\n\n\r"; //make sure all three kinds of newline sequences are handled
				String expectedOutput = input.replace("^", "^^").replace("\"", "^'").replace("\r\n", "^n").replace("\r", "^n").replace("\n", "^n");

				VObjectParameters parameters = new VObjectParameters();
				parameters.put("PARAM", input);
				writer.writeProperty(null, "PROP", parameters, "");

				String actual = sw.toString();

				//@formatter:off
				String expected =
				"PROP;PARAM=\"" + expectedOutput + "\":\r\n";
				//@formatter:on

				assertEquals(expected, actual);
			}
		}
	}

	/**
	 * When the property value is null, it should treat the value as an empty
	 * string.
	 */
	@Test
	public void property_value_null() throws Exception {
		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			writer.writeProperty(null, "PROP", new VObjectParameters(), null);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP:\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	/**
	 * When the property value contains newlines, it should encode in
	 * quoted-printable in old style, and escape newlines in new style.
	 */
	@Test
	public void property_value_with_newlines() throws Exception {
		SyntaxStyle style = SyntaxStyle.OLD;
		{
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			writer.getFoldedLineWriter().setLineLength(null);

			VObjectParameters parameters = new VObjectParameters();
			VObjectParameters expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\r\ntwo");
			assertEquals(expectedParams, parameters); //nothing should be added to the parameters object that was passed into the method

			parameters = new VObjectParameters();
			expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\rtwo");
			assertEquals(expectedParams, parameters);

			parameters = new VObjectParameters();
			expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\ntwo");
			assertEquals(expectedParams, parameters);

			parameters = new VObjectParameters();
			parameters.put(null, "QUOTED-PRINTABLE");
			expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\r\ntwo");
			assertEquals(expectedParams, parameters);

			parameters = new VObjectParameters();
			parameters.put("ENCODING", "QUOTED-PRINTABLE");
			expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\r\ntwo");
			assertEquals(expectedParams, parameters);

			parameters = new VObjectParameters();
			parameters.put("CHARSET", "UTF-16");
			expectedParams = new VObjectParameters(parameters);
			writer.writeProperty(null, "PROP", parameters, "one\r\ntwo");
			assertEquals(expectedParams, parameters);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0Dtwo\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0Atwo\r\n" +
			"PROP;QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n" +
			"PROP;CHARSET=UTF-16;ENCODING=QUOTED-PRINTABLE:=FE=FF=00o=00n=00e=00=0D=00=0A=00t=00w=00o\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}

		style = SyntaxStyle.NEW;
		{
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			writer.writeProperty(null, "PROP", new VObjectParameters(), "one\r\ntwo");
			writer.writeProperty(null, "PROP", new VObjectParameters(), "one\rtwo");
			writer.writeProperty(null, "PROP", new VObjectParameters(), "one\ntwo");

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP:one\\ntwo\r\n" +
			"PROP:one\\ntwo\r\n" +
			"PROP:one\\ntwo\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	/**
	 * When a QUOTED-PRINTABLE parameter value is present, the writer should
	 * encode the value in quoted-printable.
	 */
	@Test
	public void property_value_quoted_printable() throws Exception {
		final String propValue = "value \u00e4\u00f6\u00fc\u00df";

		for (SyntaxStyle style : SyntaxStyle.values()) {
			StringWriter sw = new StringWriter();
			VObjectWriter writer = new VObjectWriter(sw, style);
			writer.getFoldedLineWriter().setLineLength(null);

			//no parameters
			VObjectParameters parameters = new VObjectParameters();
			writer.writeProperty(null, "PROP", parameters, propValue);

			//no charset
			parameters = new VObjectParameters();
			parameters.put("ENCODING", "QUOTED-PRINTABLE");
			writer.writeProperty(null, "PROP", parameters, propValue);

			//UTF-8
			parameters = new VObjectParameters();
			parameters.put("ENCODING", "QUOTED-PRINTABLE");
			parameters.put("CHARSET", "UTF-8");
			writer.writeProperty(null, "PROP", parameters, propValue);

			//UTF-16
			parameters = new VObjectParameters();
			parameters.put("ENCODING", "QUOTED-PRINTABLE");
			parameters.put("CHARSET", "UTF-16");
			writer.writeProperty(null, "PROP", parameters, propValue);

			//invalid
			parameters = new VObjectParameters();
			parameters.put("ENCODING", "QUOTED-PRINTABLE");
			parameters.put("CHARSET", "invalid");
			writer.writeProperty(null, "PROP", parameters, propValue);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"PROP:" + propValue + "\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:value =C3=A4=C3=B6=C3=BC=C3=9F\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:value =C3=A4=C3=B6=C3=BC=C3=9F\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-16:=FE=FF=00v=00a=00l=00u=00e=00 =00=E4=00=F6=00=FC=00=DF\r\n" +
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:value =C3=A4=C3=B6=C3=BC=C3=9F\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	/**
	 * Creates a printable string representation of a given character if the
	 * character is not printable, or just returns the character if it is
	 * printable.
	 * @param c the character
	 * @return the string representation
	 */
	private static String ch(char c) {
		switch (c) {
		case '\n':
			return "\\n";
		case '\r':
			return "\\r";
		case '\t':
			return "\\t";
		case ' ':
			return "<space>";
		}

		if (isPrintableChar(c)) {
			return c + "";
		}

		int code = (int) c;
		return "(" + code + ")";
	}

	/**
	 * Creates a string containing all the ASCII characters from 0 to 127.
	 * @param exclude a string containing the characters to exclude
	 * @return the string
	 */
	private static String testString(String exclude) {
		StringBuilder sb = new StringBuilder(128);
		for (char c = 0; c < 128; c++) {
			boolean contains = false;
			for (int i = 0; i < exclude.length(); i++) {
				if (c == exclude.charAt(i)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Determines if the given character is printable.
	 * @param c the character
	 * @return true if it's printable, false if not
	 * @see "http://stackoverflow.com/a/418560/13379"
	 */
	private static boolean isPrintableChar(char c) {
		return c > 31 && c < 127;
	}

	@SuppressWarnings("unused")
	private static String printable(String string) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (isPrintableChar(c)) {
				sb.append(c);
			} else {
				int code = (int) c;
				sb.append('(').append(code).append(')');
			}
		}
		return sb.toString();
	}
}
