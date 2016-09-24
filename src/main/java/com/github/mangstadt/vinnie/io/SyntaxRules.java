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

import java.util.HashMap;
import java.util.Map;

import com.github.mangstadt.vinnie.SyntaxStyle;

/**
 * Defines a set of rules that determine what kind of syntax the vobject data
 * stream uses.
 * @author Michael Angstadt
 */
public class SyntaxRules {
	private SyntaxStyle defaultSyntaxStyle;
	private final Map<String, Map<String, SyntaxStyle>> syntaxRules = new HashMap<String, Map<String, SyntaxStyle>>();

	/**
	 * Creates a new set of syntax rules.
	 * @param defaultSyntaxStyle the syntax style to use when it can't be
	 * determined what the data stream uses
	 */
	public SyntaxRules(SyntaxStyle defaultSyntaxStyle) {
		this.defaultSyntaxStyle = defaultSyntaxStyle;
	}

	/**
	 * Gets the the syntax style to use when it can't be determined what the
	 * data stream uses.
	 * @return the default syntax style
	 */
	public SyntaxStyle getDefaultSyntaxStyle() {
		return defaultSyntaxStyle;
	}

	/**
	 * Sets the syntax style to use when it can't be determined what the data
	 * stream uses.
	 * @param defaultSyntaxStyle the default syntax style (cannot be null)
	 */
	public void setDefaultSyntaxStyle(SyntaxStyle defaultSyntaxStyle) {
		this.defaultSyntaxStyle = defaultSyntaxStyle;
	}

	/**
	 * Determines if this object contains rules for the given component.
	 * @param component the component name (e.g. "VCARD")
	 * @return true if this component has syntax rules, false if not
	 */
	public boolean hasSyntaxRules(String component) {
		if (component != null) {
			component = component.toUpperCase();
		}
		return syntaxRules.containsKey(component);
	}

	/**
	 * Gets the syntax style to use for a given component.
	 * @param component the component name (e.g. "VCARD")
	 * @param versionValue the value of the component's VERSION property (e.g.
	 * "2.1")
	 * @return the syntax style or null if none was found
	 */
	public SyntaxStyle getSyntaxStyle(String component, String versionValue) {
		component = (component == null) ? null : component.toUpperCase();
		Map<String, SyntaxStyle> rules = syntaxRules.get(component);
		return (rules == null) ? null : rules.get(versionValue);
	}

	/**
	 * Adds a rule.
	 * @param component the name of the component that contains the VERSION
	 * property (e.g. "VCARD"), or null if the VERSION property will not be
	 * inside of any components
	 * @param version the value of the VERSION property
	 * @param syntaxStyle the syntax style to use when a VERSION property with
	 * the given value, and under the given component, is encountered
	 */
	public void addRule(String component, String version, SyntaxStyle syntaxStyle) {
		component = (component == null) ? null : component.toUpperCase();

		Map<String, SyntaxStyle> rules = syntaxRules.get(component);
		if (rules == null) {
			rules = new HashMap<String, SyntaxStyle>();
			syntaxRules.put(component, rules);
		}

		rules.put(version, syntaxStyle);
	}

	/**
	 * Creates a set of rules for iCalendar data.
	 * @return the rules
	 */
	public static SyntaxRules iCalendar() {
		/*
		 * Initialize to the old style syntax because the VERSION property can
		 * technically exist anywhere inside the data stream under this version.
		 * 
		 * However, this setting is rarely important in practice because I've
		 * never seen an iCalendar object that doesn't put its VERSION property
		 * at the very beginning.
		 */
		SyntaxRules rules = new SyntaxRules(SyntaxStyle.OLD);
		String component = "VCALENDAR";
		rules.addRule(component, "1.0", SyntaxStyle.OLD);
		rules.addRule(component, "2.0", SyntaxStyle.NEW);
		return rules;
	}

	/**
	 * Creates a set of rules for vCard data.
	 * @return the rules
	 */
	public static SyntaxRules vcard() {
		/*
		 * Initialize to the old style syntax because the VERSION property can
		 * technically exist anywhere inside the data stream under this version.
		 * 
		 * However, this setting is rarely important in practice because I've
		 * never seen a vCard that doesn't put its VERSION property at the very
		 * beginning.
		 */
		SyntaxRules rules = new SyntaxRules(SyntaxStyle.OLD);
		String component = "VCARD";
		rules.addRule(component, "2.1", SyntaxStyle.OLD);
		rules.addRule(component, "3.0", SyntaxStyle.NEW);
		rules.addRule(component, "4.0", SyntaxStyle.NEW);
		return rules;
	}
}
