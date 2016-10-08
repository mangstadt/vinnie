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

package com.github.mangstadt.vinnie.validate;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.github.mangstadt.vinnie.SyntaxStyle;

/**
 * <p>
 * Checks properties for illegal characters.
 * </p>
 * <p>
 * Two kinds of checking are supported: strict and non-strict. Strict ensures
 * that the data adhere to the specifications. Non-strict allows all characters
 * to be used, as long as they do not break the syntax.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * SyntaxStyle style = SyntaxStyle.NEW;
 * String name = "NOTE #2";
 * 
 * boolean strict = false;
 * assertTrue(VObjectValidator.validatePropertyName(name, style, strict));
 * 
 * strict = true;
 * assertFalse(VObjectValidator.validatePropertyName(name, style, strict));
 * </pre>
 * @author Michael Angstadt
 */
public class VObjectValidator {
	private static final Map<SyntaxStyle, Map<Boolean, AllowedCharacters>> propertyName = new EnumMap<SyntaxStyle, Map<Boolean, AllowedCharacters>>(SyntaxStyle.class);
	static {
		boolean strict;
		SyntaxStyle syntax;

		syntax = SyntaxStyle.OLD;
		{
			Map<Boolean, AllowedCharacters> map = new HashMap<Boolean, AllowedCharacters>();
			strict = false;
			{
				//@formatter:off
				map.put(strict, new AllowedCharacters.Builder()
					.allowAll()
					.except("\r\n:.;")
				.build());
				//@formatter:on
			}

			strict = true;
			{
				//@formatter:off
				map.put(strict, new AllowedCharacters.Builder()
					.allowPrintable()
					.except("[]=:.,")
				
					/*
					 * Note: The specification's formal grammar allows semicolons to
					 * be present in property name. This may be a mistake because
					 * this would break the syntax. This validator will treat
					 * semicolons as invalid in this context.
					 * 
					 * The specifications state that semicolons can be included in
					 * parameter values by escaping them with a backslash--however,
					 * the specification is not clear as to whether this is also
					 * permitted in property names.
					 * 
					 * vCard 2.1: Section 2.1.2
					 * vCal 1.0: Section 2, "Property" sub-heading
					 */
					.except(';')
				.build());
				//@formatter:on
			}

			propertyName.put(syntax, map);
		}

		syntax = SyntaxStyle.NEW;
		{
			Map<Boolean, AllowedCharacters> map = new HashMap<Boolean, AllowedCharacters>();
			strict = false;
			{
				//same as old style syntax
				map.put(strict, propertyName.get(SyntaxStyle.OLD).get(strict));
			}

			strict = true;
			{
				//@formatter:off
				map.put(strict, new AllowedCharacters.Builder()
					.allow('A', 'Z')
					.allow('a', 'z')
					.allow('0', '9')
					.allow('-')
				.build());
				//@formatter:on
			}

			propertyName.put(syntax, map);
		}
	}

	private static final Map<SyntaxStyle, Map<Boolean, AllowedCharacters>> group = propertyName;

	private static final Map<SyntaxStyle, Map<Boolean, AllowedCharacters>> parameterName = new EnumMap<SyntaxStyle, Map<Boolean, AllowedCharacters>>(SyntaxStyle.class);
	static {
		boolean strict;
		SyntaxStyle syntax;

		syntax = SyntaxStyle.OLD;
		{
			Map<Boolean, AllowedCharacters> map = new HashMap<Boolean, AllowedCharacters>();
			strict = false;
			{
				//@formatter:off
				map.put(strict, new AllowedCharacters.Builder()
					.allowAll()
					.except("\r\n:;=")
				.build());
				//@formatter:on
			}

			strict = true;
			{
				//same as property name
				map.put(strict, propertyName.get(syntax).get(strict));
			}

			parameterName.put(syntax, map);
		}

		syntax = SyntaxStyle.NEW;
		{
			Map<Boolean, AllowedCharacters> map = new HashMap<Boolean, AllowedCharacters>();
			strict = false;
			{
				//same as old style syntax
				map.put(strict, parameterName.get(SyntaxStyle.OLD).get(strict));
			}

			strict = true;
			{
				//same as property name
				map.put(strict, propertyName.get(syntax).get(strict));
			}

			parameterName.put(syntax, map);
		}
	}

