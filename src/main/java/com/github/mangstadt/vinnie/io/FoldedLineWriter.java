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

import static com.github.mangstadt.vinnie.Utils.CRLF;
import static com.github.mangstadt.vinnie.Utils.isCRLF;
import static com.github.mangstadt.vinnie.Utils.isCROrLF;

import java.io.IOException;
import java.io.Writer;

/**
 * Automatically folds lines as they are written.
 * @author Michael Angstadt
 */
public class FoldedLineWriter extends Writer {
	private final Writer writer;
	private final int lineLength;
	private final String indent;

	private int curLineLength = 0;

	/**
	 * Creates a folded line writer with standard settings: line length 75 and
	 * single space indent.
	 * @param writer the writer object to wrap
	 */
	public FoldedLineWriter(Writer writer) {
		this(writer, 75, " ");
	}

	/**
	 * Creates a folded line writer.
	 * @param writer the writer object to wrap
	 * @param lineLength the maximum length a line can be before it is folded
	 * (excluding the newline)
	 * @param indent the string that is prepended onto each folded line. Must
	 * have a length of at least one and must consist only of tabs and spaces
	 * @throws IllegalArgumentException if line length or indent are invalid
	 */
	public FoldedLineWriter(Writer writer, int lineLength, String indent) {
		validate(lineLength, indent);

		this.writer = writer;
		this.lineLength = lineLength;
		this.indent = indent;
	}

	private static void validate(int lineLength, String indent) {
		if (lineLength < 4) {
			/*
			 * Line folding breaks when line length is < 4.
			 */
			throw new IllegalArgumentException("Line length must be greater than 3.");
		}
		if (lineLength <= indent.length()) {
			throw new IllegalArgumentException("Line length must be greater than indent string length.");
		}

		if (indent.isEmpty()) {
			throw new IllegalArgumentException("Indent string cannot be empty.");
		}
		if (indent.length() >= lineLength) {
			throw new IllegalArgumentException("Indent string length must be less than the line length.");
		}
		for (int i = 0; i < indent.length(); i++) {
			char c = indent.charAt(i);
			if (c != ' ' && c != '\t') {
				throw new IllegalArgumentException("Indent string can only contain tabs and spaces.");
			}
		}
	}

	/**
	 * <p>
	 * Writes a string that's been encoded in quoted-printable encoding.
	 * </p>
	 * <p>
	 * The folded line writer must know that a quoted-printable string is being
	 * written because an equals character must be appended onto each folded
	 * line, and the next line must receive no indentation whitespace.
	 * </p>
	 * @param encoded the quoted-printable string
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeQuotedPrintable(String encoded) throws IOException {
		char[] array = encoded.toCharArray();
		write(array, 0, array.length, true);
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
		write(cbuf, off, len, false);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param cbuf the array of characters
	 * @param off the offset from which to start writing characters
	 * @param len the number of characters to write
	 * @param quotedPrintable true if the string is encoded in quoted-printable
	 * encoding, false if not
	 * @throws IOException if there's a problem writing to the output stream
	 */
	private void write(char[] cbuf, int off, int len, boolean quotedPrintable) throws IOException {
		int effectiveLineLength = calculateEffectiveLineLength(quotedPrintable);

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

			if (isCRCharInCRLF(i, end, cbuf)) {
				/*
				 * Process newline at next loop iteration.
				 */
				continue;
			}

			if (isCROrLF(c)) {
				writer.write(cbuf, start, i - start + 1);
				curLineLength = 0;
				start = i + 1;
				continue;
			}

			if (quotedPrintable && c == '=') {
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
					if (i >= end - 1 && Character.isWhitespace(c)) {
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

				writeFoldedLine(i, start, cbuf, quotedPrintable);

				start = i;

				continue;
			}

			curLineLength++;
		}

		writer.write(cbuf, start, end - start);
	}

	private int calculateEffectiveLineLength(boolean quotedPrintable) {
		/*
		 * Account for the "=" character that must be appended onto each line
		 * for quoted-printable values.
		 */
		return quotedPrintable ? lineLength - 1 : lineLength;
	}

	private boolean isCRCharInCRLF(int i, int end, char[] cbuf) {
		return (i < end - 1) && isCRLF(cbuf[i], cbuf[i + 1]);
	}

	private void writeFoldedLine(int i, int start, char[] cbuf, boolean quotedPrintable) throws IOException {
		writer.write(cbuf, start, i - start);
		if (quotedPrintable) {
			writer.write('=');
		}
		writer.write(CRLF);

		/*
		 * Do not include indentation whitespace if the value is
		 * quoted-printable.
		 */
		curLineLength = 1;
		if (!quotedPrintable) {
			writer.write(indent);
			curLineLength += indent.length();
		}
	}

	/**
	 * Gets the maximum length a line can be before it is folded (excluding the
	 * newline, defaults to 75).
	 * @return the line length
	 */
	public int getLineLength() {
		return lineLength;
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
