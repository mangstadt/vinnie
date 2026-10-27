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
import static com.github.mangstadt.vinnie.Utils.escapeNewlines;
import static com.github.mangstadt.vinnie.Utils.isCRLF;
import static com.github.mangstadt.vinnie.Utils.isCROrLF;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.github.mangstadt.vinnie.LaciToken;
import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.codec.EncoderException;
import com.github.mangstadt.vinnie.codec.QuotedPrintableCodec;
import com.github.mangstadt.vinnie.validate.AllowedCharacters;
import com.github.mangstadt.vinnie.validate.VObjectValidator;

/**
 * <p>
 * Writes data to a vobject data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * Writer writer = ...
 * VObjectWriter vobjectWriter = new VObjectWriter(writer, SyntaxStyle.NEW);
 * vobjectWriter.writeBeginComponent("VCARD");
 * vobjectWriter.writeVersion("4.0");
 * vobjectWriter.writeProperty("FN", "John Doe");
 * vobjectWriter.writeEndComponent("VCARD");
 * vobjectWriter.close();
 * </pre>
 * 
 * <p>
 * <b>Invalid characters</b>
 * </p>
 * <p>
 * If property data contains any invalid characters, the {@code writeProperty}
 * method throws an {@link IllegalArgumentException} and the property is not
 * written. A character is considered to be invalid if it cannot be encoded or
 * escaped, and would break the vobject syntax if written.
 * </p>
 * <p>
 * The rules regarding which characters are considered invalid is fairly
 * complex. Here are some general guidelines:
 * </p>
 * <ul>
 * <li>Try to limit group names, property names, and parameter names to
 * alphanumerics and hyphens.</li>
 * <li>Avoid the use of newlines, double quotes, and colons inside of parameter
 * values. They can be used in some contexts, but not others.</li>
 * </ul>
 * 
 * <p>
 * <b>Newlines in property values</b>
 * </p>
 * <p>
 * All newline characters ("\r" or "\n") within property values are
 * automatically escaped.
 * </p>
 * <p>
 * In old-style syntax, the property value will be encoded in quoted-printable
 * encoding.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, SyntaxStyle.OLD);
 * vobjectWriter.writeProperty("NOTE", "one\r\ntwo");
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n", sw.toString());
 * </pre>
 * 
 * <p>
 * In new-style syntax, the newline characters will be replaced with the "\n"
 * escape sequence (Windows newline sequences are replaced with a single "\n"
 * even though they consist of two characters).
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, SyntaxStyle.NEW);
 * vobjectWriter.writeProperty("NOTE", "one\r\ntwo");
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE:one\\ntwo\r\n", sw.toString());
 * </pre>
 * 
 * <p>
 * <b>Quoted-printable Encoding</b>
 * </p>
 * <p>
 * If a property has a parameter named ENCODING that has a value of
 * QUOTED-PRINTABLE (case-insensitive), then the property's value will
 * automatically be written in quoted-printable encoding.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, ...);
 * 
 * VObjectProperty note = new VObjectProperty("NOTE", "¡Hola, mundo!");
 * note.getParameters().put("ENCODING", "QUOTED-PRINTABLE");
 * vobjectWriter.writeProperty(note);
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!\r\n", sw.toString());
 * </pre>
 * <p>
 * A nameless parameter may also be used for backwards compatibility with
 * old-style syntax.
 * </p>
 * 
 * <pre class="brush:java">
 * VObjectProperty note = new VObjectProperty("NOTE", "¡Hola, mundo!");
 * note.getParameters().put(null, "QUOTED-PRINTABLE");
 * vobjectWriter.writeProperty(note);
 * </pre>
 * <p>
 * By default, the property value is encoded under the UTF-8 character set when
 * encoded in quoted-printable encoding. This can be changed by specifying a
 * CHARSET parameter. If the character set is not recognized by the local JVM,
 * then UTF-8 will be used.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, ...);
 * 
 * VObjectProperty note = new VObjectProperty("NOTE", "¡Hola, mundo!");
 * note.getParameters().put("ENCODING", "QUOTED-PRINTABLE");
 * note.getParameters().put("CHARSET", "Windows-1252");
 * vobjectWriter.writeProperty(note);
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=Windows-1252:=A1Hola, mundo!\r\n", sw.toString());
 * </pre>
 * 
 * <p>
 * <b>Circumflex Accent Encoding</b>
 * </p>
 * <p>
 * Newlines and double quote characters are not permitted inside of parameter
 * values unless circumflex accent encoding is enabled. It is turned off by
 * default.
 * </p>
 * <p>
 * Note that this encoding mechanism is defined in a separate specification and
 * may not be supported by the consumer of the vobject data. Also note that it
 * can only be used with new-style syntax.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, SyntaxStyle.NEW);
 * vobjectWriter.setCaretEncodingEnabled(true);
 * 
 * VObjectProperty note = new VObjectProperty("NOTE", "The truth is out there.");
 * note.getParameters().put("X-AUTHOR", "Fox \"Spooky\" Mulder");
 * vobjectWriter.writeProperty(note);
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE;X-AUTHOR=Fox ^'Spooky^' Mulder:The truth is out there.\r\n", sw.toString());
 * </pre>
 * 
 * <p>
 * <b>Line Folding</b>
 * </p>
 * <p>
 * Lines longer than 75 characters are automatically folded, as per the
 * vCard/iCalendar recommendation.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = new VObjectWriter(sw, ...);
 * 
 * vobjectWriter.writeProperty("NOTE", "Lorem ipsum dolor sit amet\, consectetur adipiscing elit. Vestibulum ultricies tempor orci ac dignissim.");
 * vobjectWriter.close();
 * 
 * assertEquals(
 * "NOTE:Lorem ipsum dolor sit amet\\, consectetur adipiscing elit. Vestibulum u\r\n" +
 * " ltricies tempor orci ac dignissim.\r\n"
 * , sw.toString());
 * </pre>
 * <p>
 * Line folding can be disabled.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * VObjectWriter vobjectWriter = VObjectWriter.withoutLineFolding(sw, ...);
 * 
 * vobjectWriter.writeProperty("NOTE", "Lorem ipsum dolor sit amet\, consectetur adipiscing elit. Vestibulum ultricies tempor orci ac dignissim.");
 * vobjectWriter.close();
 * 
 * assertEquals("NOTE:Lorem ipsum dolor sit amet\\, consectetur adipiscing elit. Vestibulum ultricies tempor orci ac dignissim.\r\n", sw.toString());
 * </pre>
 * 
 * <p>
 * You may specify the folding line length and indent whitespace by passing in a
 * FoldedLineWriter object. The default is 75 characters and single space
 * character respectively. Note that new-style syntax requires the folding
 * whitespace to be EXACTLY ONE character long.
 * </p>
 * 
 * <pre class="brush:java">
 * StringWriter sw = new StringWriter();
 * FoldedLineWriter flw = new FoldedLineWriter(sw, 75, "\t");
 * VObjectWriter vobjectWriter = new VObjectWriter(flw, ...);
 * 
 * vobjectWriter.writeProperty("NOTE", "Lorem ipsum dolor sit amet\, consectetur adipiscing elit. Vestibulum ultricies tempor orci ac dignissim.");
 * vobjectWriter.close();
 * 
 * assertEquals(
 * "NOTE:Lorem ipsum dolor sit amet\\, consectetur adipiscing elit. Vestibulum u\r\n" +
 * "\tltricies tempor orci ac dignissim.\r\n"
 * , sw.toString());
 * </pre>
 * @author Michael Angstadt
 */
