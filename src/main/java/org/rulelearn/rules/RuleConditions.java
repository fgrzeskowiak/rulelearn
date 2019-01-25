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

package org.rulelearn.rules;

import java.util.List;
import org.rulelearn.data.InformationTable;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import static org.rulelearn.core.Precondition.notNull;

/**
 * List (complex) of elementary conditions on the LHS of a decision rule. Each condition is identified by its position on the list.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class RuleConditions {

	/**
	 * Elementary conditions, in order of their addition to rule's LHS.
	 */
	protected List<Condition<?>> conditions;
	
	/**
	 * Indices of objects from learning information (decision) table that are considered to be positive objects for this complex of rule conditions.
	 * In case of inducing a certain decision rule, one should assume that positive objects are those belonging to the lower approximation of an approximated set,
	 * or that positive objects are those belonging to the approximated set.
	 * In case of inducing a possible decision rule, positive objects are those belonging to the upper approximation of the approximated set.
	 * The meaning of the notion "positive object" is such, that this complex of rule conditions should be appreciated for covering "positive objects" and/or for not covering the other objects ("non-positive" ones).
	 */
	protected IntSet indicesOfPositiveObjects; //e.g., IntOpenHashSet from fastutil library
	
	/**
	 * Learning information (decision) table in context of which this complex of elementary conditions is evaluated.
	 */
	protected InformationTable learningInformationTable = null;
	
	/**
	 * Constructor setting learning information table and the set of indices of positive objects from this table.
	 * 
	 * @param learningInformationTable information table containing positive and negative objects
	 * @param indicesOfPositiveObjects set of indices of positive objects from the given information table
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	public RuleConditions(InformationTable learningInformationTable, IntSet indicesOfPositiveObjects) {
		this.learningInformationTable = notNull(learningInformationTable, "Information table is null.");
		this.indicesOfPositiveObjects = notNull(indicesOfPositiveObjects, "Set of indices of positive objects is null.");
		
		this.conditions = new ObjectArrayList<Condition<?>>();
	}
	
	/**
	 * Gets the set of indices of positive objects
	 * 
	 * @return the set of indices of positive objects
	 */
	public IntSet getIndicesOfPositiveObjects() {
		return this.indicesOfPositiveObjects;
	}

	/**
	 * Gets the learning information table.
	 * 
	 * @return the learning information table
	 */
	public InformationTable getLearningInformationTable() {
		return this.learningInformationTable;
	}

	/**
	 * Tells if object with given index is positive for this set of rule conditions.
	 * 
	 * @param objectIndex index of an object in learning information table
	 * @return {@code true} if object with given index is positive for this set of rule conditions
	 *         {@code false} otherwise
	 */
	public boolean objectIsPositive(int objectIndex) {
		return this.indicesOfPositiveObjects.contains(objectIndex);
	}
	
	/**
	 * Adds given condition to this complex of rule's conditions.
	 * 
	 * @param condition new condition to add
	 * @return index of added condition
	 * 
	 * @throws NullPointerException if condition does not conform to {@link org.rulelearn.core.Precondition#notNull(Object, String)}
	 */
	public int addCondition(Condition<?> condition) {
		this.conditions.add(notNull(condition, "Condition is null."));
		return this.conditions.size() - 1;
	}
	
	/**
	 * Removes from the list of conditions the condition with given index.
	 * 
	 * @param index index of a condition to remove from this list of conditions
	 * @throws IndexOutOfBoundsException if given index does not refer to any stored condition
	 */
	public void removeCondition(int index) {
		this.conditions.remove(index);
	}
	
	/**
	 * Gets list of elementary conditions building this complex of elementary conditions.
	 * The order of conditions on the list reflects the order in which they have been added.
	 * 
	 * @return list of elementary conditions building this complex of elementary conditions,
	 *         in order of their addition
	 */
	public List<Condition<?>> getConditions() {
		return this.conditions;
	}
	
	/**
	 * Gets an elementary condition building this list of elementary conditions and indexed by the given value.
	 * 
	 * @param index index of a condition on this list of conditions
	 * @return an elementary condition building this list of elementary conditions and indexed by the given value
	 */
	public Condition<?> getCondition(int index) {
		return this.conditions.get(index);
	}
	
	/**
	 * Gets number of conditions on this list of conditions.
	 * 
	 * @return number of conditions on this list of conditions
	 */
	public int size() {
		return this.conditions.size();
	}
	
	/**
	 * Checks if this list contains given condition.
	 * 
	 * @param condition condition whose presence on the list of conditions should be verified
	 * @return {@code true} if this list contains given condition, {@code false} otherwise
	 */
	public boolean containsCondition(Condition<?> condition) {
		return this.conditions.contains(condition);
	}
	
}
