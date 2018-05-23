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

import static org.rulelearn.core.Precondition.notNull;
import org.rulelearn.core.InvalidValueException;
import org.rulelearn.measures.object.ObjectConsistencyMeasure;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

/**
 * VCDominanceBasedRoughSetCalculator (assumes x \in X)
 * TODO: write javadoc
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class VCDominanceBasedRoughSetCalculator implements ExtendedDominanceBasedRoughSetCalculator {
	
	protected ObjectConsistencyMeasure<Union>[] lowerApproximationConsistencyMeasures;
	protected double[] lowerApproximationConsistencyThresholds;

	/**
	 * TODO: write javadoc
	 * TODO: add method with accelerateByReadOnlyParam
	 * 
	 * @param lowerApproximationConsistencyMeasures array with object consistency measures applied when calculating lower approximation
	 * @param lowerApproximationConsistencyThresholds array with thresholds for object consistency measures applied when calculating lower approximation
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidValueException if numbers of object consistency measures and respective thresholds are different
	 * @throws InvalidValueException if the array with object consistency measures is empty
	 */
	public VCDominanceBasedRoughSetCalculator(ObjectConsistencyMeasure<Union>[] lowerApproximationConsistencyMeasures, double[] lowerApproximationConsistencyThresholds) {
		super();
		notNull(lowerApproximationConsistencyMeasures, "Consistency measures are null.");
		notNull(lowerApproximationConsistencyThresholds, "Consistency thresholds are null.");
		
		if (lowerApproximationConsistencyMeasures.length != lowerApproximationConsistencyThresholds.length) {
			throw new InvalidValueException("Numbers of object consistency measures and respective thresholds are different.");
		}
		
		if (lowerApproximationConsistencyMeasures.length < 1) {
			throw new InvalidValueException("VC dominance-based rough set calculator requires at least one object consistency measure.");
		}
		
		this.lowerApproximationConsistencyMeasures = lowerApproximationConsistencyMeasures;
		this.lowerApproximationConsistencyThresholds = lowerApproximationConsistencyThresholds;
	}
	
	/**
	 * TODO: add javadoc
	 * 
	 * @param lowerApproximationConsistencyMeasure object consistency measures applied when calculating lower approximation
	 * @param lowerApproximationConsistencyThreshold threshold for object consistency measures applied when calculating lower approximation
	 * 
	 * @throws NullPointerException if lower approximation consistency measure is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public VCDominanceBasedRoughSetCalculator(ObjectConsistencyMeasure<Union> lowerApproximationConsistencyMeasure, double lowerApproximationConsistencyThreshold) {
		super();
		notNull(lowerApproximationConsistencyMeasure, "Consistency measure is null.");
		
		this.lowerApproximationConsistencyMeasures = (ObjectConsistencyMeasure<Union>[])new ObjectConsistencyMeasure[1];
		//this.lowerApproximationConsistencyMeasures = (ObjectConsistencyMeasure<Union>[])Array.newInstance(lowerApproximationConsistencyMeasures.getClass(), 1);
		this.lowerApproximationConsistencyMeasures[0] = lowerApproximationConsistencyMeasure;
		this.lowerApproximationConsistencyThresholds = new double[1];
		this.lowerApproximationConsistencyThresholds[0] = lowerApproximationConsistencyThreshold;
	}

	@Override
	public IntSortedSet calculateUpperApproximation(Union union) {
		// TODO: implement
		return null;
	}

	/* (non-Javadoc)
	 * @see org.rulelearn.approximations.ExtendedRoughSetCalculator#getLowerApproximationAndInconsistentObjects(org.rulelearn.approximations.ApproximatedSet)
	 */
	@Override
	public IntSortedSet calculateLowerApproximation(Union set) {
		// TODO: implement
		return null;
	}

}
