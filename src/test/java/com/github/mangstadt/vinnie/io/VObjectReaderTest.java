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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.io.StringReader;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.InOrder;

import com.github.mangstadt.vinnie.DebugDataListener;
import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.codec.DecoderException;

/**
 * @author Michael Angstadt
 */
//"resource": No need to call VObjectReader.close()
@SuppressWarnings({ "resource" })
public class VObjectReaderTest {
	/**
	 * Asserts that the component hierarchy is correctly parsed.
	 */
	@Test
	public void structure() throws Exception {
		//@formatter:off
		String string =
		"PROP1:value1\r\n" +
		"BEGIN:COMP1\r\n" +
			"PROP2:value2\r\n" +
			"BEGIN:COMP2\r\n" +
				"PROP3:value3\r\n" +
			"END:COMP2\r\n" +
			"PROP4:value4\r\n" +
		"END:COMP1";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP1").value("value1").build()), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value2").build()), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP3").value("value3").build()), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP4").value("value4").build()), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1", "COMP2"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Asserts that a warning should be thrown when an unmatched END property is
	 * found.
	 */
	@Test
	public void structured_extra_end() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
		"PROP:value\r\n" +
		"END:COMP2\r\n" +
		"END:COMP1\r\n";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("value").build()), any(Context.class));
			inorder.verify(listener).onWarning(eq(Warning.UNMATCHED_END), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Asserts what happens when BEGIN/END components are not nested correctly.
	 */
	@Test
	public void structure_components_out_of_order() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
		"PROP1:value1\r\n" +
		"BEGIN:COMP2\r\n" +
		"PROP2:value2\r\n" +
		"END:COMP1\r\n" + //this also ends COMP2
		"PROP3:value3\r\n" +
		"END:COMP2\r\n";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP1").value("value1").build()), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value2").build()), any(Context.class));

			/*
			 * COMP2 is ended even though its END property wasn't reached
			 * because COMP1 ended and COMP2 is nested inside of COMP1.
			 */
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));

			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP3").value("value3").build()), any(Context.class));
			inorder.verify(listener).onWarning(eq(Warning.UNMATCHED_END), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1", "COMP2"), lines[line], ++line),
				context(asList("COMP1"), lines[line], line+1),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When the stream ends, but the components haven't ended.
	 */
	@Test
	public void structure_missing_end() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
		"PROP1:value1\r\n" +
		"BEGIN:COMP2\r\n" +
		"PROP2:value2\r\n";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP1").value("value1").build()), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value2").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1", "COMP2"), lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When a BEGIN or END property value is empty.
	 */
	@Test
	public void structure_no_component_name() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:\r\n" +
		"BEGIN: \r\n" +
		"PROP:value\r\n" +
		"END:\r\n" +
		"END: \r\n";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener, times(2)).onWarning(eq(Warning.EMPTY_BEGIN), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("value").build()), any(Context.class));
			inorder.verify(listener, times(2)).onWarning(eq(Warning.EMPTY_END), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Asserts the case-sensitivity of the parts of a vobject.
	 */
	@Test
	public void case_insensitivity() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
		"group.prop;param=param_value:prop_value\r\n" +
		"end:comp1\r\n" +
		"BEGIN:comp2\r\n" +
		"end:COMP2";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().group("group").name("prop").param("PARAM", "param_value").value("prop_value").build()), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When checking for BEGIN and END properties, the property name and value
	 * should be trimmed so that any whitespace around the colon ignored.
	 * Whitespace around the colon is allowed by old style syntax, though it
	 * never happens in practice.
	 */
	@Test
	public void whitespace_around_component_names() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
		"BEGIN:COMP2\r\n" +
		"END : COMP2 \r\n" +
		"END\t:\tCOMP1\t";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Incorrect newline sequences should be accepted.
	 */
	@Test
	public void wrong_newlines() throws Exception {
		//@formatter:off
		String string =
		"PROP1:value1\r" +
		"PROP2:value2\n" +
		"PROP3:value3";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP1").value("value1").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value2").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP3").value("value3").build()), any(Context.class));

			//@formatter:off
			int line = 0;
			assertContexts(listener,
				context("PROP1:value1", ++line),
				context("PROP2:value2", ++line),
				context("PROP3:value3", ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Empty lines should be ignored.
	 */
	@Test
	public void empty_lines() throws Exception {
		//@formatter:off
		String string =
		"PROP1:value1\r\n" +
		"\r\n" +
		"PROP2:value2\r\n" +
		"\r\n" +
		"\n" + 
		"\r" +
		"PROP3:value3";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP1").value("value1").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value2").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP3").value("value3").build()), any(Context.class));

			//@formatter:off
			assertContexts(listener,
				context("PROP1:value1", 1),
				context("PROP2:value2", 3),
				context("PROP3:value3", 7)
			);
			//@formatter:on
		}
	}

	/**
	 * Tests what happens when the producer did not add whitespace to the
	 * beginning of a folded line.
	 */
	@Test
	public void badly_folded_line() throws Exception {
		//@formatter:off
		String string =
		"PROP;PARAM=one;PARA\r\n" +
		"M=two:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onWarning(eq(Warning.MALFORMED_LINE), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("M=two").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When the input stream ends before the property value is reached.
	 */
	@Test
	public void property_cut_off() throws Exception {
		//@formatter:off
		String string =
		"PROP;PARAM=one;PARA";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onWarning(eq(Warning.MALFORMED_LINE), isNull(VObjectProperty.class), isNull(Exception.class), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When the group and/or property names are empty.
	 */
	@Test
	public void empty_group_and_property_names() throws Exception {
		//@formatter:off
		String string =
		":value\r\n" +
		".:value\r\n" +
		"group.:value\r\n" +
		".PROP:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("").value("value").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().group("").name("").value("value").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().group("group").name("").value("value").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().group("").name("PROP").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When there are special characters in the group and property name.
	 */
	@Test
	public void special_characters_in_group_and_name() throws Exception {
		//@formatter:off
		String string =
		"g=,\"roup.P.=,\"ROP:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(new VObjectProperty("g=,\"roup", "P.=,\"ROP", "value")), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When a parameter name is empty.
	 */
	@Test
	public void blank_parameter_name() throws Exception {
		//@formatter:off
		String string =
		"PROP;=value:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("", "value").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When a parameter value doesn't have a name.
	 */
	@Test
	public void no_parameter_name() throws Exception {
		//@formatter:off
		String string =
		"PROP;HOME;WORK:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param(null, "HOME", "WORK").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When a parameter name has special characters.
	 */
	@Test
	public void special_chars_in_parameter_name() throws Exception {
		//@formatter:off
		String string =
		"PROP;P,.\"ARAM=value:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("P,.\"ARAM", "value").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * If there is any whitespace surrounding the equals character in a
	 * parameter declaration, it is handled differently depending on the
	 * version.
	 */
	@Test
	public void parameters_space_around_equals() throws Exception {
		//@formatter:off
		String string =
		"PROP;PARAM = value:value";
		//@formatter:on

		SyntaxStyle style = SyntaxStyle.OLD;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "value").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}

		style = SyntaxStyle.NEW;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM ", " value").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * New style syntax has special syntax for defining multi-valued parameters.
	 */
	@Test
	public void multi_valued_parameters() throws Exception {
		//@formatter:off
		String string =
		"PROP;PARAM=value1,value2:value";
		//@formatter:on

		SyntaxStyle style = SyntaxStyle.OLD;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "value1,value2").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}

		style = SyntaxStyle.NEW;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "value1", "value2").value("value").build()), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * New style syntax lets you surround parameter values in double quotes.
	 * Doing this lets you put special characters like semi-colons in the
	 * property value.
	 */
	@Test
	public void parameter_values_in_double_quotes() throws Exception {
		//@formatter:off
		String string =
		"PROP;PARAM=\"a;b:c,d\":value";
		//@formatter:on

		Map<SyntaxStyle, VObjectProperty> styleToProperty = new HashMap<SyntaxStyle, VObjectProperty>();
		styleToProperty.put(SyntaxStyle.OLD, property().name("PROP").param("PARAM", "\"a").param(null, "b").value("c,d\":value").build());
		styleToProperty.put(SyntaxStyle.NEW, property().name("PROP").param("PARAM", "a;b:c,d").value("value").build());

		for (SyntaxStyle style : styleToProperty.keySet()) {
			VObjectProperty expectedProperty = styleToProperty.get(style);

			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(expectedProperty), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Tests the various escaping mechanisms for parameter values.
	 */
	@Test
	public void parameter_value_escaping() throws Exception {
		SyntaxStyle style = SyntaxStyle.OLD;
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped semi-colon (must be escaped in 2.1)
			//6: backslash-escaped newline (lowercase n)
			//7: backslash-escaped newline (uppercase N)
			//8: caret-escaped double quote
			//9: un-escaped double quote (no special meaning in 2.1)
			//a: caret that doesn't escape anything
			//b: backslash-escaped backslash

			//@formatter:off
			String string =
			"PROP;PARAM=1\\ 2^^ 3^n 4^N 5\\; 6\\n 7\\N 8^' 9\" a^ b\\\\:";
			//@formatter:on

			for (boolean caretDecodingEnabled : new boolean[] { false, true }) { //caret decoding has no effect in old style
				VObjectReader reader = reader(string, style);
				reader.setCaretDecodingEnabled(caretDecodingEnabled);
				VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
				reader.parse(listener);

				InOrder inorder = inOrder(listener);
				inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "1\\ 2^^ 3^n 4^N 5; 6\n 7\n 8^' 9\" a^ b\\").value("").build()), any(Context.class));

				//@formatter:off
				String lines[] = string.split("\r\n");
				int line = 0;
				assertContexts(listener,
					context(lines[line], ++line)
				);
				//@formatter:on
			}
		}

		style = SyntaxStyle.NEW;
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: backslash-escaped backslash
			//a: caret that doesn't escape anything

			//@formatter:off
			String string =
			"PROP;PARAM=1\\ 2^^ 3^n 4^N 5\\n 6\\N 7^' 8\\\" 9\\\\ a^ :";
			//@formatter:on

			Map<Boolean, String> expectedParamValues = new HashMap<Boolean, String>();
			expectedParamValues.put(false, "1\\ 2^^ 3^n 4^N 5\n 6\n 7^' 8\" 9\\ a^ ");
			expectedParamValues.put(true, "1\\ 2^ 3\n 4^N 5\n 6\n 7\" 8\" 9\\ a^ ");

			for (Boolean caretDecodingEnabled : expectedParamValues.keySet()) {
				String expectedParamValue = expectedParamValues.get(caretDecodingEnabled);

				VObjectReader reader = reader(string, style);
				reader.setCaretDecodingEnabled(caretDecodingEnabled);
				VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
				reader.parse(listener);

				InOrder inorder = inOrder(listener);
				inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", expectedParamValue).value("").build()), any(Context.class));

				//@formatter:off
				String lines[] = string.split("\r\n");
				int line = 0;
				assertContexts(listener,
					context(lines[line], ++line)
				);
				//@formatter:on
			}
		}
	}

	/**
	 * Asserts that it can unfold folded lines.
	 */
	@Test
	public void folded_lines() throws Exception {
		//@formatter:off
		String string =
		"PROP:fo\r\n" +
		" lded\r\n" +
		"PROP:fo\r\n" +
		"\tlded\r\n" +
		"PROP:fo\r\n" +
		" \r\n" + //empty folded line
		" lded\r\n" +
		"PROP:fo\r\n" +
		" \tlded\r\n" +
		"PROP:fo\r\n" +
		"\t lded\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE:fo=\r\n" +
		" lded\r\n" +
		"PROP;QUOTED-PRINTABLE:fo=\r\n" +
		" lded\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE:fo=\r\n" +
		"lded\r\n" +
		"PROP;P\r\n" +
		" ARAM=value:\r\n" +
		"PROP:last";
		//@formatter:on

		SyntaxStyle style = SyntaxStyle.OLD;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener, times(5)).onProperty(eq(property().name("PROP").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param(null, "QUOTED-PRINTABLE").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "value").value("").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("last").build()), any(Context.class));

			//@formatter:off
			assertContexts(listener,
				context("PROP:folded", 1),
				context("PROP:folded", 3),
				context("PROP:folded", 5),
				context("PROP:folded", 8),
				context("PROP:folded", 10),
				context("PROP;ENCODING=QUOTED-PRINTABLE:folded", 12),
				context("PROP;QUOTED-PRINTABLE:folded", 14),
				context("PROP;ENCODING=QUOTED-PRINTABLE:folded", 16),
				context("PROP;PARAM=value:", 18),
				context("PROP:last", 20)
			);
			//@formatter:on
		}

		style = SyntaxStyle.NEW;
		{
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener, times(3)).onProperty(eq(property().name("PROP").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("fo\tlded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("fo lded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("folded").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param(null, "QUOTED-PRINTABLE").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("folded").value("folded").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("PARAM", "value").value("").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("last").build()), any(Context.class));

			//@formatter:off
			assertContexts(listener,
				context("PROP:folded", 1),
				context("PROP:folded", 3),
				context("PROP:folded", 5),
				context("PROP:fo\tlded", 8),
				context("PROP:fo lded", 10),
				context("PROP;ENCODING=QUOTED-PRINTABLE:folded", 12),
				context("PROP;QUOTED-PRINTABLE:folded", 14),
				context("PROP;ENCODING=QUOTED-PRINTABLE:folded", 16),
				context("PROP;PARAM=value:", 18),
				context("PROP:last", 20)
			);
			//@formatter:on
		}
	}

	/**
	 * Tests quoted-printable encoding.
	 */
	@Test
	public void quoted_printable() throws Exception {
		//@formatter:off
		String string =
		"PROP;QUOTED-PRINTABLE:one=0D=0Atwo\r\n" +
		"PROP;quoted-printable:one=0D=0Atwo\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE:one=0D=0Atwo\r\n" +
		"PROP;ENCODING=quoted-printable:one=0D=0Atwo\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE:=XX\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:one=0D=0Atwo\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=illegal name:one=0D=0Atwo";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
			reader.parse(listener);

			InOrder inorder = inOrder(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").param(null, "QUOTED-PRINTABLE").value("one\r\ntwo").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param(null, "quoted-printable").value("one\r\ntwo").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("one\r\ntwo").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "quoted-printable").value("one\r\ntwo").build()), any(Context.class));

			VObjectProperty errorDecoding = property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").value("=XX").build();
			inorder.verify(listener).onWarning(eq(Warning.QUOTED_PRINTABLE_ERROR), eq(errorDecoding), any(DecoderException.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(errorDecoding), any(Context.class));

			inorder.verify(listener).onProperty(eq(property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").param("CHARSET", "UTF-8").value("one\r\ntwo").build()), any(Context.class));

			VObjectProperty invalidCharset = property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").param("CHARSET", "invalid").value("one\r\ntwo").build();
			inorder.verify(listener).onWarning(eq(Warning.UNKNOWN_CHARSET), eq(invalidCharset), any(UnsupportedCharsetException.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(invalidCharset), any(Context.class));

			VObjectProperty illegalName = property().name("PROP").param("ENCODING", "QUOTED-PRINTABLE").param("CHARSET", "illegal name").value("one\r\ntwo").build();
			inorder.verify(listener).onWarning(eq(Warning.UNKNOWN_CHARSET), eq(illegalName), any(IllegalCharsetNameException.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(illegalName), any(Context.class));

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				context(lines[line], ++line),
				
				context(lines[line], line+1),
				context(lines[line], ++line),
				
				context(lines[line], ++line),
				
				context(lines[line], line+1),
				context(lines[line], ++line),
				
				context(lines[line], line+1),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * When a parameter value doesn't have a name.
	 */
	@Test
	public void syntax_style_rules() throws Exception {
		//@formatter:off
		String string =
		"VERSION:1\r\n" +
		"BEGIN:COMP1\r\n" +
			"PROP;PARAM=\"value\":\r\n" + //default to OLD style
		"END:COMP1\r\n" +
		"BEGIN:COMP1\r\n" +
			"VERSION:2\r\n" + //wrong parent
			"PROP;PARAM=\"value\":\r\n" +
		"END:COMP1\r\n" +
		"BEGIN:COMP2\r\n" +
			"VERSION:3\r\n" + //invalid version
			"PROP;PARAM=\"value\":\r\n" +
		"END:COMP2\r\n" +
		"BEGIN:COMP2\r\n" +
			"VERSION:2\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
			"BEGIN:COMP3\r\n" + //keep syntax of parent
				"PROP;PARAM=\"value\":\r\n" +
			"END:COMP3\r\n" +
			"BEGIN:COMP2\r\n" + //change syntax
				"VERSION:1\r\n" +
				"PROP;PARAM=\"value\":\r\n" +
			"END:COMP2\r\n" +
			"PROP;PARAM=\"value\":\r\n" + //syntax returns
		"END:COMP2\r\n";
		//@formatter:on

		SyntaxRules rules = new SyntaxRules(SyntaxStyle.OLD);
		rules.addRule("COMP2", "1", SyntaxStyle.OLD);
		rules.addRule("COMP2", "2", SyntaxStyle.NEW);

		VObjectReader reader = new VObjectReader(new StringReader(string), rules);
		VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
		reader.parse(listener);

		VObjectProperty oldStyleProp = property().name("PROP").param("PARAM", "\"value\"").value("").build();
		VObjectProperty newStyleProp = property().name("PROP").param("PARAM", "value").value("").build();

		InOrder inorder = inOrder(listener);

		//@formatter:off
		inorder.verify(listener).onProperty(eq(property().name("VERSION").value("1").build()), any(Context.class));
		inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("VERSION").value("2").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onWarning(eq(Warning.UNKNOWN_VERSION), eq(property().name("VERSION").value("3").build()), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("VERSION").value("3").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onVersion(eq("2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP3"), any(Context.class));
				inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP3"), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
				inorder.verify(listener).onVersion(eq("1"), any(Context.class));
				inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
		//@formatter:on

		//@formatter:off
		String lines[] = string.split("\r\n");
		int line = 0;
		assertContexts(listener,
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("COMP1"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("COMP1"), lines[line], ++line),
			context(asList("COMP1"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("COMP2"), lines[line], line+1),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2", "COMP3"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2", "COMP2"), lines[line], ++line),
			context(asList("COMP2", "COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(asList("COMP2"), lines[line], ++line),
			context(lines[line], ++line)
		);
		//@formatter:on
	}

	@Test
	public void icalendar_rules() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:VCALENDAR\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:10\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		VObjectReader reader = new VObjectReader(new StringReader(string), SyntaxRules.iCalendar());
		VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
		reader.parse(listener);

		VObjectProperty oldStyleProp = property().name("PROP").param("PARAM", "\"value\"").value("").build();
		VObjectProperty newStyleProp = property().name("PROP").param("PARAM", "value").value("").build();

		InOrder inorder = inOrder(listener);

		//@formatter:off
		inorder.verify(listener).onComponentBegin(eq("VCALENDAR"), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCALENDAR"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCALENDAR"), any(Context.class));
			inorder.verify(listener).onWarning(eq(Warning.UNKNOWN_VERSION), eq(property().name("VERSION").value("10").build()), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("VERSION").value("10").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCALENDAR"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCALENDAR"), any(Context.class));
			inorder.verify(listener).onVersion(eq("1.0"), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCALENDAR"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCALENDAR"), any(Context.class));
			inorder.verify(listener).onVersion(eq("2.0"), any(Context.class));
			inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCALENDAR"), any(Context.class));
		//@formatter:on

		//@formatter:off
		String lines[] = string.split("\r\n");
		int line = 0;
		assertContexts(listener,
			context(lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], line+1),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(asList("VCALENDAR"), lines[line], ++line),
			context(lines[line], ++line)
		);
		//@formatter:on
	}

	@Test
	public void vcard_rules() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:VCARD\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:10\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"PROP;PARAM=\"value\":\r\n" +
		"END:VCARD";
		//@formatter:on

		VObjectReader reader = new VObjectReader(new StringReader(string), SyntaxRules.vcard());
		VObjectDataListenerMock listener = spy(new VObjectDataListenerMock());
		reader.parse(listener);

		VObjectProperty oldStyleProp = property().name("PROP").param("PARAM", "\"value\"").value("").build();
		VObjectProperty newStyleProp = property().name("PROP").param("PARAM", "value").value("").build();

		InOrder inorder = inOrder(listener);

		//@formatter:off
		inorder.verify(listener).onComponentBegin(eq("VCARD"), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCARD"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCARD"), any(Context.class));
			inorder.verify(listener).onWarning(eq(Warning.UNKNOWN_VERSION), eq(property().name("VERSION").value("10").build()), isNull(Exception.class), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("VERSION").value("10").build()), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCARD"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCARD"), any(Context.class));
			inorder.verify(listener).onVersion(eq("2.1"), any(Context.class));
			inorder.verify(listener).onProperty(eq(oldStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCARD"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCARD"), any(Context.class));
			inorder.verify(listener).onVersion(eq("3.0"), any(Context.class));
			inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCARD"), any(Context.class));
		
		inorder.verify(listener).onComponentBegin(eq("VCARD"), any(Context.class));
			inorder.verify(listener).onVersion(eq("4.0"), any(Context.class));
			inorder.verify(listener).onProperty(eq(newStyleProp), any(Context.class));
		inorder.verify(listener).onComponentEnd(eq("VCARD"), any(Context.class));
		//@formatter:on

		//@formatter:off
		String lines[] = string.split("\r\n");
		int line = 0;
		assertContexts(listener,
			context(lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCARD"), lines[line], line+1),
			context(asList("VCARD"), lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(lines[line], ++line),
			
			context(lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(asList("VCARD"), lines[line], ++line),
			context(lines[line], ++line)
		);
		//@formatter:on
	}

	@Test
	public void pause_parsing() throws Exception {
		//@formatter:off
		String string =
		"BEGIN:COMP1\r\n" +
			"PROP:value\r\n" +
		"END:COMP1\r\n" +
		"BEGIN:COMP2\r\n" +
			"PROP:value\r\n" +
		"END:COMP2\r\n" +
		"PROP2:value\r\n" +
		"PROP:value";
		//@formatter:on

		for (SyntaxStyle style : SyntaxStyle.values()) {
			VObjectReader reader = reader(string, style);
			VObjectDataListenerMock listener = spy(new VObjectDataListenerMock() {
				@Override
				public void onComponentBegin(String name, Context context) {
					super.onComponentBegin(name, context);
					if (name.equals("COMP1")) {
						context.stop();
					}
				}

				@Override
				public void onComponentEnd(String name, Context context) {
					super.onComponentEnd(name, context);
					if (name.equals("COMP2")) {
						context.stop();
					}
				}

				@Override
				public void onProperty(VObjectProperty property, Context context) {
					super.onProperty(property, context);
					if (property.getName().equals("PROP2")) {
						context.stop();
					}
				}
			});

			InOrder inorder = inOrder(listener);

			reader.parse(listener);
			inorder.verify(listener).onComponentBegin(eq("COMP1"), any(Context.class));
			inorder.verifyNoMoreInteractions();

			reader.parse(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("value").build()), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP1"), any(Context.class));
			inorder.verify(listener).onComponentBegin(eq("COMP2"), any(Context.class));
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("value").build()), any(Context.class));
			inorder.verify(listener).onComponentEnd(eq("COMP2"), any(Context.class));
			inorder.verifyNoMoreInteractions();

			reader.parse(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP2").value("value").build()), any(Context.class));
			inorder.verifyNoMoreInteractions();

			reader.parse(listener);
			inorder.verify(listener).onProperty(eq(property().name("PROP").value("value").build()), any(Context.class));
			inorder.verifyNoMoreInteractions();

			//@formatter:off
			String lines[] = string.split("\r\n");
			int line = 0;
			assertContexts(listener,
				context(lines[line], ++line),
				context(asList("COMP1"), lines[line], ++line),
				context(lines[line], ++line),
				
				context(lines[line], ++line),
				context(asList("COMP2"), lines[line], ++line),
				context(lines[line], ++line),
				
				context(lines[line], ++line),
				context(lines[line], ++line)
			);
			//@formatter:on
		}
	}

	/**
	 * Checks the values of the {@link Context} objects that were passed into
	 * the listener.
	 * @param listener the listener
	 * @param expected the expected {@link Context} objects
	 */
	private static void assertContexts(VObjectDataListenerMock listener, Context... expected) {
		List<Context> actual = listener.contexts;
		assertEquals("Expected " + expected.length + " Context objects, found " + actual.size(), expected.length, actual.size());

		List<Context> expectedList = Arrays.asList(expected);
		for (int i = 0; i < expectedList.size(); i++) {
			Context e = expectedList.get(i);
			Context a = actual.get(i);

			assertEquals("Context " + i + ": expected <" + e.getParentComponents() + "> but was: <" + a.getParentComponents() + ">", e.getParentComponents(), a.getParentComponents());
			assertEquals("Context " + i + ": expected <" + e.getUnfoldedLine() + "> but was: <" + a.getUnfoldedLine() + ">", e.getUnfoldedLine(), a.getUnfoldedLine());
			assertEquals("Context " + i + ": expected <" + e.getLineNumber() + "> but was: <" + a.getLineNumber() + ">", e.getLineNumber(), a.getLineNumber());
		}
	}

	/**
	 * Creates a {@link Context} object. This method alleviates the need to add
	 * a constructor to the class, which is only needed for unit testing.
	 * @param components the component hierarchy
	 * @param unfoldedLine the unfolded line
	 * @param lineNumber the line number
	 * @return the object
	 */
	private static Context context(List<String> components, String unfoldedLine, int lineNumber) {
		Context context = new Context(components);
		context.unfoldedLine.append(unfoldedLine);
		context.lineNumber = lineNumber;
		return context;
	}

	/**
	 * Creates a {@link Context} object. This method alleviates the need to add
	 * a constructor to the class, which is only needed for unit testing.
	 * @param unfoldedLine the unfolded line
	 * @param lineNumber the line number
	 * @return the object
	 */
	private static Context context(String unfoldedLine, int lineNumber) {
		return context(Collections.<String> emptyList(), unfoldedLine, lineNumber);
	}

	@SuppressWarnings("unused")
	private static void debug(VObjectReader reader) throws Exception {
		reader.parse(new DebugDataListener());
	}

	private static VObjectReader reader(String string, SyntaxStyle syntaxStyle) {
		return new VObjectReader(new StringReader(string), new SyntaxRules(syntaxStyle));
	}

	private static VObjectPropertyBuilder property() {
		return new VObjectPropertyBuilder();
	}

	private static class VObjectPropertyBuilder {
		private final VObjectProperty property = new VObjectProperty();

		public VObjectPropertyBuilder name(String name) {
			property.setName(name);
			return this;
		}

		public VObjectPropertyBuilder group(String group) {
			property.setGroup(group);
			return this;
		}

		public VObjectPropertyBuilder value(String value) {
			property.setValue(value);
			return this;
		}

		public VObjectPropertyBuilder param(String name, String... values) {
			if (values.length == 0) {
				throw new IllegalArgumentException("No parameter values given.");
			}

			property.getParameters().putAll(name, values);
			return this;
		}

		public VObjectProperty build() {
			return property;
		}
	}

	private static class VObjectDataListenerMock implements VObjectDataListener {
		/**
		 * Stores copies of the context objects that are passed into each
		 * callback method. This is needed because Mockito gets confused by the
		 * fact that the same object is passed into these callback methods every
		 * time they are called.
		 * @see "http://stackoverflow.com/q/38779862/13379"
		 */
		private final List<Context> contexts = new ArrayList<Context>();

		public void onComponentBegin(String name, Context context) {
			contexts.add(copy(context));
		}

		public void onComponentEnd(String name, Context context) {
			contexts.add(copy(context));
		}

		public void onProperty(VObjectProperty property, Context context) {
			contexts.add(copy(context));
		}

		public void onVersion(String value, Context context) {
			contexts.add(copy(context));
		}

		public void onWarning(Warning warning, VObjectProperty property, Exception thrown, Context context) {
			contexts.add(copy(context));
		}

		private Context copy(Context orig) {
			Context copy = new Context(new ArrayList<String>(orig.parentComponents));
			copy.unfoldedLine.append(orig.getUnfoldedLine());
			copy.lineNumber = orig.lineNumber;
			return copy;
		}
	}
}
