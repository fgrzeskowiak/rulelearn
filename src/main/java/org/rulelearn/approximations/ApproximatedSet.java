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

package org.rulelearn.approximations;

import org.rulelearn.data.Decision;
import org.rulelearn.data.InformationTable;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import static org.rulelearn.core.Precondition.notNull;
import org.rulelearn.core.TernaryLogicValue;

/**
 * Top level class for all sets of objects that can be approximated using the rough set concept.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public abstract class ApproximatedSet {
	
	/**
	 * Set of indices of objects belonging to the lower approximation of this approximated set.
	 */
	protected IntSortedSet lowerApproximation = null;
	
	/**
	 * Set of indices of objects belonging to the upper approximation of this approximated set.
	 */
	protected IntSortedSet upperApproximation = null;
	
	/**
	 * Set of indices of objects belonging to the boundary of this approximated set.
	 */
	protected IntSortedSet boundary = null;
	
	/**
	 * Information table containing, among other objects, the objects belonging to this approximated set.
	 */
	protected InformationTable informationTable;
	
	/**
	 * Rough set calculator used to calculate approximations and boundary of this set.
	 */
	protected RoughSetCalculator<? extends ApproximatedSet> roughSetCalculator;
	
	/**
	 * Set of indices of objects from the information table that are inconsistent with the objects belonging to the lower approximation of this approximated set.
	 */
	protected IntSet inconsistentObjectsInPositiveRegion = null;
	
	/**
	 * Set of indices of objects belonging to the positive region of this approximated set.
	 * This region is composed of objects belonging to the lower approximation of this set plus
	 * objects from the complement of this set which are inconsistent with the objects from the lower approximation.
	 */
	protected IntSet positiveRegion = null;
	/**
	 * Set of indices of objects belonging to the negative region of this approximated set.
	 */
	protected IntSet negativeRegion = null;
	/**
	 * Set of indices of objects belonging to the boundary region of this approximated set.
	 */
	protected IntSet boundaryRegion = null;
	
	/**
	 * Set with indices of objects belonging to this approximated set (so-called positive objects).
	 */
	protected IntSortedSet objects = null;
	
	/**
	 * Limiting decision, determining which objects from the information table belong to this set.
	 */
	protected Decision limitingDecision;
	
	/**
	 * Constructs this approximated set.
	 * 
	 * @param informationTable information table containing, among other objects, the objects belonging to this approximated set
	 * @param limitingDecision limiting decision, determining which objects from the information table belong to this set
	 * @param roughSetCalculator rough set calculator used to calculate approximations and boundary of this set
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	public ApproximatedSet(InformationTable informationTable, Decision limitingDecision, RoughSetCalculator<? extends ApproximatedSet> roughSetCalculator) {
		this.informationTable = notNull(informationTable, "Information table for constructed approximated set is null.");
		this.limitingDecision = notNull(limitingDecision, "Limiting decision for constructed approximated set is null.");
		this.roughSetCalculator = notNull(roughSetCalculator, "Rough set calculator for constructed approximated set is null.");
	}
	
	/**
	 * Gets the information table for which this approximated set was defined.
	 * 
	 * @return the information table for which this approximated set was defined
	 */
	public InformationTable getInformationTable() {
		return informationTable;
	}

	/**
	 * Gets indices of objects belonging to this approximated set (so-called positive objects).
	 * 
	 * @return indices of objects belonging to this approximated set
	 */
	public IntSortedSet getObjects() {
		return objects;
	}
	
	/**
	 * Gets limiting decision of this set, determining which objects from the information table belong to this set.
	 * 
	 * @return limiting decision of this set, determining which objects from the information table belong to this set
	 */
	public Decision getLimitingDecision() {
		return limitingDecision;
	}
	
	/**
	 * Gets indices of uncomparable objects from the information table such that this set's limiting decision is uncomparable with their decision.
	 * The concept of uncomparable objects is important, e.g., in case of multicriteria decision problems,
	 * when considering unions of ordered decision classes, and multiple decision criteria.
	 * Then, it may be the case that limiting decision of a union of ordered decision classes is uncomparable with decisions assigned to some objects
	 * from an {@link InformationTable}.<br>
	 * <br>
	 * If the concept of uncomparable objects is meaningless for a particular subtype of this class, then implementing method should return {@code null}.
	 * 
	 * @return indices of uncomparable objects from the information table, such that this set's limiting decision is uncomparable with their decision
	 */
	public abstract IntSortedSet getUncomparableObjects();

	/**
	 * Gets set of indices of objects belonging to the lower approximation of this approximated set.
	 * 
	 * @return set of indices of objects belonging to the lower approximation of this approximated set
	 */
	public abstract IntSortedSet getLowerApproximation();
	
	/**
	 * Gets set of indices of objects belonging to the upper approximation of this approximated set.
	 * 
	 * @return set of indices of objects belonging to the upper approximation of this approximated set
	 */
	public abstract IntSortedSet getUpperApproximation();
	
	/**
	 * Gets set of indices of objects belonging to the boundary of this approximated set.
	 * 
	 * @return set of indices of objects belonging to the boundary of this approximated set
	 */
	public abstract IntSortedSet getBoundary();
	
	/**
	 * Gets set of indices of objects belonging to the positive region of this approximated set.
	 * This region is composed of objects belonging to the lower approximation of this set plus
	 * objects from the complement of this set which are inconsistent with the objects from the lower approximation. 
	 * 
	 * @return set of indices of objects belonging to the positive region of this approximated set
	 */
	public IntSet getPositiveRegion() {
		if (this.positiveRegion == null) { //positive region not calculated yet
			IntSortedSet lowerApproximation = getLowerApproximation();
			// TODO what if null?
			IntSet inconsistentObjectsInPositiveRegion = this.getInconsistentObjectsInPositiveRegion();
			
			this.positiveRegion = new IntOpenHashSet(lowerApproximation.size() + inconsistentObjectsInPositiveRegion.size());
			
			this.positiveRegion.addAll(lowerApproximation);
			this.positiveRegion.addAll(inconsistentObjectsInPositiveRegion);
		}
		
		return this.positiveRegion;
	}
	
	/**
	 * Gets set of indices of objects belonging to the negative region of this approximated set, i.e., to the positive region of the complement of this approximated set.
	 * 
	 * @return set of indices of objects belonging to the negative region of this approximated set
	 */
	public abstract IntSet getNegativeRegion();
	
	/**
	 * Gets set of indices of objects belonging to the boundary region of this approximated set, i.e., the set of objects that are neither
	 * in the positive region nor in the negative region of this approximated set.
	 * 
	 * @return set of indices of objects belonging to the boundary region of this approximated set
	 */
	public IntSet getBoundaryRegion() {
		if (this.boundaryRegion == null) { //boundary region not calculated yet
			IntSet positiveRegion = this.getPositiveRegion();
			IntSet negativeRegion = this.getNegativeRegion();
			
			int objectsCount = this.informationTable.getNumberOfObjects();
			this.boundaryRegion = new IntOpenHashSet(objectsCount - positiveRegion.size() - negativeRegion.size());
			
			for (int i = 0; i < objectsCount; i++) {
				if (!positiveRegion.contains(i) && !negativeRegion.contains(i)) {
					this.boundaryRegion.add(i);
				}
			}
		}
		
		return this.boundaryRegion;
	}
	
	/**
	 * Gets the set of indices of objects from the information table that are inconsistent with the objects belonging to the lower approximation of this approximated set
	 *  
	 * @return set of indices of objects from the information table that are inconsistent with the objects belonging to the lower approximation of this approximated set
	 */
	public IntSet getInconsistentObjectsInPositiveRegion() {
		return this.inconsistentObjectsInPositiveRegion;
	}
	
	/**
	 * Gets accuracy of approximation of this set.
	 * This is the cardinality of the lower approximation divided by the cardinality of the upper approximation.
	 * 
	 * @return accuracy of approximation of this set by the set of all active condition attributes of the information table
	 */
	public double getAccuracyOfApproximation() {
		return (double)getLowerApproximation().size() / (double)getUpperApproximation().size();
	}
	
	/**
	 * Gets quality of approximation of this set.
	 * This is the cardinality of the lower approximation divided by the number of all objects belonging to this set.
	 * 
	 * @return quality of approximation of this set by the set of all active condition attributes of the information table
	 */
	public double getQualityOfApproximation() {
		return (double)getLowerApproximation().size() / (double)size();
	}
	
	/**
	 * Gets number of objects belonging to this approximated set.
	 * 
	 * @return number of objects belonging to this approximated set
	 */
	public int size() {
		return this.objects.size();
	}
	
	/**
	 * Checks if an object with given index, from the information table for which this approximated set has been created,
	 * belongs to this approximated set.
	 * 
	 * @param objectIndex index of an object, concerning information table for which this approximated set has been created
	 * @return {@code true} if this approximated set contains the object having given index,
	 *         {@code false} otherwise
	 */
	public boolean contains(int objectIndex) {
		return this.objects.contains(objectIndex);
	}
	
	/**
	 * Tests if this set is concordant with given decision.
	 * 
	 * @param decision decision that limiting decision of this set should be compared with
	 * @return {@link TernaryLogicValue#TRUE} if this sets' limiting decision is concordant with given decision,
	 *         {@link TernaryLogicValue#FALSE} if this sets' limiting decision is not concordant with given decision,
	 *         {@link TernaryLogicValue#UNCOMPARABLE} if this sets' limiting decision is uncomparable with given decision
	 * 
	 * @throws NullPointerException if given decision is {@code null}
	 */
	public abstract TernaryLogicValue isConcordantWithDecision(Decision decision);
	
}
