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

import static com.github.mangstadt.vinnie.Utils.ltrim;
import static com.github.mangstadt.vinnie.Utils.rtrim;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.codec.DecoderException;
import com.github.mangstadt.vinnie.codec.QuotedPrintableCodec;

/**
 * <p>
 * Parses a vobject data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = ...
 * SyntaxRules rules = SyntaxRules.vcard();
 * VObjectReader vobjectReader = new VObjectReader(reader, rules);
 * vobjectReader.parse(new VObjectDataListener(){ ... });
 * vobjectReader.close();
 * </pre>
 * 
 * <p>
 * <b>Quoted-printable Encoding</b>
 * </p>
 * <p>
 * Property values encoded in quoted-printable encoding are automatically
 * decoded. A property value is considered to be encoded in quoted-printable
 * encoding if it has a "ENCODING=QUOTED-PRINTABLE" parameter. Even though the
 * property value is automatically decoded, the ENCODING and CHARSET parameters
 * are not removed from the parsed property object so that the caller can
 * determine its original encoding.
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = new StringReader("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!");
 * VObjectReader vobjectReader = new VObjectReader(reader, ...);
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("¡Hola, mundo!", property.getValue());
 *     assertEquals("QUOTED-PRINTABLE", property.getParameters().first("ENCODING"));
 *     assertEquals("UTF-8", property.getParameters().first("CHARSET"));
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * 
 * <p>
 * If a CHARSET parameter is not present in the quoted-printable property, then
 * the character set of the input stream will be used to decode the value. If
 * this cannot be determined, then the local JVM's default character set will be
 * used. However, this behavior can be overridden by supplying your own
 * character set to use in the event that a CHARSET parameter is not present.
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = new StringReader("NOTE;ENCODING=QUOTED-PRINTABLE:=A1Hola, mundo!");
 * VObjectReader vobjectReader = new VObjectReader(reader, ...);
 * vobjectReader.setDefaultQuotedPrintableCharset(Charset.forName("Windows-1252"));
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("¡Hola, mundo!", property.getValue());
 *     assertEquals("QUOTED-PRINTABLE", property.getParameters().first("ENCODING"));
 *     assertNull(property.getParameters().first("CHARSET"));
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * <p>
 * Nameless ENCODING parameters are also recognized for backwards compatibility
 * with old-style syntax.
 * </p>
 * 
 * <pre>
 * NOTE;QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!
 * </pre>
 * 
 * <p>
 * If there is an error decoding a quoted-printable value, then a warning will
 * be emitted and the value will be treated as plain-text.
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = new StringReader("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=ZZ invalid");
 * VObjectReader vobjectReader = new VObjectReader(reader, ...);
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("=ZZ invalid", property.getValue());
 *   }
 *   public void onWarning(Warning warning, VObjectProperty property, Exception thrown, Context context) {
 *     assertEquals(Warning.QUOTED_PRINTABLE_ERROR, warning);
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * 
 * <p>
 * <b>Circumflex Accent Encoding</b>
 * </p>
 * <p>
 * Circumflex accent encoding allows newlines and double quote characters to be
 * included inside of parameter values. Parameter values that are encoded using
 * this encoding scheme are automatically decoded. Note that this encoding
 * mechanism is only supported by new-style syntax.
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = new StringReader("NOTE;X-AUTHOR=Fox ^'Spooky^' Mulder:The truth is out there.");
 * VObjectReader vobjectReader = new VObjectReader(reader, new SyntaxRules(SyntaxStyle.NEW));
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("Fox \"Spooky\" Mulder", property.getParameters().first("X-AUTHOR"));
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * 
 * <p>
 * In the rare event that your vobject data has raw "^" characters in its
 * parameter values, and it does not use this encoding scheme, circumflex accent
 * decoding can be turned off.
 * </p>
 * 
 * <pre class="brush:java">
 * Reader reader = new StringReader("NOTE;X-EMOTE=^_^:Good morning!");
 * VObjectReader vobjectReader = new VObjectReader(reader, new SyntaxRules(SyntaxStyle.NEW));
 * vobjectReader.setCaretDecodingEnabled(false);
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("^_^", property.getParameters().first("X-EMOTE"));
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * 
 * <p>
 * <b>Line Folding</b>
 * </p>
 * <p>
 * Folded lines are automatically unfolded when read.
 * </p>
 * 
 * <pre class="brush:java">
 * String string = 
 * "NOTE:Lorem ipsum dolor sit amet\\, consectetur adipiscing elit. Vestibulum u\r\n" +
 * " ltricies tempor orci ac dignissim.";
 * Reader reader = new StringReader(string);
 * VObjectReader vobjectReader = new VObjectReader(reader, ...);
 * vobjectReader.parse(new VObjectDataAdapter() {
 *   public void onProperty(VObjectProperty property, Context context) {
 *     assertEquals("Lorem ipsum dolor sit amet\\, consectetur adipiscing elit. Vestibulum ultricies tempor orci ac dignissim.", property.getValue());
 *   }
 * });
 * vobjectReader.close();
 * </pre>
 * 
 * @author Michael Angstadt
 */