public class VObjectWriter implements Closeable, Flushable {
	private final Writer writer;
	private boolean caretEncodingEnabled = false;
	private SyntaxStyle syntaxStyle;

	private AllowedCharacters allowedPropertyNameChars;
	private AllowedCharacters allowedGroupChars;
	private AllowedCharacters allowedParameterNameChars;
	private AllowedCharacters allowedParameterValueChars;

	/**
	 * <p>
	 * Creates a new vobject writer.
	 * </p>
	 * <p>
	 * By default, standard line folding settings will be used. To change these
	 * settings, pass a {@link FoldedLineWriter} object into this constructor.
	 * </p>
	 * @param writer the writer
	 * @param syntaxStyle the syntax style to use
	 */
	public VObjectWriter(Writer writer, SyntaxStyle syntaxStyle) {
		this(writer, syntaxStyle, true);
	}

	/**
	 * Creates a new vobject writer with line folding disabled.
	 * @param writer the writer
	 * @param syntaxStyle the syntax style to use
	 * @return the vobject writer
	 * @throws IllegalArgumentException if a {@link FoldedLineWriter} was passed
	 * into this method
	 */
	public static VObjectWriter withoutLineFolding(Writer writer, SyntaxStyle syntaxStyle) {
		if (writer instanceof FoldedLineWriter) {
			throw new IllegalArgumentException("FoldedLineWriter cannot be used here.");
		}
		return new VObjectWriter(writer, syntaxStyle, false);
	}

