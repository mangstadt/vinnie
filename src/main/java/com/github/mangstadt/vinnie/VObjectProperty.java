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

package com.github.mangstadt.vinnie;

/**
 * Represents a vobject property.
 * @author Michael Angstadt
 */
public class VObjectProperty {
	private String group;
	private String name;
	private VObjectParameters parameters;
	private String value;

	/**
	 * Creates an empty property.
	 */
	public VObjectProperty() {
		this(null, null);
	}

	/**
	 * Create a new property.
	 * @param name the property name (should contain only letters, numbers, and
	 * dashes; letters should be uppercase by convention)
	 * @param value the property value
	 */
	public VObjectProperty(String name, String value) {
		this(null, name, value);
	}

	/**
	 * Creates a new property
	 * @param group the group name (should contain only letters, numbers, and
	 * dashes; can be null)
	 * @param name the property name (should contain only letters, numbers, and
	 * dashes; letters should be uppercase by convention)
	 * @param value the property value
	 */
	public VObjectProperty(String group, String name, String value) {
		this(group, name, new VObjectParameters(), value);
	}

	/**
	 * Creates a new property
	 * @param group the group name (should contain only letters, numbers, and
	 * dashes; can be null)
	 * @param name the property name (should contain only letters, numbers, and
	 * dashes; letters should be uppercase by convention)
	 * @param parameters the property parameters (cannot be null)
	 * @param value the property value
	 */
	public VObjectProperty(String group, String name, VObjectParameters parameters, String value) {
		this.group = group;
		this.name = name;
		this.parameters = parameters;
		this.value = value;
	}

	/**
	 * Gets the group name (note: iCalendar properties do not use group names).
	 * @return the group name or null if the property doesn't have one
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets the group name (note: iCalendar properties do not use group names).
	 * @param group the group name or null to remove (should contain only
	 * letters, numbers, and dashes)
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gets the property name.
	 * @return the property name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the property name.
	 * @param name the property name (should contain only letters, numbers, and
	 * dashes; letters should be uppercase by convention)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the parameters.
	 * @return the parameters
	 */
	public VObjectParameters getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters.
	 * @param parameters the parameters (cannot be null)
	 */
	public void setParameters(VObjectParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the property value.
	 * @return the property value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the property value.
	 * @param value the property value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VObjectProperty other = (VObjectProperty) obj;
		if (group == null) {
			if (other.group != null) return false;
		} else if (!group.equals(other.group)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (parameters == null) {
			if (other.parameters != null) return false;
		} else if (!parameters.equals(other.parameters)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "VObjectProperty [group=" + group + ", name=" + name + ", parameters=" + parameters + ", value=" + value + "]";
	}
}
