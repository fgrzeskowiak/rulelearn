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

package org.rulelearn.data;

/**
 * Data available for the learning process, aggregating table with values of active condition attributes for considered objects,
 * and preference information concerning these objects.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class LearningData {
	
	/**
	 * Table with values of active condition attributes for considered objects.
	 * Only such attributes are used in the learning process.
	 */
	protected Table learningTable;
	/**
	 * Preference information concerning considered objects.
	 */
	protected PreferenceInformation preferenceInformation;
	
	/**
	 * Constructor storing table with values of active condition attributes
	 * and preference information concerning considered objects.
	 * 
	 * @param conditionTable table with values of active condition attributes for considered objects
	 * @param preferenceInformation preference information concerning considered objects
	 */
	public LearningData(Table conditionTable, PreferenceInformation preferenceInformation) {
		this.learningTable = conditionTable;
		this.preferenceInformation = preferenceInformation;
	}

	/**
	 * Gets table with values of active condition attributes for objects that this learning data concern. 
	 * 
	 * @return table with values of active condition attributes for objects that this learning data concern.
	 */
	public Table getConditionTable() {
		return this.learningTable;
	}

	/**
	 * Gets preference information concerning objects that this learning data concern.
	 * 
	 * @return preference information concerning objects that this learning data concern
	 */
	public PreferenceInformation getPreferenceInformation() {
		return this.preferenceInformation;
	}
	
	/**
	 * TODO: write documentation
	 * 
	 * @param objectIndices
	 * @return
	 */
	public LearningData select(int[] objectIndices) {
		return select(objectIndices, false);
	}
	
	/**
	 * TODO: write documentation
	 * 
	 * @param objectIndices
	 * @param accelerateByReadOnlyResult
	 * @return
	 */
	public LearningData select(int[] objectIndices, boolean accelerateByReadOnlyResult) {
		//TODO: implement
		return null;
	}

}