	private VObjectWriter(Writer writer, SyntaxStyle syntaxStyle, boolean useFoldedLineWriter) {
		if (useFoldedLineWriter) {
			this.writer = (writer instanceof FoldedLineWriter) ? (FoldedLineWriter) writer : new FoldedLineWriter(writer);
		} else {
			this.writer = writer;
		}

		setSyntaxStyle(syntaxStyle);
	}

	/**
	 * Gets the writer that is used to write data to the output stream.
	 * @return the writer
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by new style syntax.
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
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public boolean isCaretEncodingEnabled() {
		return caretEncodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by new style syntax.
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
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		caretEncodingEnabled = enable;
		allowedParameterValueChars = VObjectValidator.allowedCharactersParameterValue(syntaxStyle, enable, false);
	}

	/**
	 * Gets the syntax style the writer is using.
	 * @return the syntax style
	 */
	public SyntaxStyle getSyntaxStyle() {
		return syntaxStyle;
	}

	/**
	 * Sets the syntax style that the writer should use.
	 * @param syntaxStyle the syntax style
	 */
	public void setSyntaxStyle(SyntaxStyle syntaxStyle) {
		this.syntaxStyle = syntaxStyle;
		allowedGroupChars = VObjectValidator.allowedCharactersGroup(syntaxStyle, false);
		allowedPropertyNameChars = VObjectValidator.allowedCharactersPropertyName(syntaxStyle, false);
		allowedParameterNameChars = VObjectValidator.allowedCharactersParameterName(syntaxStyle, false);
		allowedParameterValueChars = VObjectValidator.allowedCharactersParameterValue(syntaxStyle, caretEncodingEnabled, false);
	}

