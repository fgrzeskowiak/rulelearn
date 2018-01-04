/**
 * Copyright (C) Jerzy Błaszczyński, Marcin Szeląg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rulelearn.types;

import org.rulelearn.core.TernaryLogicValue;

/**
 * Field representing enumeration value.
 * Should be instantiated using {@link EnumerationFieldFactory#create(ElementList, int, org.rulelearn.data.AttributePreferenceType)}.
 * 
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 *
 */
public abstract class EnumerationField extends SimpleField {
	/**
	 * List of elements representing enumeration.
	 */
	protected ElementList list;
	
	/**
	 * Position on element list which is equivalent to value of this field.
	 */
	protected int index = 0;
	
	/**
	 * Constructor preventing object creation.
	 */
	protected EnumerationField() {}
	
	/**
	 * Constructor setting value of this field.
	 * 
	 * @param list element list of the created field
	 * @param index position in the element list of enumeration which represents value of the field
	 */
	protected EnumerationField(ElementList list, int index) {
		this.list = list;
		// TODO should the correctness of index be checked here? (throws IndexOutOfBoundsException)
		this.index = index;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @param otherField {@inheritDoc}
	 */
	@Override
	public TernaryLogicValue isDifferentThan(Field otherField) {
		switch (this.isEqualTo(otherField)) {
			case TRUE: return TernaryLogicValue.FALSE;
			case FALSE: return TernaryLogicValue.TRUE;
			case UNCOMPARABLE: return TernaryLogicValue.UNCOMPARABLE;
			default: return TernaryLogicValue.UNCOMPARABLE;
		}
	}
	
	/**
	 * Compares this field with the other field.
	 * 
	 * @param otherField other field to be compared with this field
	 * 
	 * @return negative number when this field is smaller than the other field,<br>
	 *         zero if both fields are equal,<br>
	 *         positive number when this field is greater than the other field
	 * 
	 * @throws ClassCastException if the other field is not of type {@link EnumerationField}
	 * @throws NullPointerException if the other field is {@code null}
	 */
	@Override
	public int compareTo(SimpleField otherField) {
		EnumerationField other = (EnumerationField)otherField;
		if (this.index > other.index) {
			return 1;
		} else if (this.index < other.index) {
			return -1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Checks whether element list of this field has equal hash to element list of the given other field.
	 * 
	 * @param otherField other field that this field is being compared to
	 * @return result of comparison; see {@link TernaryLogicValue}
	 */
	public TernaryLogicValue hasEqualHashOfElementList(EnumerationField otherField) {
		return list.hasEqualHash(otherField.getElementList());
	}
	
	/**
	 * Checks whether element list of this field is equal to element list of the given other field.
	 * 
	 * @param otherField other field that this field is being compared to
	 * @return result of comparison; see {@link TernaryLogicValue}
	 */
	public TernaryLogicValue hasEqualElementList(EnumerationField otherField) {
		return list.isEqualTo(otherField.getElementList());
	}

	/**
	 * Gets the element list of enumeration.
	 * 
	 * @return the element list {@link ElementList}
	 */
	public ElementList getElementList() {
		return list;
	}

	/**
	 * Gets the position (index) on the element list of enumeration which represents value of the field.
	 * 
	 * @return index of element on list
	 */
	public int getValue() {
		return index;
	}
	
	/**
	 * Gets element.
	 * 
	 * @return {@link String} element 
	 */
	public String getElement() {
		return list.getElement(index);
	}

	/**
	 * Sets the element set of enumeration 
	 * 
	 * @param set to be set
	 *
	 * TODO setters
	 *
	public void setElementSet(ElementSet set) {
		this.set = set;
		// TODO check if index is correct
		if (index > set.)
	}*/
	
	/**
	 * Sets the position in the element set of enumeration which represents value of the field
	 * 
	 * @param index the index to set
	 *
	 * TODO setters
	 *
	public void setIndex(int index) {
		// TODO check if index is correct
		this.index = index;
	}*/
}
