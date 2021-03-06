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
 * Enumeration for locations of read-only array references in method's signature
 * (e.g., in method's input = passed as method's parameter; in method's output = obtained as method's result).
 * A reference to an array is considered to be read-only if it should not be used to modify array content outside the class.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public enum ReadOnlyArrayReferenceLocation {
	/**
	 * Warns that the respective method assumes that an input parameter is a read-only array reference, i.e., such reference,
	 * that once the method returns, this reference will not be used outside the class to modify array content.
	 */
	INPUT,
	/**
	 * Warns that the respective method returns a read-only array reference, i.e., a reference that should not be used outside the class to modify array content.
	 */
	OUTPUT,
	/**
	 * Warns that the respective method:<br>
	 * - assumes that an input parameter is a read-only array reference, i.e., such reference,
	 * that once the method returns, this reference will not be used outside the class to modify array content;<br>
	 * - returns a read-only array reference, i.e., a reference that should not be used outside the class to modify array content.
	 */
	INPUT_AND_OUTPUT
}