	/**
	 * Writes a property marking the beginning of a component.
	 * @param componentName the component name (e.g. "VCARD")
	 * @throws IllegalArgumentException if the component name is null or empty
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeBeginComponent(String componentName) throws IOException {
		if (componentName == null || componentName.isEmpty()) {
			throw new IllegalArgumentException("Component name cannot be null or empty.");
		}
		writeProperty("BEGIN", componentName);
	}

	/**
	 * Writes a property marking the end of a component.
	 * @param componentName the component name (e.g. "VCARD")
	 * @throws IllegalArgumentException if the component name is null or empty
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeEndComponent(String componentName) throws IOException {
		if (componentName == null || componentName.isEmpty()) {
			throw new IllegalArgumentException("Component name cannot be null or empty.");
		}
		writeProperty("END", componentName);
	}

	/**
	 * Writes a "VERSION" property.
	 * @param version the version string (e.g. "2.1")
	 * @throws IllegalArgumentException if the version string is null or empty
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeVersion(String version) throws IOException {
		if (version == null || version.isEmpty()) {
			throw new IllegalArgumentException("Version string cannot be null or empty.");
		}
		writeProperty("VERSION", version);
	}

	/**
	 * Writes a property to the data stream.
	 * @param name the property name (e.g. "FN")
	 * @param value the property value
	 * @throws IllegalArgumentException if the given data contains one or more
	 * characters which would break the syntax and cannot be written
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeProperty(String name, String value) throws IOException {
		writeProperty(null, name, new VObjectParameters(), value);
	}

	/**
	 * Writes a property to the data stream.
	 * @param property the property to write
	 * @throws IllegalArgumentException if the given data contains one or more
	 * characters which would break the syntax and cannot be written
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeProperty(VObjectProperty property) throws IOException {
		writeProperty(property.getGroup(), property.getName(), property.getParameters(), property.getValue());
	}

	/**
	 * Writes a property to the data stream.
	 * @param group the group or null if there is no group
	 * @param name the property name (e.g. "FN")
	 * @param parameters the property parameters
	 * @param value the property value (will be converted to "quoted-printable"
	 * encoding if the ENCODING parameter is set to "QUOTED-PRINTABLE")
	 * @throws IllegalArgumentException if the given data contains one or more
	 * characters which would break the syntax and cannot be written
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeProperty(String group, String name, VObjectParameters parameters, String value) throws IOException {
		/*
		 * Ensure that the property is safe to write before writing it.
		 */
		validate(group, name, parameters);

		CopyOnModify parametersWrapper = new CopyOnModify(parameters);

		value = sanitizeValue(parametersWrapper, value);

		boolean useQuotedPrintable = parametersWrapper.get().isQuotedPrintable();
		if (useQuotedPrintable) {
			value = encodeQuotedPrintableValue(parametersWrapper, value);
		}

		writeGroup(group);

		writer.append(name);

		writeParameters(parametersWrapper.get());

		writer.append(':');

