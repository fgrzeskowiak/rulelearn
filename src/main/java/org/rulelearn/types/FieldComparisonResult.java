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
 * Result of comparing two fields preference type of the attribute which values are represented by fields are taken into account. 
 * 
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public enum FieldComparisonResult {
	/**
	 * Relation in question between two fields is verified positively.
	 */
	TRUE,
	/**
	 * Relation in question between two fields is verified negatively.
	 */
	FALSE,
	/**
	 * Two fields are uncomparable (i.e., not-comparable). Relation in question between two fields can be neither verified positively nor negatively.
	 */
	UNCOMPARABLE
}