public class VObjectReader implements Closeable {
	/**
	 * The local computer's newline character sequence.
	 */
	private final String NEWLINE = System.getProperty("line.separator");

	private final Reader reader;
	private final SyntaxRules syntaxRules;

	private boolean caretDecodingEnabled = true;
	private Charset defaultQuotedPrintableCharset;

	private final ComponentStack stack;

	/**
	 * String buffer used when tokenizing a property.
	 */
	private final Buffer buffer = new Buffer();

	/**
	 * Keeps track of the current status of the parser.
	 */
	private final Context context;

	/**
	 * The character that was read when it was determined that the current
	 * property being parsed has ended.
	 */
	private int leftOver = -1;

	/**
	 * The current line number the parser is on.
	 */
	private int lineNumber = 1;

	/**
	 * Has the entire stream been consumed?
	 */
	private boolean eos = false;

	/**
	 * Creates a new vobject reader.
	 * @param reader the input stream
	 * @param syntaxRules defines the rules that are used to determine what kind
	 * of syntax the data is in
	 */
	public VObjectReader(Reader reader, SyntaxRules syntaxRules) {
		this.reader = reader;
		this.syntaxRules = syntaxRules;
		stack = new ComponentStack(syntaxRules.getDefaultSyntaxStyle());
		context = new Context(stack.names);

		if (reader instanceof InputStreamReader) {
			InputStreamReader isr = (InputStreamReader) reader;
			defaultQuotedPrintableCharset = Charset.forName(isr.getEncoding());
		} else {
			defaultQuotedPrintableCharset = Charset.defaultCharset();
		}
	}

	/**
	 * <p>
	 * Gets the default character set to use when decoding quoted-printable
	 * values of properties that lack CHARSET parameters, or of properties whose
	 * CHARSET parameters are not recognized by the local JVM.
	 * </p>
	 * <p>
	 * By default, this is set to the character set of the {@link Reader} object
	 * that this class was constructed with. If the character set of the
	 * {@link Reader} object could not be determined, then it will be set to the
	 * local JVM's default character set.
	 * </p>
	 * @return the default quoted-printable character set
	 */
	public Charset getDefaultQuotedPrintableCharset() {
		return defaultQuotedPrintableCharset;
	}

	/**
	 * <p>
	 * Sets the character set to use when decoding quoted-printable values of
	 * properties that lack CHARSET parameters, or of properties whose CHARSET
	 * parameters are not recognized by the local JVM.
	 * </p>
	 * <p>
	 * By default, this is set to the character set of the {@link Reader} object
	 * that this class was constructed with. If the character set of the
	 * {@link Reader} object could not be determined, then it will be set to the
	 * local JVM's default character set.
	 * </p>
	 * @param charset the default quoted-printable character set (cannot be
	 * null)
	 */
	public void setDefaultQuotedPrintableCharset(Charset charset) {
		defaultQuotedPrintableCharset = charset;
	}

	/**
	 * <p>
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values. It is only
	 * supported by new style syntax.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <caption>Characters encoded by circumflex accent encoding</caption>
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public boolean isCaretDecodingEnabled() {
		return caretDecodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values. It is only
	 * supported by new style syntax.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <caption>Characters encoded by circumflex accent encoding</caption>
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":geo:40.446816,-80.00566
	 * </pre>
	 * 
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		caretDecodingEnabled = enable;
	}

	/**
	 * <p>
	 * Starts or continues to parse the data off the input stream.
	 * </p>
	 * <p>
	 * This method blocks until one of the following events happen:
	 * </p>
	 * <ol>
	 * <li>The end of the input stream has been reached or</li>
	 * <li>One of the methods in the given {@link VObjectDataListener}
	 * implementation has invoked {@link Context#stop()}.</li>
	 * </ol>
	 * @param listener callback interface for handling data as it is read off
	 * the input stream
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public void parse(VObjectDataListener listener) throws IOException {
		context.stop = false;

		while (!eos && !context.stop) {
			context.lineNumber = lineNumber;
			buffer.clear();
			context.unfoldedLine.clear();

			VObjectProperty property = parseProperty(listener);

			if (context.unfoldedLine.size() == 0) {
				//input stream was empty
				return;
			}

			if (property == null) {
				listener.onWarning(Warning.MALFORMED_LINE, null, null, context);
				continue;
			}

			/*
			 * Note: Property names are trimmed when checking for BEGIN and END
			 * properties because old style syntax allows there to be whitespace
			 * around the colon character for these two properties. Component
			 * names are trimmed for the same reason.
			 */

