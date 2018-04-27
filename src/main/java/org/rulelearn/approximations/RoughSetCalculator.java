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

import it.unimi.dsi.fastutil.ints.IntSortedSet;

/**
 * Basic rough set calculator capable of calculating lower/upper approximation and boundary of an approximated set.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 * 
 * @param<T> type of an approximated set for which this calculator can be applied
 */
public interface RoughSetCalculator<T extends ApproximatedSet> {
	
	/**
	 * Calculates the lower approximation of the given set.
	 * 
	 * @param set set of objects that is going to be approximated
	 * @return set if indices of objects belonging to the lower approximation of the given set
	 */
	public abstract IntSortedSet getLowerApproximation(T set);
	
	/**
	 * Calculates the upper approximation of the given set.
	 * 
	 * @param set set of objects that is going to be approximated
	 * @return set if indices of objects belonging to the upper approximation of the given set
	 */
	public abstract IntSortedSet getUpperApproximation(T set);
	
	/**
	 * Calculates the boundary of the given set.
	 * 
	 * @param set set of objects that is going to be approximated
	 * @return set if indices of objects belonging to the boundary of the given set
	 */
	public abstract IntSortedSet getBoundary(T set);
	
}