		if (useQuotedPrintable && writer instanceof FoldedLineWriter) {
			((FoldedLineWriter) writer).writeQuotedPrintable(value);
		} else {
			writer.write(value);
		}
		writer.write(CRLF);
	}

	/**
	 * Checks to make sure the given property data is safe to write (does not
	 * contain illegal characters, etc).
	 * @param group the property group or null if not set
	 * @param name the property name
	 * @param parameters the property parameters
	 * @throws IllegalArgumentException if there is a validation error
	 */
	private void validate(String group, String name, VObjectParameters parameters) {
		validateGroup(group, name);
		validateName(name);
		validateParameters(name, parameters);
	}

	private void validateGroup(String group, String name) {
		if (group == null) {
			return;
		}

		if (!allowedGroupChars.check(group)) {
			throw new IllegalArgumentException("Property \"" + name + "\" has its group set to \"" + group + "\".  This group name contains one or more invalid characters.  The following characters are not permitted: " + allowedGroupChars.flip());
		}
		if (beginsWithWhitespace(group)) {
			throw new IllegalArgumentException("Property \"" + name + "\" has its group set to \"" + group + "\".  This group name begins with one or more whitespace characters, which is not permitted.");
		}
	}

	private void validateName(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Property name cannot be empty.");
		}
		if (!allowedPropertyNameChars.check(name)) {
			throw new IllegalArgumentException("Property name \"" + name + "\" contains one or more invalid characters.  The following characters are not permitted: " + allowedPropertyNameChars.flip());
		}
		if (beginsWithWhitespace(name)) {
			throw new IllegalArgumentException("Property name \"" + name + "\" begins with one or more whitespace characters, which is not permitted.");
		}
	}

	private void validateParameters(String name, VObjectParameters parameters) {
		for (Map.Entry<LaciToken, List<String>> parameter : parameters) {
			//validate the parameter name
			LaciToken parameterName = parameter.getKey();
			if (parameterName == null && syntaxStyle == SyntaxStyle.NEW) {
				throw new IllegalArgumentException("Property \"" + name + "\" has a parameter whose name is null. This is not permitted with new style syntax.");
			}
			if (parameterName != null && !allowedParameterNameChars.check(parameterName.toUpperCase())) {
				throw new IllegalArgumentException("Property \"" + name + "\" has a parameter named \"" + parameterName + "\".  This parameter's name contains one or more invalid characters.  The following characters are not permitted: " + allowedParameterNameChars.flip());
			}

			//validate the parameter values
			List<String> parameterValues = parameter.getValue();
			for (String parameterValue : parameterValues) {
				if (!allowedParameterValueChars.check(parameterValue)) {
					throw new IllegalArgumentException("Property \"" + name + "\" has a parameter named \"" + parameterName + "\" whose value contains one or more invalid characters.  The following characters are not permitted: " + allowedParameterValueChars.flip());
				}
			}
		}
	}

	private String sanitizeValue(CopyOnModify parameters, String value) {
		if (value == null) {
			value = "";
		}

		switch (syntaxStyle) {
		case OLD:
			/*
			 * Old style does not support the "\n" escape sequence so encode the
			 * value in quoted-printable encoding if any newline characters
			 * exist.
			 */
			if (containsNewlines(value) && !parameters.get().isQuotedPrintable()) {
				parameters.modify().put("ENCODING", "QUOTED-PRINTABLE");
			}
			break;
		case NEW:
			value = escapeNewlines(value);
			break;
		}

		return value;
	}

	private String encodeQuotedPrintableValue(CopyOnModify parameters, String value) throws IOException {
		Charset charset = null;
		try {
			charset = parameters.get().getCharset();
		} catch (Exception e) {
			//character set not recognized
		}

		if (charset == null) {
			charset = Charset.forName("UTF-8");
			parameters.modify().replace("CHARSET", charset.name());
		}

		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		try {
			return codec.encode(value);
		} catch (EncoderException e) {
			/*
			 * Thrown if an unsupported charset is passed into the codec. This
			 * should never happen because we were able to create a Charset
			 * object above.
			 */
			throw new IOException(e);
		}
	}

	private void writeGroup(String group) throws IOException {
		if (group == null || group.isEmpty()) {
			return;
		}

		writer.append(group).append('.');
	}

	private void writeParameters(VObjectParameters parameters) throws IOException {
		for (Map.Entry<LaciToken, List<String>> parameter : parameters) {
			LaciToken parameterName = parameter.getKey();
			List<String> parameterValues = parameter.getValue();
			if (parameterValues.isEmpty()) {
				continue;
			}

			if (syntaxStyle == SyntaxStyle.OLD) {
				//e.g. ADR;TYPE=home;TYPE=work;TYPE=another,value:
				writeParameterOldStyle(parameterName, parameterValues);
			} else {
				//e.g. ADR;TYPE=home,work,"another,value":
				writeParameterNewStyle(parameterName, parameterValues);
			}
		}
	}

	private void writeParameterOldStyle(LaciToken parameterName, List<String> parameterValues) throws IOException {
		for (String parameterValue : parameterValues) {
			parameterValue = sanitizeOldStyleParameterValue(parameterValue);

			writer.append(';');
			if (parameterName != null) {
				writer.append(parameterName.toUpperCase()).append('=');
			}
			writer.append(parameterValue);
		}
	}

	private void writeParameterNewStyle(LaciToken parameterName, List<String> parameterValues) throws IOException {
		writer.append(';');
		if (parameterName != null) {
			writer.append(parameterName.toUpperCase()).append('=');
		}

		boolean first = true;
		for (String parameterValue : parameterValues) {
			parameterValue = sanitizeNewStyleParameterValue(parameterValue);

			if (!first) {
				writer.append(',');
			}

			if (shouldQuoteParameterValue(parameterValue)) {
				writer.append('"').append(parameterValue).append('"');
			} else {
				writer.append(parameterValue);
			}

			first = false;
		}
	}

	/**
	 * Determines if a string contains at least one newline character.
	 * @param string the string
	 * @return true if it contains at least one newline character, false if not
	 */
	private boolean containsNewlines(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (isCROrLF(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if a parameter value should be enclosed in double quotes.
	 * @param value the parameter value
	 * @return true if it should be enclosed in double quotes, false if not
	 */
	private boolean shouldQuoteParameterValue(String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case ',':
			case ':':
			case ';':
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if a string starts with whitespace.
	 * @param string the string
	 * @return true if it starts with whitespace, false if not
	 */
	private boolean beginsWithWhitespace(String string) {
		if (string.isEmpty()) {
			return false;
		}
		char first = string.charAt(0);
		return (first == ' ' || first == '\t');
	}

	/**
	 * <p>
	 * Sanitizes a parameter value for new style syntax.
	 * </p>
	 * <p>
	 * This method applies circumflex accent encoding, if it's enabled.
	 * Otherwise, it returns the value unchanged.
	 * </p>
	 * @param value the parameter value
	 * @return the sanitized parameter value
	 */
	private String sanitizeNewStyleParameterValue(String value) {
		return caretEncodingEnabled ? applyCaretEncoding(value) : value;
	}

	/**
	 * <p>
	 * Sanitizes a parameter value for old style syntax.
	 * </p>
	 * <p>
	 * This method escapes backslashes and semicolons.
	 * </p>
	 * @param value the parameter value
	 * @return the sanitized parameter value
	 */
	private String sanitizeOldStyleParameterValue(String value) {
		StringBuilder sb = null;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (c == '\\' || c == ';') {
				if (sb == null) {
					sb = new StringBuilder(value.length() * 2);
					sb.append(value, 0, i);
				}
				sb.append('\\');
			}

			if (sb != null) {
				sb.append(c);
			}
		}
		return (sb == null) ? value : sb.toString();
	}

	/**
	 * Applies circumflex accent encoding to a parameter value.
	 * @param value the parameter value
	 * @return the encoded value
	 */
	private String applyCaretEncoding(String value) {
		StringBuilder sb = null;
		char prev = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (isEscapableCharForCaretEncoding(c)) {
				if (isCRLF(prev, c)) {
					/*
					 * Do not write a second newline escape sequence if the
					 * newline sequence is "\r\n".
					 */
				} else {
					if (sb == null) {
						sb = new StringBuilder(value.length() * 2);
						sb.append(value, 0, i);
					}
					sb.append('^').append(caretEscape(c));
				}
			} else if (sb != null) {
				sb.append(c);
			}

			prev = c;
		}
		return (sb == null) ? value : sb.toString();
	}

	private boolean isEscapableCharForCaretEncoding(char c) {
		return c == '^' || c == '"' || c == '\r' || c == '\n';
	}

	private char caretEscape(char c) {
		switch (c) {
		case '\r':
		case '\n':
			return 'n';
		case '"':
			return '\'';
		default:
			return c;
		}
	}

	private static class CopyOnModify {
		private VObjectParameters parameters;
		private boolean copied;

		public CopyOnModify(VObjectParameters parameters) {
			this.parameters = parameters;
		}

		public VObjectParameters modify() {
			if (!copied) {
				parameters = new VObjectParameters(parameters);
				copied = true;
			}
			return get();
		}

		public VObjectParameters get() {
			return parameters;
		}
	}

	/**
	 * Flushes the underlying output stream.
	 * @throws IOException if there's a problem flushing the output stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the underlying output stream.
	 * @throws IOException if there's a problem closing the output stream
	 */
	public void close() throws IOException {
		writer.close();
	}
}