			if ("BEGIN".equalsIgnoreCase(property.getName().trim())) {
				String componentName = property.getValue().trim().toUpperCase();
				if (componentName.length() == 0) {
					listener.onWarning(Warning.EMPTY_BEGIN, null, null, context);
					continue;
				}

				listener.onComponentBegin(componentName, context);

				stack.push(componentName);
				continue;
			}

			if ("END".equalsIgnoreCase(property.getName().trim())) {
				String componentName = property.getValue().trim().toUpperCase();
				if (componentName.length() == 0) {
					listener.onWarning(Warning.EMPTY_END, null, null, context);
					continue;
				}

				//find the component that this END property matches up with
				int popCount = stack.popCount(componentName);
				if (popCount == 0) {
					//END property does not match up with any BEGIN properties, so ignore
					listener.onWarning(Warning.UNMATCHED_END, null, null, context);
					continue;
				}

				while (popCount > 0) {
					String poppedName = stack.pop();
					listener.onComponentEnd(poppedName, context);
					popCount--;
				}
				continue;
			}

			if ("VERSION".equalsIgnoreCase(property.getName())) {
				String parentComponent = stack.peekName();
				if (syntaxRules.hasSyntaxRules(parentComponent)) {
					SyntaxStyle style = syntaxRules.getSyntaxStyle(parentComponent, property.getValue());
					if (style == null) {
						listener.onWarning(Warning.UNKNOWN_VERSION, property, null, context);
					} else {
						listener.onVersion(property.getValue(), context);
						stack.updateSyntax(style);
						continue;
					}
				}
			}

