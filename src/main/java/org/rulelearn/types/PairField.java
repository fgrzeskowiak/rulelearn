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

import org.rulelearn.utils.UncomparableException;

/**
 * Field composed of two simple fields of the same sub-type of {@link SimpleField}.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 *
 */
public class PairField<T extends SimpleField> extends CompositeField {
	
	/**
	 * The first value in this pair.
	 */
	protected T firstValue;
	/**
	 * The second value in this pair.
	 */
	protected T secondValue;
	
	/**
	 * Constructor setting both values.
	 * 
	 * @param firstValue first value of this pair
	 * @param secondValue second value of this pair
	 */
	public PairField(T firstValue, T secondValue) {
		this.firstValue = firstValue;
		this.secondValue = secondValue;
	}

	@Override
	public int compareToEx(Field otherObject) throws UncomparableException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TernaryLogicValue isAtLeastAsGoodAs(Field otherField) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TernaryLogicValue isAtMostAsGoodAs(Field otherField) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TernaryLogicValue isEqualTo(Field otherField) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TernaryLogicValue isDifferentThan(Field otherField) {
		// TODO Auto-generated method stub
		return null;
	}

}
