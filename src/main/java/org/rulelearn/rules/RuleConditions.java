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

import static org.rulelearn.core.Precondition.notNull;

import java.util.List;

import org.rulelearn.approximations.ApproximatedSet;
import org.rulelearn.core.Precondition;
import org.rulelearn.data.InformationTable;
import org.rulelearn.types.EvaluationField;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * List (complex) of elementary conditions on the left-hand side (LHS) of a decision rule induced to cover objects from a single approximated set {@link ApproximatedSet}.
 * Each condition is identified by its position on the list.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class RuleConditions {
	
	/**
	 * Elementary conditions, in order of their addition to rule's LHS.
	 */
	List<Condition<? extends EvaluationField>> conditions;
	
	/**
	 * Indices of all objects that satisfy right-hand side (RHS, decision part) of induced decision rule.
	 * In case of a certain/possible rule, these are the objects from considered approximated set.
	 */
	IntSet indicesOfPositiveObjects; //e.g., IntOpenHashSet from fastutil library
	
	/**
	 * Indices of objects from learning information (decision) table that are taken into account when generating elementary conditions considered for addition to induced decision rule.
	 * In case of inducing a certain decision rule, one should assume that base objects are those belonging to the lower approximation of the approximated set.
	 * In case of inducing a possible decision rule, base objects are those belonging to the upper approximation of the approximated set.
	 * In case of inducing an approximate decision rule, base objects are those belonging to the boundary of the approximated set.<br>
	 * <br>
	 * This set is a subset of {@link #indicesOfObjectsThatCanBeCovered}.
	 */
	IntSet indicesOfElementaryConditionsBaseObjects; //lower/upper approximation, or boundary of considered approximated set
	
	/**
	 * Indices of all objects from learning information (decision) table that can be covered by these rule conditions (allowed objects).<br>
	 * <br>
	 * This set is a superset of {@link #indicesOfElementaryConditionsBaseObjects} and a superset of {@link #indicesOfNeutralObjects}.
	 */
	IntSet indicesOfObjectsThatCanBeCovered; //allowed objects, AO
	
	/**
	 * Indices of all neutral objects from learning information (decision) table that can be covered by these rule conditions. Can be an empty set.
	 * This set is a subset of {@link #indicesOfObjectsThatCanBeCovered}.
	 * Neutral objects are such that their decision is uncomparable with the limiting decision of considered approximated set {@link ApproximatedSet#getLimitingDecision()}.
	 */
	IntSet indicesOfNeutralObjects;
	
	/**
	 * Learning information (decision) table in context of which this complex of elementary conditions is evaluated.
	 */
	InformationTable learningInformationTable;
	
	/**
	 * Indices of attributes from learning information table for which conditions are already stored in {@link #conditions}.
	 */
	Int2IntMap attributeIndices;
	
	/**
	 * Indices of objects from learning information table covered by these rule conditions.
	 */
	IntList indicesOfCoveredObjects;
	
	/**
	 * Stores for each object index the number of conditions from these rule conditions that do not cover that object (i.e., "eliminate" that object from the set of covered objects).
	 * If {@code notCoveringConditionsCounts[objectIndex] = 2}, it means that these rule conditions contain two conditions that do not cover object indexed by {@code objectIndex}.
	 * If {@code notCoveringConditionsCounts[objectIndex] = 0}, it means that these rule conditions cover object indexed by {@code objectIndex}.
	 * This field is initialized with all zeros.
	 */
	int[] notCoveringConditionsCounts; 
	
	/**
	 * Iterator among objects covered by the rule conditions.
	 *
	 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
	 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
	 */
	public class CoveredObjectsIterator {
		
		/**
		 * Index of last found covered object.
		 */
		private int lastCoveredObjectIndex = -1;
	
		/**
		 * Gets index of next object covered by the rule conditions, or {@code -1} if there is no such object.
		 * 
		 * @return index of next object covered by the rule conditions, or {@code -1} if there is no such object
		 */
		public int next() {
			int objectsCount = learningInformationTable.getNumberOfObjects();
			for (int i = lastCoveredObjectIndex + 1; i < objectsCount; i++) {
				if (covers(i)) {
					lastCoveredObjectIndex = i;
					return lastCoveredObjectIndex;
				}
			}
			return -1;
		}
	}
	
	/**
	 * Initializes {@link #notCoveringConditionsCounts} by setting, for each object from the learning information table, count equal to zero.
	 * Assumes that {@link #learningInformationTable} is already set.
	 */
	private void initializeNotCoveringConditionsCounts() {
		int objectsCount = this.learningInformationTable.getNumberOfObjects();
		this.notCoveringConditionsCounts = new int[objectsCount];
		
		for (int objectIndex = 0; objectIndex < objectsCount; objectIndex++) {
			this.notCoveringConditionsCounts[objectIndex] = 0;
		}
	}
	
	/**
	 * Checks if these rule conditions cover the object from the learning information table having given index.
	 * 
	 * @param objectIndex index of an object in the learning information table
	 * @return {@code true} if these rule conditions cover the object with given index, {@code false} otherwise
	 * 
	 * @throws IndexOutOfBoundsException if given object index does not correspond to any object in the learning information table
	 */
	public boolean covers(int objectIndex) {
		int conditionsCount = this.conditions.size();
		for (int i = 0; i < conditionsCount; i++) {
			if (!this.conditions.get(i).satisfiedBy(objectIndex, this.learningInformationTable)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets an iterator among ordered set of indices of objects from learning information table that are covered by this rule conditions, as returned by {@link #covers(int)}.
	 * 
	 * @return an iterator among ordered set of indices of objects from learning information table that are covered by this rule conditions
	 */
	public CoveredObjectsIterator getCoveredObjectsIterator() {
		return new CoveredObjectsIterator();
	}
	
	/**
	 * Constructor setting learning information table and the sets of indices of:
	 * <ul>
	 *   <li>all objects that satisfy right-hand side (RHS, decision part) of induced decision rule
	 *   (in case of a certain/possible rule, these are the objects from considered approximated set),</li>
	 *   <li>objects from learning information (decision) table that are taken into account when generating elementary conditions considered for addition to induced decision rule,</li>
	 *   <li>all objects from the information table that can be covered by these rule conditions.</li>
	 * </ul>
	 * In case of inducing a certain decision rule, positive objects are those belonging to the lower approximation of considered approximated set.
	 * In case of inducing a possible decision rule, positive objects are those belonging to the upper approximation of the approximated set.
	 * In case of inducing an approximate decision rule, positive objects are those belonging to the boundary of the approximated set.<br>
	 * <br> 
	 * The set of neutral objects (as returned by {@link #getIndicesOfNeutralObjects()}, is initialized as an empty set {@link IntSets#EMPTY_SET}.
	 * Neutral objects are such objects that their decision is uncomparable with the limiting decision of considered approximated set {@link ApproximatedSet#getLimitingDecision()}.
	 * 
	 * @param learningInformationTable learning information table for which these rule conditions are constructed
	 * @param indicesOfPositiveObjects set of indices of positive objects from the given information table, i.e., all objects that satisfy right-hand side (RHS, decision part) of induced decision rule;
	 *        in case of a certain/possible rule, these are the objects from considered approximated set
	 * @param indicesOfElementaryConditionsBaseObjects indices of objects from learning information (decision) table that are taken into account
	 *        when generating elementary conditions considered for addition to induced decision rule; it is expected that this set is a subset of {@code indicesOfObjectsThatCanBeCovered}
	 * @param indicesOfObjectsThatCanBeCovered indices of objects from the given learning information (decision) table that can be covered by these rule conditions
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	public RuleConditions(InformationTable learningInformationTable, IntSet indicesOfPositiveObjects, IntSet indicesOfElementaryConditionsBaseObjects, IntSet indicesOfObjectsThatCanBeCovered) {
		this(learningInformationTable, indicesOfPositiveObjects, indicesOfElementaryConditionsBaseObjects, indicesOfObjectsThatCanBeCovered, IntSets.EMPTY_SET);
	}

	/**
	 * Constructor setting learning learning information table and the sets of indices of:
	 * <ul>
	 *    <li>all objects that satisfy right-hand side (RHS, decision part) of induced decision rule
	 *   (in case of a certain/possible rule, these are the objects from considered approximated set),</li>
	 *   <li>objects from learning information (decision) table that are taken into account when generating elementary conditions considered for addition to induced decision rule,</li>
	 *   <li>all objects from the information table that can be covered by these rule conditions,</li>
	 *   <li>objects from the information table that are neutral with respect to these rule conditions.</li>
	 * </ul>
	 * In case of inducing a certain decision rule, positive objects are those belonging to the lower approximation of considered approximated set.
	 * In case of inducing a possible decision rule, positive objects are those belonging to the upper approximation of the approximated set.
	 * In case of inducing an approximate decision rule, positive objects are those belonging to the boundary of the approximated set.<br>
	 * <br>
	 * Neutral objects are such objects that their decision is uncomparable with the limiting decision of considered approximated set {@link ApproximatedSet#getLimitingDecision()}.
	 * 
	 * @param learningInformationTable learning information table for which these rule conditions are constructed
	 * @param indicesOfPositiveObjects set of indices of positive objects from the given information table, i.e., all objects that satisfy right-hand side (RHS, decision part) of induced decision rule;
	 *        in case of a certain/possible rule, these are the objects from considered approximated set; this object should be unmodifiable
	 * @param indicesOfElementaryConditionsBaseObjects indices of objects from learning information (decision) table that are taken into account
	 *        when generating elementary conditions considered for addition to induced decision rule; it is expected that this set is a subset of {@code indicesOfObjectsThatCanBeCovered};
	 *        this object should be unmodifiable
	 * @param indicesOfObjectsThatCanBeCovered indices of objects from the given learning information (decision) table that can be covered by these rule conditions; this object should be unmodifiable
	 * @param indicesOfNeutralObjects indices of neutral objects from the given learning information (decision) table; it is expected that this set is a subset of {@code indicesOfObjectsThatCanBeCovered};
	 *        this object should be unmodifiable
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	public RuleConditions(InformationTable learningInformationTable, IntSet indicesOfPositiveObjects, IntSet indicesOfElementaryConditionsBaseObjects, IntSet indicesOfObjectsThatCanBeCovered, IntSet indicesOfNeutralObjects) {
		this.learningInformationTable = notNull(learningInformationTable, "Information table is null.");
		this.indicesOfPositiveObjects = notNull(indicesOfPositiveObjects, "Set of indices of positive objects is null.");
		this.indicesOfElementaryConditionsBaseObjects = notNull(indicesOfElementaryConditionsBaseObjects, "Set of indices of elementary conditions base objects is null.");
		this.indicesOfObjectsThatCanBeCovered = notNull(indicesOfObjectsThatCanBeCovered, "Set of indices of objects that can be covered is null.");
		this.indicesOfNeutralObjects = notNull(indicesOfNeutralObjects, "Set of indices of neutral objects is null.");
		
		this.conditions = new ObjectArrayList<Condition<?>>();
		this.attributeIndices = new Int2IntOpenHashMap();
		this.indicesOfCoveredObjects = new IntArrayList();
		
		int objectsCount = learningInformationTable.getNumberOfObjects();
		for (int objectIndex = 0; objectIndex < objectsCount; objectIndex++) {
			this.indicesOfCoveredObjects.add(objectIndex); //initially all objects are covered
		}
		
		initializeNotCoveringConditionsCounts();
	}
	
	/**
	 * Gets indices of all objects that satisfy right-hand side (RHS, decision part) of induced decision rule.
	 * In case of a certain/possible rule, these are the objects from considered approximated set.
	 * 
	 * @return indices of all objects that satisfy right-hand side (RHS, decision part) of induced decision rule
	 */
	public IntSet getIndicesOfPositiveObjects() {
		return this.indicesOfPositiveObjects;
	}
	
	/**
	 * Gets the set of indices of objects from learning information (decision) table that are taken into account when generating elementary conditions considered for addition to induced decision rule.<br>
	 * <br>
	 * In case of inducing a certain decision rule, base objects are those belonging to the lower approximation of considered approximated set.
	 * In case of inducing a possible decision rule, base objects are those belonging to the upper approximation of the approximated set.
	 * In case of inducing an approximate decision rule, base objects are those belonging to the boundary of the approximated set.<br>
	 * <br>
	 * The returned set is a subset of set returned by {@link #getIndicesOfObjectsThatCanBeCovered()}.
	 * 
	 * @return the set of indices of objects from the information table that are taken into account when generating elementary conditions considered for addition to induced decision rule
	 */
	public IntSet getIndicesOfElementaryConditionsBaseObjects() {
		return this.indicesOfElementaryConditionsBaseObjects;
	}
	
	/**
	 * Gets indices of all objects from the learning information (decision) table that can be covered by these rule conditions.
	 * 
	 * @return the set of indices of all objects from the learning information (decision) table that can be covered by these rule conditions
	 */
	public IntSet getIndicesOfObjectsThatCanBeCovered() {
		return this.indicesOfObjectsThatCanBeCovered;
	}
	
	/**
	 * Gets indices of all neutral objects from learning information (decision) table that can be covered by these rule conditions.
	 * Neutral objects are such that their decision is uncomparable with the limiting decision of considered approximated set {@link ApproximatedSet#getLimitingDecision()}.<br>
	 * <br>
	 * The returned set is a subset of set returned by {@link #getIndicesOfObjectsThatCanBeCovered()}.
	 * 
	 * @return indices of all neutral objects from learning information (decision) table that can be covered by these rule conditions
	 */
	public IntSet getIndicesOfNeutralObjects() {
		return this.indicesOfNeutralObjects;
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
	 * Adds given condition to this complex of rule's conditions.
	 * 
	 * @param condition new condition to add
	 * @return index of added condition
	 * 
	 * @throws NullPointerException if given condition is {@code null}
	 */
	public int addCondition(Condition<?> condition) {
		this.conditions.add(notNull(condition, "Condition is null."));
		
		int attributeIndex = condition.getAttributeWithContext().getAttributeIndex();
		int count = this.attributeIndices.containsKey(attributeIndex) ? this.attributeIndices.get(attributeIndex) : 0;
		this.attributeIndices.put(attributeIndex, count + 1);
		
		updateCoveredObjectsWithCondition(this.indicesOfCoveredObjects, condition);
		updateNotCoveringConditionsCountsWithCondition(condition);
		
		return this.conditions.size() - 1;
	}
	
	/**
	 * Gets indices of objects covered by these rules conditions.
	 * 
	 * @return indices of objects covered by these rules conditions;
	 *         this list is in general non-ordered if at least one condition has been removed from these rule conditions
	 */
	public IntList getIndicesOfCoveredObjects() {
		return IntLists.unmodifiable(this.indicesOfCoveredObjects);
	}
	
	/**
	 * Gets indices of objects covered by these rules conditions assuming addition of given condition.
	 * 
	 * @param condition condition that can be added to these rule conditions
	 * @return indices of objects covered by these rules conditions assuming addition of given condition
	 * 
	 * @throws NullPointerException if given condition is {@code null}
	 */
	public IntList getIndicesOfCoveredObjectsWithCondition(Condition<?> condition) {
		notNull(condition, "Condition is null.");
		
		IntList indicesOfCoveredObjects = new IntArrayList(this.indicesOfCoveredObjects); //copy list
		updateCoveredObjectsWithCondition(indicesOfCoveredObjects, condition);
		
		return indicesOfCoveredObjects;
	}
	
	/**
	 * Gets indices of objects covered by these rule conditions if condition with given index is dropped.
	 * 
	 * @param conditionIndex index of condition in these rule conditions that is considered to be removed
	 * @return indices of objects covered by these rule conditions if condition with given index is dropped
	 * 
	 * @throws IndexOutOfBoundsException if given condition index is less than zero or too big concerning number of stored conditions
	 */
	public IntList getIndicesOfCoveredObjectsWithoutCondition(int conditionIndex) {
		IntList indicesOfCoveredObjects = new IntArrayList(this.indicesOfCoveredObjects); //copy list
		updateCoveredObjectsWithoutCondition(indicesOfCoveredObjects, conditionIndex, false);
		
		return indicesOfCoveredObjects;
	}
	
	/**
	 * Updates given set of indices of objects covered by these rule conditions assuming addition of given condition.
	 * 
	 * @param indicesOfCoveredObjects indices of objects covered so far by these rule conditions
	 * @param condition condition that is going to or can be added to these rule conditions
	 */
	private void updateCoveredObjectsWithCondition(IntList indicesOfCoveredObjects, Condition<?> condition) {
		IntSet nonCoveredObjects = new IntOpenHashSet(indicesOfCoveredObjects.size()); //ensures faster execution of indicesOfCoveredObjects.removeAll(nonCoveredObjects); expected capacity ensures no need of rehashing)
		
		for (int objectIndex : indicesOfCoveredObjects) { //iterate over already covered objects to see if they remain covered or get "rejected" by the given condition
			if (!condition.satisfiedBy(objectIndex, this.learningInformationTable)) {
				nonCoveredObjects.add(objectIndex);
			}
		}
		
		indicesOfCoveredObjects.removeAll(nonCoveredObjects);
	}
	
	/**
	 * Updates counts of not covering conditions {@link #notCoveringConditionsCounts} in view of adding given condition.
	 * Assumes that {@link #learningInformationTable} and {@link #notCoveringConditionsCounts} are already set.
	 */
	private void updateNotCoveringConditionsCountsWithCondition(Condition<?> condition) {
		int objectsCount = this.learningInformationTable.getNumberOfObjects();
		
		for (int objectIndex = 0; objectIndex < objectsCount; objectIndex++) { //iterate over all objects to see which are not covered by the given condition
			if (!condition.satisfiedBy(objectIndex, this.learningInformationTable)) { //condition eliminates given object
				this.notCoveringConditionsCounts[objectIndex] = this.notCoveringConditionsCounts[objectIndex] + 1; //increase counter for considered object
			}
		}
	}
	
	/**
	 * Updates given set of indices of objects covered by these rule conditions, and optionally (depending on the flag) also array with counts of conditions not covering particular objects,<br>
	 * assuming removal of condition with given index.
	 * 
	 * @param indicesOfCoveredObjects indices of objects covered by these rule conditions;
	 *        this parameter is modified to reflect the situation
	 *        when condition with given index is dropped from these rule conditions
	 * @param conditionIndex index of condition considered to be removed from these rule conditions
	 * @param tells if {@link #notCoveringConditionsCounts} should be also updated, apart of the given set of indices of objects covered by these rule conditions
	 * 
	 * @throws NullPointerException if the given list is {@code null}
	 * @throws IndexOutOfBoundsException if given condition index is less than zero or too big concerning number of stored conditions
	 */
	private void updateCoveredObjectsWithoutCondition(IntList indicesOfCoveredObjects, int conditionIndex, boolean updateNotCoveringConditionsCounts) {
		Precondition.notNull(indicesOfCoveredObjects, "Indices of covered objects are null.");
		
		Condition<?> condition = this.getCondition(conditionIndex); //validates given index of condition
		int numberOfObjects = this.notCoveringConditionsCounts.length;
		
		if (updateNotCoveringConditionsCounts) {
			int count;
			//update this.notCoveringConditionsCounts and given indicesOfCoveredObjects
			for (int objectIndex = 0; objectIndex < numberOfObjects; objectIndex++) {
				count = this.notCoveringConditionsCounts[objectIndex];
				if (count > 0) { //something can change
					if (!condition.satisfiedBy(objectIndex, this.learningInformationTable)) { //dropped condition eliminated current object
						this.notCoveringConditionsCounts[objectIndex] = --count;
						if (count == 0) {
							indicesOfCoveredObjects.add(objectIndex);
						}
					}
				}
			}
		} else {
			//update only given indicesOfCoveredObjects
			for (int objectIndex = 0; objectIndex < numberOfObjects; objectIndex++) {
				if (this.notCoveringConditionsCounts[objectIndex] == 1) { //something can change
					if (!condition.satisfiedBy(objectIndex, this.learningInformationTable)) { //dropped condition eliminated current object
						indicesOfCoveredObjects.add(objectIndex);
					}
				}
			}
		}
		
		
	}
	
	/**
	 * Removes from the list of conditions the condition with given index.
	 * 
	 * @param conditionIndex index of a condition to remove from this list of conditions
	 * @throws IndexOutOfBoundsException if given index does not refer to any stored condition
	 */
	public void removeCondition(int conditionIndex) {
		int attributeIndex = getCondition(conditionIndex).getAttributeWithContext().getAttributeIndex();
		
		//first call method that gets considered condition ...
		this.updateCoveredObjectsWithoutCondition(this.indicesOfCoveredObjects, conditionIndex, true);
		//...and only then remove that condition
		this.conditions.remove(conditionIndex);
		this.attributeIndices.put(attributeIndex, this.attributeIndices.get(attributeIndex) - 1); //decrease count
	}
	
	/**
	 * Gets list of elementary conditions building this complex of elementary conditions.
	 * The order of conditions on the list reflects the order in which they have been added.
	 * 
	 * @return list of elementary conditions building this complex of elementary conditions,
	 *         in order of their addition
	 */
	public List<Condition<? extends EvaluationField>> getConditions() { //TODO: return unmodifiable list
		return this.conditions;
	}
	
	/**
	 * Gets an elementary condition building this list of elementary conditions and indexed by the given value.
	 * 
	 * @param conditionIndex index of a condition on this list of conditions
	 * @return an elementary condition building this list of elementary conditions and indexed by the given value
	 * 
	 * @throws IndexOutOfBoundsException if given index is less than zero or too big concerning number of stored conditions
	 */
	public Condition<?> getCondition(int conditionIndex) {
		if (conditionIndex < 0 || conditionIndex >= this.conditions.size()) {
			throw new IndexOutOfBoundsException("Condition index is less than zero or too big concerning number of stored conditions.");
		}
		
		return this.conditions.get(conditionIndex);
	}
	
	/**
	 * Gets index of the first occurrence of the specified condition in this list of conditions.
	 *  
	 * @param condition condition whose index should be found
	 * @return index of the given condition, or -1 if given condition has not been found
	 * 
	 * @throws NullPointerException if given condition is {@code null}
	 */
	public int getConditionIndex(Condition<?> condition) {
		return this.conditions.indexOf(Precondition.notNull(condition, "Condition is null."));
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
	
	/**
	 * Tells if attribute with given index is already involved in at least one of elementary conditions.
	 * 
	 * @param attributeIndex index of an attribute
	 * @return {@code true} if the attribute with given index is involved in at least one of elementary conditions,
	 *         {@code false} otherwise
	 */
	public boolean hasConditionForAttribute(int attributeIndex) {
		return (this.attributeIndices.containsKey(attributeIndex) && (this.attributeIndices.get(attributeIndex) > 0));
	}
	
	/**
	 * Gets coverage information concerning induced decision rule.
	 * 
	 * @return coverage information concerning induced decision rule
	 * @see RuleCoverageInformation
	 */
	public RuleCoverageInformation getRuleCoverageInformation() {
		return new RuleCoverageInformation(this.indicesOfPositiveObjects, this.indicesOfNeutralObjects, this.indicesOfCoveredObjects,
				this.learningInformationTable.getNumberOfObjects());
	}
	
}
