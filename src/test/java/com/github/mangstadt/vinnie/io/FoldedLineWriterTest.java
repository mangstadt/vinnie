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
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.junit.Test;

import com.github.mangstadt.vinnie.codec.QuotedPrintableCodec;

/**
 * @author Michael Angstadt
 */
public class FoldedLineWriterTest {
	/**
	 * Asserts the writer's behavior without changing any settings.
	 */
	@Test
	public void default_settings() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		assertEquals(Integer.valueOf(75), writer.getLineLength());
		assertEquals(" ", writer.getIndent());

		writer.write("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tem\r\n" +
		" por incididunt ut labore et dolore magna aliqua.";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * The folding line length can be adjusted.
	 */
	@Test
	public void line_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		writer.write("Lorem ipsum dolor sit amet");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"Lorem ipsu\r\n" +
		" m dolor s\r\n" +
		" it amet";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Setting the line length to "null" disables line folding.
	 */
	@Test
	public void line_length_null() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(null);
		writer.write("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * When a newline sequence in the input string is encountered, this should
	 * cause the line length to reset so that the next line can reach the max
	 * line length.
	 */
	@Test
	public void line_length_reset_on_newline() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		writer.write("Lorem\r\nipsum dolor sit\ramet, consectetur\nadipiscing elit");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"Lorem\r\n" +
		"ipsum dolo\r\n" +
		" r sit\r" +
		"amet, cons\r\n" +
		" ectetur\n" +
		"adipiscing \r\n" +
		" elit";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * <p>
	 * When the max line length has been reached, if the next character is a
	 * space, the writer should temporarily exceed the max line length in order
	 * to write the space on the same line before folding it. This is to prevent
	 * the space character from being included with the folding whitespace of
	 * the next line and possibly being ignored by the consuming application.
	 * </p>
	 * <p>
	 * This is a possibility with old style syntax because old style syntax
	 * allows multiple characters to be included in the folding whitespace. New
	 * style syntax, by contrast, requires exactly one folding whitespace
	 * character.
	 * </p>
	 */
	@Test
	public void exceed_max_line_length_when_space_encountered() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);

		writer.write("Lorem ipsum dolor");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"Lorem \r\n" + //exceed max line length temporarily
		" ipsu\r\n" +
		" m do\r\n" +
		" lor";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Same as {@link #exceed_max_line_length_when_space_encountered} test, but
	 * with the space character at the end of the string.
	 */
	@Test
	public void exceed_max_line_length_when_space_encountered_end_of_string() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);

		writer.write("Lorem ");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"Lorem ";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * The folding whitespace string can be changed.
	 */
	@Test
	public void indent() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setIndent("\t");

		writer.write("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tem\r\n" +
		"\tpor incididunt ut labore et dolore magna aliqua.";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void quoted_printable() throws Exception {
		String input = "test \n\u00e4\u00f6\u00fc\u00df\n test";
		Charset charset = Charset.forName("ISO-8859-1");

		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		writer.write(input, true, charset);
		writer.close();

		String actual = sw.toString();
		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		String expectedOutput = codec.encode(input);
		assertEquals(expectedOutput, actual);
	}

	/**
	 * <p>
	 * When a quoted-printable encoded value is folded:
	 * </p>
	 * <ol>
	 * <li>Each line should end with a "=", except for the last line.</li>
	 * <li>No indent whitespace should be added to the folded lines.</li>
	 * <li>Max line length may be exceeded to ensure that no encoded character
	 * sequence spans multiple lines.</li>
	 * </ol>
	 */
	@Test
	public void quoted_printable_folded() throws Exception {
		String input = "test \n\u00e4\u00f6\u00fc\u00df\n testing";

		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		writer.write(input, true, Charset.forName("ISO-8859-1"));
		writer.write("\r\nthis line should be indented");
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"test =0A=E4=\r\n" + //exceed max line length so encoded character does not span multiple lines
		"=F6=FC=DF=\r\n" +
		"=0A testi=\r\n" +
		"ng\r\n" +
		"this line \r\n" +
		" should be \r\n" +
		" indented";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Same as {@link #quoted_printable_folded} test, but when an encoded
	 * character ends the string.
	 */
	@Test
	public void quoted_printable_encoded_char_ends_the_string() throws Exception {
		String input = "test\n";

		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(6);

		writer.write(input, true, Charset.forName("ISO-8859-1"));
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"test=0A";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Surrogate pairs should not be split across multiple lines.
	 */
	@Test
	public void surrogate_pair() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);

		writer.write("test\uD83D\uDCF0test"); // should not be split
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"test\uD83D\uDCF0\r\n" +
		" test";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Same as {@link #surrogate_pair} test, but with the surrogate pair at the
	 * end of the string.
	 */
	@Test
	public void surrogate_pair_ends_the_string() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);

		writer.write("test\uD83D\uDCF0"); // should not be split
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"test\uD83D\uDCF0";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Makes sure the writer takes character array sub-ranges into account.
	 */
	@Test
	public void write_sub_array() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		writer.write("This line should be folded.", 5, 14);
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"line shoul\r\n" +
		" d be";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void writeln() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		writer.write("test");
		writer.writeln();
		writer.close();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"test\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void getLineLength() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		writer.setLineLength(10);
		assertEquals(Integer.valueOf(10), writer.getLineLength());

		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLineLength_zero() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(0);
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLineLength_negative() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(-1);
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLineLength_shorter_than_indent_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setIndent(" \t \t ");
		writer.setLineLength(4);
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLineLength_equal_to_indent_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setIndent(" \t \t ");
		writer.setLineLength(5);
		writer.close();
	}

	@Test
	public void getIndent() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		writer.setIndent("  ");
		assertEquals("  ", writer.getIndent());

		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_empty() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setIndent("");
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_longer_than_line_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);
		writer.setIndent(" \t \t \t");
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_equal_to_line_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);
		writer.setIndent(" \t \t ");
		writer.close();
	}

	@Test
	//exception shouldn't be thrown
	public void setIndent_with_null_line_length() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(null);
		writer.setIndent(" \t \t ");
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_invalid_characters() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setIndent("---");
		writer.close();
	}

	@Test
	public void getWriter() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);

		assertSame(sw, writer.getWriter());

		writer.close();
	}

	@Test
	public void close() throws Exception {
		Writer w = mock(Writer.class);
		FoldedLineWriter writer = new FoldedLineWriter(w);
		writer.close();

		verify(w).close();
	}

	@Test
	public void flush() throws Exception {
		Writer w = mock(Writer.class);
		FoldedLineWriter writer = new FoldedLineWriter(w);
		writer.flush();

		verify(w).flush();

		writer.close();
	}
}
