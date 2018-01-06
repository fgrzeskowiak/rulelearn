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

/**
 * Field representing a single unknown value (unknown value of a simple field).
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public abstract class UnknownSimpleField extends SimpleField {

//	@Override
//	@SuppressWarnings("unchecked")
//	public <S extends Field> S selfClone() {
//		return (S)new UnknownSimpleField();
//	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return {@code false}, as value of this simple field is always unknown (missing).
	 */
	@Override
	public boolean hasValue() {
		return false;
	}
	
}
