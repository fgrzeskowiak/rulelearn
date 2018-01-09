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
import org.rulelearn.types.SimpleField;

/**
 * UnknownSimpleFieldMV2
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class UnknownSimpleFieldMV2 extends UnknownSimpleField {
	
	/**
	 * Checks if the given field is not {@code null} and if this field can be compared with that field (i.e., it is of type {@link SimpleField}).
	 * 
	 * @param otherField other field that this field is being compared to
	 * @return {@link TernaryLogicValue#TRUE} if this field can be compared with the other field,<br>
	 *         {@link TernaryLogicValue#FALSE} otherwise.
	 * @throws NullPointerException if the other field is {@code null}
	 */
	private TernaryLogicValue comparesWith(Field otherField) {
		if (otherField == null) {
			throw new NullPointerException("Field is null.");
		} else {
			return (otherField instanceof SimpleField) ? TernaryLogicValue.TRUE : TernaryLogicValue.UNCOMPARABLE;
		}
	}

	/**
	 * Compares this field with the other field.
	 * 
	 * @param otherField other field to which this field is being compared to
	 * @return zero, as this field is assumed to be equal to any other non-null simple field
	 * @throws NullPointerException if the other field is {@code null}
	 */
	@Override
	public int compareTo(SimpleField otherField) {
		if (otherField == null) {
			throw new NullPointerException("Field is null.");
		} else {
			return 0;
		}
	}
	
	/**
	 * Compares the other field to this field. Does the reverse comparison than {@link Comparable#compareTo(Object)}.
	 * 
	 * @param otherField other field to be compared to this field
	 * @return zero, as any other non-null simple field is assumed to be equal to this field
	 * @throws NullPointerException if the other field is {@code null}
	 */
	@Override
	public int reverseCompareTo(SimpleField otherField) {
		if (otherField == null) {
			throw new NullPointerException("Field is null.");
		} else {
			return 0;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Field> S selfClone() {
		return (S)new UnknownSimpleFieldMV2();
	}

	@Override
	public TernaryLogicValue isAtLeastAsGoodAs(Field otherField) {
		return this.comparesWith(otherField);
	}

	@Override
	public TernaryLogicValue isAtMostAsGoodAs(Field otherField) {
		return this.comparesWith(otherField);
	}

	@Override
	public TernaryLogicValue isEqualTo(Field otherField) {
		return this.comparesWith(otherField);
	}

}