			listener.onProperty(property, context);
		}
	}

	/**
	 * Parses the next property off the input stream.
	 * @param listener the data listener (for reporting warnings)
	 * @return the parsed property or null if the property could not be parsed
	 * @throws IOException if there was a problem reading from the input stream
	 */
	private VObjectProperty parseProperty(VObjectDataListener listener) throws IOException {
		VObjectProperty property = new VObjectProperty();

		/*
		 * The syntax style to assume the data is in.
		 */
		SyntaxStyle syntax = stack.peekSyntax();

		/*
		 * The name of the parameter we're currently inside of.
		 */
		String curParamName = null;

		/*
		 * The character that was used to escape the current character (for
		 * parameter values).
		 */
		char paramValueEscapeChar = 0;

		/*
		 * Are we currently inside a parameter value that is surrounded with
		 * double-quotes?
		 */
		boolean inQuotes = false;

		/*
		 * Are we currently inside the property value?
		 */
		boolean inValue = false;

		/*
		 * Does the line use quoted-printable encoding, and does it end all of
		 * its folded lines with a "=" character?
		 */
		boolean foldedQuotedPrintableLine = false;

		/*
		 * Are we currently inside the whitespace that prepends a folded line?
		 */
		boolean inFoldedLineWhitespace = false;

		/*
		 * The current character.
		 */
		char ch = 0;

		/*
		 * The previous character.
		 */
		char prevChar;

		while (true) {
			prevChar = ch;

			int read = nextChar();
			if (read < 0) {
				//end of stream
				eos = true;
				break;
			}

			ch = (char) read;

			if (prevChar == '\r' && ch == '\n') {
				/*
				 * The newline was already processed when the "\r" character was
				 * encountered, so ignore the accompanying "\n" character.
				 */
				continue;
			}

			if (isNewline(ch)) {
				foldedQuotedPrintableLine = (inValue && prevChar == '=' && property.getParameters().isQuotedPrintable());
				if (foldedQuotedPrintableLine) {
					/*
					 * Remove the "=" character that sometimes appears at the
					 * end of quoted-printable lines that are followed by a
					 * folded line.
					 */
					buffer.chop();
					context.unfoldedLine.chop();
				}

				//keep track of the current line number
				lineNumber++;

				continue;
			}

			if (isNewline(prevChar)) {
				if (isWhitespace(ch)) {
					/*
					 * This line is a continuation of the previous line (the
					 * line is folded).
					 */
					inFoldedLineWhitespace = true;
					continue;
				}

				if (foldedQuotedPrintableLine) {
					/*
					 * The property's parameters indicate that the property
					 * value is quoted-printable. And the previous line ended
					 * with an equals sign. This means that folding whitespace
					 * may not be prepended to folded lines like it should.
					 */
				} else {
					/*
					 * We're reached the end of the property.
					 */
					leftOver = ch;
					break;
				}
			}

			if (inFoldedLineWhitespace) {
				if (isWhitespace(ch) && syntax == SyntaxStyle.OLD) {
					/*
					 * 2.1 allows multiple whitespace characters to be used for
					 * folding (section 2.1.3).
					 */
					continue;
				}
				inFoldedLineWhitespace = false;
			}

			context.unfoldedLine.append(ch);

			if (inValue) {
				buffer.append(ch);
				continue;
			}

			//decode escaped parameter value character
			if (paramValueEscapeChar != 0) {
				char escapeChar = paramValueEscapeChar;
				paramValueEscapeChar = 0;

				switch (escapeChar) {
				case '\\':
					switch (ch) {
					case '\\':
						buffer.append(ch);
						continue;
					case ';':
						/*
						 * Semicolons can only be escaped in old style parameter
						 * values. If a new style parameter value has
						 * semicolons, the value should be surrounded in double
						 * quotes.
						 */
						buffer.append(ch);
						continue;
					}
					break;
				case '^':
					switch (ch) {
					case '^':
						buffer.append(ch);
						continue;
					case 'n':
						buffer.append(NEWLINE);
						continue;
					case '\'':
						buffer.append('"');
						continue;
					}
					break;
				}

				/*
				 * Treat the escape character as a normal character because it's
				 * not a valid escape sequence.
				 */
				buffer.append(escapeChar).append(ch);
				continue;
			}

			//check for a parameter value escape character
			if (curParamName != null) {
				switch (syntax) {
				case OLD:
					if (ch == '\\') {
						paramValueEscapeChar = ch;
						continue;
					}
					break;
				case NEW:
					if (ch == '^' && caretDecodingEnabled) {
						paramValueEscapeChar = ch;
						continue;
					}
					break;
				}
			}

			//set the group
			if (ch == '.' && property.getGroup() == null && property.getName() == null) {
				property.setGroup(buffer.getAndClear());
				continue;
			}

			if ((ch == ';' || ch == ':') && !inQuotes) {
				if (property.getName() == null) {
					//set the property name
					property.setName(buffer.getAndClear());
				} else {
					//set a parameter value
					String paramValue = buffer.getAndClear();
					if (syntax == SyntaxStyle.OLD) {
						//old style allows whitespace to surround the "=", so remove it
						paramValue = ltrim(paramValue);
					}
					property.getParameters().put(curParamName, paramValue);
					curParamName = null;
				}

				if (ch == ':') {
					//the rest of the line is the property value
					inValue = true;
				}
				continue;
			}

			if (property.getName() != null) {
				//it's a multi-valued parameter
				if (ch == ',' && curParamName != null && !inQuotes && syntax != SyntaxStyle.OLD) {
					String paramValue = buffer.getAndClear();
					property.getParameters().put(curParamName, paramValue);
					continue;
				}

				//set the parameter name
				if (ch == '=' && curParamName == null) {
					String paramName = buffer.getAndClear().toUpperCase();
					if (syntax == SyntaxStyle.OLD) {
						//old style allows whitespace to surround the "=", so remove it
						paramName = rtrim(paramName);
					}
					curParamName = paramName;
					continue;
				}

				//entering/leaving a double-quoted parameter value (new style only)
				if (ch == '"' && curParamName != null && syntax != SyntaxStyle.OLD) {
					inQuotes = !inQuotes;
					continue;
				}
			}

			buffer.append(ch);
		}

		/*
		 * Line or stream ended before the property value was reached.
		 */
		if (!inValue) {
			return null;
		}

		property.setValue(buffer.getAndClear());
		if (property.getParameters().isQuotedPrintable()) {
			decodeQuotedPrintable(property, listener);
		}

		return property;
	}

	/**
	 * Decodes the given property's value from quoted-printable encoding.
	 * @param property the property
	 * @param listener the data listener
	 */
	private void decodeQuotedPrintable(VObjectProperty property, VObjectDataListener listener) {
		Charset charset = getCharset(property, listener);
		if (charset == null) {
			charset = defaultQuotedPrintableCharset;
		}

		String value = property.getValue();
		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		try {
			value = codec.decode(value);
		} catch (DecoderException e) {
			listener.onWarning(Warning.QUOTED_PRINTABLE_ERROR, property, e, context);
			return;
		}

		property.setValue(value);
	}

	/**
	 * Gets the character set the given property is encoded in.
	 * @param property the property
	 * @param listener the data listener
	 * @return the character set or null if the character is not set or could
	 * not be determined
	 */
	private Charset getCharset(VObjectProperty property, VObjectDataListener listener) {
		Exception thrown;
		try {
			return property.getParameters().getCharset();
		} catch (IllegalCharsetNameException e) {
			//name contains illegal characters
			thrown = e;
		} catch (UnsupportedCharsetException e) {
			//not recognized by the JVM
			thrown = e;
		}

		listener.onWarning(Warning.UNKNOWN_CHARSET, property, thrown, context);
		return null;
	}

	/**
	 * Gets the next character in the input stream.
	 * @return the next character or -1 if the end of the stream has been
	 * reached
	 * @throws IOException if there's a problem reading from the input stream
	 */
	private int nextChar() throws IOException {
		if (leftOver >= 0) {
			/*
			 * Use the character that was left over from the previous invocation
			 * of "readLine()".
			 */
			int ch = leftOver;
			leftOver = -1;
			return ch;
		}

		return reader.read();
	}

	/**
	 * Determines if the given character is a newline character.
	 * @param ch the character
	 * @return true if it's a newline character, false if not
	 */
	private static boolean isNewline(char ch) {
		return ch == '\n' || ch == '\r';
	}

	/**
	 * Determines if the given character is a space or a tab.
	 * @param ch the character
	 * @return true if it's a space or a tab, false if not
	 */
	private static boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t';
	}

	/**
	 * Keeps track of the hierarchy of nested components and their syntax
	 * styles.
	 */
	private static class ComponentStack {
		/**
		 * The hierarchy of components the parser is currently inside of.
		 */
		private final List<String> names = new ArrayList<String>();

		/**
		 * <p>
		 * The syntax style of each component in the hierarchy.
		 * </p>
		 * 
		 * <p>
		 * Note: This will always be one element larger than the "names" list
		 * because it must remember the style of the "root" (for properties that
		 * are not inside of a component, should there happen to be any).
		 * </p>
		 */
		private final List<SyntaxStyle> syntax = new ArrayList<SyntaxStyle>();

		/**
		 * Creates a new stack.
		 * @param defaultSyntax the default syntax style
		 */
		public ComponentStack(SyntaxStyle defaultSyntax) {
			syntax.add(defaultSyntax);
		}

		/**
		 * Pushes a component onto the stack.
		 * @param component the component name
		 */
		public void push(String component) {
			names.add(component);
			syntax.add(peekSyntax());
		}

		/**
		 * Removes the top component from the stack.
		 * @return the name of the component that was removed
		 */
		public String pop() {
			syntax.remove(syntax.size() - 1);
			return names.remove(names.size() - 1);
		}

		/**
		 * Gets the number of calls to {@link #pop()} it would take to pop the
		 * given component name.
		 * @param name the component name
		 * @return the number of pops or 0 if the name was not found
		 */
		public int popCount(String name) {
			int index = names.lastIndexOf(name);
			return (index < 0) ? 0 : names.size() - index;
		}

		/**
		 * Gets the top component name.
		 * @return the top component name or null if the name stack is empty
		 */
		public String peekName() {
			return names.isEmpty() ? null : names.get(names.size() - 1);
		}

		/**
		 * Gets the top syntax style.
		 * @return the top syntax style or null if the syntax stack is empty
		 */
		public SyntaxStyle peekSyntax() {
			return syntax.isEmpty() ? null : syntax.get(syntax.size() - 1);
		}

		/**
		 * Replaces the top syntax style.
		 * @param style the syntax style
		 */
		public void updateSyntax(SyntaxStyle style) {
			syntax.set(syntax.size() - 1, style);
		}
	}

	/**
	 * Closes the underlying input stream.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