	private static final Map<SyntaxStyle, Map<Boolean, Map<Boolean, AllowedCharacters>>> parameterValue = new EnumMap<SyntaxStyle, Map<Boolean, Map<Boolean, AllowedCharacters>>>(SyntaxStyle.class);
	static {
		boolean strict, caretEncoding;
		SyntaxStyle syntax;

		syntax = SyntaxStyle.OLD;
		{
			Map<Boolean, Map<Boolean, AllowedCharacters>> map = new HashMap<Boolean, Map<Boolean, AllowedCharacters>>();
			caretEncoding = false;
			{
				Map<Boolean, AllowedCharacters> map2 = new HashMap<Boolean, AllowedCharacters>();
				strict = false;
				{
					//@formatter:off
					map2.put(strict, new AllowedCharacters.Builder()
						.allowAll()
						.except("\r\n:")
					.build());
					//@formatter:on
				}

				strict = true;
				{
					//same as parameter name, except semicolons are allowed
					//@formatter:off
					AllowedCharacters paramName = parameterName.get(syntax).get(strict);
					map2.put(strict, new AllowedCharacters.Builder(paramName)
						.allow(';')
					.build());
					//@formatter::on
				}
				map.put(caretEncoding, map2);
			}

			caretEncoding = true;
			{
				/*
				 * Same as when caret encoding is disabled because
				 * old style syntax does not support caret encoding.
				 */
				map.put(caretEncoding, map.get(false));
			}

			parameterValue.put(syntax, map);
		}

		syntax = SyntaxStyle.NEW;
		{
			Map<Boolean, Map<Boolean, AllowedCharacters>> map = new HashMap<Boolean, Map<Boolean, AllowedCharacters>>();
			caretEncoding = false;
			{
				Map<Boolean, AllowedCharacters> map2 = new HashMap<Boolean, AllowedCharacters>();
				strict = false;
				{
					//@formatter:off
					map2.put(strict, new AllowedCharacters.Builder()
						.allowAll()
						.except("\r\n\"")
					.build());
					//@formatter:on
				}

				strict = true;
				{
					//@formatter:off
					map2.put(strict, new AllowedCharacters.Builder()
						.allowPrintable()
						.allowNonAscii()
						.allow('\t')
						.except('"')
					.build());
					//@formatter:on
				}

				map.put(caretEncoding, map2);
			}

			caretEncoding = true;
			{
				Map<Boolean, AllowedCharacters> map2 = new HashMap<Boolean, AllowedCharacters>();
				strict = false;
				{
					//@formatter:off
					map2.put(strict, new AllowedCharacters.Builder()
						.allowAll()
					.build());
					//@formatter:on
				}

				strict = true;
				{
					//@formatter:off
					map2.put(strict, new AllowedCharacters.Builder()
						.allowPrintable()
						.allowNonAscii()
						.allow("\r\n\t")
					.build());
					//@formatter:on
				}

				map.put(caretEncoding, map2);
			}

			parameterValue.put(syntax, map);
		}
	}

	/**
	 * Validates a property name.
	 * @param name the property name
	 * @param syntax the syntax style to validate against
	 * @param strict false to allow all characters as long as they don't break
	 * the syntax, true for spec-compliant validation
	 * @return true if the property name is valid, false if not
	 */
	public static boolean validatePropertyName(String name, SyntaxStyle syntax, boolean strict) {
		return allowedCharactersPropertyName(syntax, strict).check(name);
	}

	/**
	 * Gets the list of allowed characters for property names.
	 * @param syntax the syntax style
	 * @param strict false for the non-strict list, true for the spec-compliant
	 * list
	 * @return the character list
	 */
	public static AllowedCharacters allowedCharactersPropertyName(SyntaxStyle syntax, boolean strict) {
		return propertyName.get(syntax).get(strict);
	}

	/**
	 * Validates a group name.
	 * @param group the group name
	 * @param syntax the syntax style to validate against
	 * @param strict false to allow all characters as long as they don't break
	 * the syntax, true for spec-compliant validation
	 * @return true if the group name is valid, false if not
	 */
	public static boolean validateGroupName(String group, SyntaxStyle syntax, boolean strict) {
		return allowedCharactersGroup(syntax, strict).check(group);
	}

	/**
	 * Gets the list of allowed characters for group names.
	 * @param syntax the syntax style
	 * @param strict false for the non-strict list, true for the spec-compliant
	 * list
	 * @return the character list
	 */
	public static AllowedCharacters allowedCharactersGroup(SyntaxStyle syntax, boolean strict) {
		return group.get(syntax).get(strict);
	}

	/**
	 * Validates a parameter name.
	 * @param name the parameter name
	 * @param syntax the syntax style to validate against
	 * @param strict false to allow all characters as long as they don't break
	 * the syntax, true for spec-compliant validation
	 * @return true if the parameter name is valid, false if not
	 */
	public static boolean validateParameterName(String name, SyntaxStyle syntax, boolean strict) {
		return allowedCharactersParameterName(syntax, strict).check(name);
	}

	/**
	 * Gets the list of allowed characters for parameter names.
	 * @param syntax the syntax style
	 * @param strict false for the non-strict list, true for the spec-compliant
	 * list
	 * @return the character list
	 */
	public static AllowedCharacters allowedCharactersParameterName(SyntaxStyle syntax, boolean strict) {
		return parameterName.get(syntax).get(strict);
	}

	/**
	 * Validates a parameter value.
	 * @param value the parameter value
	 * @param syntax the syntax style to validate against
	 * @param caretEncoding true if caret encoding is enabled, false if not
	 * @param strict false to allow all characters as long as they don't break
	 * the syntax, true for spec-compliant validation
	 * @return true if the parameter value is valid, false if not
	 */
	public static boolean validateParameterValue(String value, SyntaxStyle syntax, boolean caretEncoding, boolean strict) {
		return allowedCharactersParameterValue(syntax, caretEncoding, strict).check(value);
	}

	/**
	 * Gets the list of allowed characters for parameter values.
	 * @param syntax the syntax style
	 * @param caretEncoding true if caret encoding is enabled, false if not
	 * @param strict false for the non-strict list, true for the spec-compliant
	 * list
	 * @return the character list
	 */
	public static AllowedCharacters allowedCharactersParameterValue(SyntaxStyle syntax, boolean caretEncoding, boolean strict) {
		return parameterValue.get(syntax).get(caretEncoding).get(strict);
	}

	private VObjectValidator() {
		//hide
	}
}
