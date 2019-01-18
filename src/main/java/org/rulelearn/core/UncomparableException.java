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

package org.rulelearn.core;

/**
 * Exception thrown when semantically uncomparable objects of the same type are requested to be compared.
 * Such situation may occur, e.g., when comparing two fields of type {@link org.rulelearn.types.PairField} in an information table.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class UncomparableException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -5460524209459293416L;

	/**
	 * Constructs an exception with message of failure reason.
	 * 
	 * @param message message of this exception
	 */
	public UncomparableException(String message) {
		super(message);
	}

}
