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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.codec.EncoderException;
import com.github.mangstadt.vinnie.codec.QuotedPrintableCodec;

/**
 * Automatically folds lines as they are written.
 * @author Michael Angstadt
 */
public class FoldedLineWriter extends Writer {
	private static final String CRLF = "\r\n";
	private final Writer writer;

	private Integer lineLength = 75;
	private String indent = " ";

	private int curLineLength = 0;

	/**
	 * Creates a folded line writer.
	 * @param writer the writer object to wrap
	 */
	public FoldedLineWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Writes a newline.
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeln() throws IOException {
		write(CRLF);
	}

	/**
	 * Writes a string.
	 * @param str the string to write
	 * @param quotedPrintable true to encode the string in quoted-printable
	 * encoding, false not to
	 * @param charset the character set to use when encoding the string into
	 * quoted-printable
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(CharSequence str, boolean quotedPrintable, Charset charset) throws IOException {
		write(str.toString().toCharArray(), 0, str.length(), quotedPrintable, charset);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param cbuf the array of characters
	 * @param off the offset from which to start writing characters
	 * @param len the number of characters to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		write(cbuf, off, len, false, null);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param cbuf the array of characters
	 * @param off the offset from which to start writing characters
	 * @param len the number of characters to write
	 * @param quotedPrintable true to encode the string in quoted-printable
	 * encoding, false not to
	 * @param charset the character set to use when encoding the string into
	 * quoted-printable
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(char[] cbuf, int off, int len, boolean quotedPrintable, Charset charset) throws IOException {
		if (quotedPrintable) {
			String str = new String(cbuf, off, len);
			QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());

			String encoded;
			try {
				encoded = codec.encode(str);
			} catch (EncoderException e) {
				/*
				 * Thrown if an unsupported charset is passed into the codec.
				 * This should never happen because we already know the charset
				 * is valid (a Charset object is passed into the method).
				 */
				throw new IOException(e);
			}

			cbuf = encoded.toCharArray();
			off = 0;
			len = cbuf.length;
		}

		if (lineLength == null) {
			/*
			 * If line folding is disabled, then write directly to the Writer.
			 */
			writer.write(cbuf, off, len);
			return;
		}

		int effectiveLineLength = lineLength;
		if (quotedPrintable) {
			/*
			 * Account for the "=" character that must be appended onto each
			 * line.
			 */
			effectiveLineLength -= 1;
		}

		int encodedCharPos = -1;
		int start = off;
		int end = off + len;
		for (int i = start; i < end; i++) {
			char c = cbuf[i];

			/*
			 * Keep track of the quoted-printable characters to prevent them
			 * from being cut in two at a folding boundary.
			 */
			if (encodedCharPos >= 0) {
				encodedCharPos++;
				if (encodedCharPos == 3) {
					encodedCharPos = -1;
				}
			}

			if (c == '\n') {
				writer.write(cbuf, start, i - start + 1);
				curLineLength = 0;
				start = i + 1;
				continue;
			}

			if (c == '\r') {
				if (i == end - 1 || cbuf[i + 1] != '\n') {
					writer.write(cbuf, start, i - start + 1);
					curLineLength = 0;
					start = i + 1;
				} else {
					curLineLength++;
				}
				continue;
			}

			if (c == '=' && quotedPrintable) {
				encodedCharPos = 0;
			}

			if (curLineLength >= effectiveLineLength) {
				/*
				 * If the last characters on the line are whitespace, then
				 * exceed the max line length in order to include the whitespace
				 * on the same line.
				 * 
				 * This is to prevent the whitespace from merging with the
				 * folding whitespace of the following folded line and
				 * potentially being lost.
				 * 
				 * Old syntax style allows multiple whitespace characters to be
				 * used for folding, so it could get lost here. New syntax style
				 * only allows one character to be used.
				 */
				if (Character.isWhitespace(c)) {
					while (Character.isWhitespace(c) && i < end - 1) {
						i++;
						c = cbuf[i];
					}
					if (i >= end - 1) {
						/*
						 * The rest of the char array is whitespace, so leave
						 * the loop.
						 */
						break;
					}
				}

				/*
				 * If we are in the middle of a quoted-printable encoded
				 * character, then exceed the max line length so the sequence
				 * doesn't get split up across multiple lines.
				 */
				if (encodedCharPos > 0) {
					i += 3 - encodedCharPos;
					if (i >= end - 1) {
						/*
						 * The rest of the char array was a quoted-printable
						 * encoded char, so leave the loop.
						 */
						break;
					}
				}

				/*
				 * If the last char is the low (second) char in a surrogate
				 * pair, don't split the pair across two lines.
				 */
				if (Character.isLowSurrogate(c)) {
					i++;
					if (i >= end - 1) {
						/*
						 * Surrogate pair finishes the char array, so leave the
						 * loop.
						 */
						break;
					}
				}

				writer.write(cbuf, start, i - start);
				if (quotedPrintable) {
					writer.write('=');
				}
				writer.write(CRLF);
				writer.write(indent);
				curLineLength = indent.length() + 1;
				start = i;

				continue;
			}

			curLineLength++;
		}

		writer.write(cbuf, start, end - start);
	}

	/**
	 * Gets the maximum length a line can be before it is folded (excluding the
	 * newline, defaults to 75).
	 * @return the line length or null if folding is disabled
	 */
	public Integer getLineLength() {
		return lineLength;
	}

	/**
	 * Sets the maximum length a line can be before it is folded (excluding the
	 * newline, defaults to 75).
	 * @param lineLength the line length or null to disable folding
	 * @throws IllegalArgumentException if the line length is less than or equal
	 * to zero, or the line length is less than the length of the indent string
	 */
	public void setLineLength(Integer lineLength) {
		if (lineLength != null) {
			if (lineLength <= 0) {
				throw new IllegalArgumentException("Line length must be greater than 0.");
			}
			if (lineLength <= indent.length()) {
				throw new IllegalArgumentException("Line length must be greater than indent string length.");
			}
		}

		this.lineLength = lineLength;
	}

	/**
	 * Gets the string that is prepended to each folded line (defaults to a
	 * single space character).
	 * @return the indent string
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the string that is prepended to each folded line (defaults to a
	 * single space character).
	 * @param indent the indent string (cannot be empty, may only contain tabs
	 * and spaces). Note that data streams using {@link SyntaxStyle#NEW} syntax
	 * MUST use an indent string that contains EXACTLY ONE character.
	 * @throws IllegalArgumentException if the indent string is empty, or the
	 * length of the indent string is greater than the max line length, or the
	 * indent string contains illegal characters
	 */
	public void setIndent(String indent) {
		if (indent.length() == 0) {
			throw new IllegalArgumentException("Indent string cannot be empty.");
		}

		if (lineLength != null && indent.length() >= lineLength) {
			throw new IllegalArgumentException("Indent string length must be less than the line length.");
		}

		for (int i = 0; i < indent.length(); i++) {
			char c = indent.charAt(i);
			switch (c) {
			case ' ':
			case '\t':
				break;
			default:
				throw new IllegalArgumentException("Indent string can only contain tabs and spaces.");
			}
		}

		this.indent = indent;
	}

	/**
	 * Gets the wrapped {@link Writer} object.
	 * @return the writer object
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * Closes the writer.
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Flushes the writer.
	 */
	@Override
	public void flush() throws IOException {
		writer.flush();
	}
}
