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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rulelearn.approximations.ApproximatedSet;
import org.rulelearn.approximations.ClassicalDominanceBasedRoughSetCalculator;
import org.rulelearn.approximations.DominanceBasedRoughSetCalculator;
import org.rulelearn.approximations.Unions;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.types.EvaluationField;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Integration tests for VCDomLEM algorithm.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
@Tag("integration")
class VCDomLemTest {

	/**
	 * Tests upward unions and certain rules.
	 */
	@Test
	public void testUpwardUnionCertain() {
		EpsilonConsistencyMeasure consistencyMeasure = new EpsilonConsistencyMeasure();
		double consistencyThreshold = 0.0;
		
		RuleConditionsEvaluator ruleConditionsEvaluator = consistencyMeasure;
		ConditionAdditionEvaluator[] conditionAdditionEvaluators = {consistencyMeasure};
		ConditionRemovalEvaluator[] conditionRemovalEvaluators = {consistencyMeasure};
		RuleConditionsEvaluator[] ruleConditionsEvaluators = {consistencyMeasure};
		RuleEvaluator ruleEvaluator = consistencyMeasure;
		
		ConditionGenerator conditionGenerator = new StandardConditionGenerator(conditionAdditionEvaluators);
		RuleInductionStoppingConditionChecker ruleInductionStoppingConditionChecker = new EvaluationAndCoverageStoppingConditionChecker(ruleConditionsEvaluator, consistencyThreshold);
		
		RuleConditionsPruner ruleConditionsPruner = new FIFORuleConditionsPruner(ruleInductionStoppingConditionChecker);
		RuleConditionsPruner ruleConditionsPrunerWithEvaluators = new AbstractRuleConditionsPrunerWithEvaluators(ruleInductionStoppingConditionChecker, conditionRemovalEvaluators) {
			@Override
			public RuleConditions prune(RuleConditions ruleConditions) {
				return null;
			}
		};
		RuleConditionsSetPruner ruleConditionsSetPruner = new EvaluationsAndOrderRuleConditionsSetPruner(ruleConditionsEvaluators);
		RuleMinimalityChecker ruleMinimalityChecker = new SingleEvaluationRuleMinimalityChecker(ruleEvaluator);
		
		InformationTableWithDecisionDistributions informationTable = Mockito.mock(InformationTableWithDecisionDistributions.class);
		
		DominanceBasedRoughSetCalculator roughSetCalculator = new ClassicalDominanceBasedRoughSetCalculator();
		Unions unionContainer = new Unions(informationTable, roughSetCalculator);
		ApproximatedSet[] approximatedSets = unionContainer.getUpwardUnions();
		
		RuleType ruleType = RuleType.CERTAIN; //certain/possible
		RuleSemantics ruleSemantics = RuleSemantics.AT_LEAST;
		AllowedObjectsType allowedObjectsType = AllowedObjectsType.POSITIVE_REGION;
		
		List<Rule> minimalRules = new ObjectArrayList<Rule>();
		List<RuleConditions> approximatedSetRuleConditions;
		Rule rule;
		
		for (ApproximatedSet approximatedSet : approximatedSets) {
			approximatedSetRuleConditions = calculateApproximatedSetRuleConditionsList(approximatedSet, ruleType, ruleSemantics, allowedObjectsType,
					ruleInductionStoppingConditionChecker, conditionGenerator, ruleConditionsPruner); //TODO: extend list of parameters
			//TODO: build a rule for each obtained rule conditions
			//TODO: check minimality of each rule built this way
		}
	}
	
	private List<RuleConditions> calculateApproximatedSetRuleConditionsList(ApproximatedSet approximatedSet, RuleType ruleType, RuleSemantics ruleSemantics, AllowedObjectsType allowedObjectsType,
			RuleInductionStoppingConditionChecker ruleInductionStoppingConditionChecker, ConditionGenerator conditionGenerator, RuleConditionsPruner ruleConditionsPruner) { //TODO: extend list of parameters
		
		IntSortedSet indicesOfElementaryConditionsBaseObjects = null;
		IntSet indicesOfObjectsThatCanBeCovered = null;
		
		List<RuleConditions> approximatedSetRuleConditions = new ObjectArrayList<RuleConditions>(); //the result
		
		switch (ruleType) {
		case CERTAIN:
			indicesOfElementaryConditionsBaseObjects = approximatedSet.getLowerApproximation();
			break;
		case POSSIBLE:
			indicesOfElementaryConditionsBaseObjects = approximatedSet.getUpperApproximation();
			break;
		case APPROXIMATE:
			indicesOfElementaryConditionsBaseObjects = approximatedSet.getBoundary();
			break;
		}
		
		switch (allowedObjectsType) {
		case POSITIVE_REGION:
			indicesOfObjectsThatCanBeCovered = approximatedSet.getPositiveRegion();
			indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getNeutralObjects());
			break;
		case POSITIVE_AND_BOUNDARY_REGIONS:
			indicesOfObjectsThatCanBeCovered = approximatedSet.getPositiveRegion();
			indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getBoundaryRegion());
			indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getNeutralObjects());
			break;
		case ANY_REGION:
			int numberOfObjects = approximatedSet.getInformationTable().getNumberOfObjects();
			indicesOfObjectsThatCanBeCovered = new IntOpenHashSet(numberOfObjects);
			for (int i = 0; i < numberOfObjects; i++) {
				indicesOfObjectsThatCanBeCovered.add(i);
			}
			break;
		}
		
		//TODO: test order of elements!
		IntList setB = new IntArrayList(indicesOfElementaryConditionsBaseObjects); //positive objects not already covered by rule conditions induced so far (set B from algorithm description)
		Condition<EvaluationField> bestCondition;
		RuleConditions ruleConditions;
		IntList consideredObjects;
		
		//TODO: implement
		while (!setB.isEmpty()) {
			ruleConditions = new RuleConditions(approximatedSet.getInformationTable(), approximatedSet.getObjects(), indicesOfElementaryConditionsBaseObjects, indicesOfObjectsThatCanBeCovered, approximatedSet.getNeutralObjects());
			consideredObjects = new IntArrayList(setB);
			
			while (!ruleInductionStoppingConditionChecker.isStoppingConditionSatisified(ruleConditions)) {
				try {
					bestCondition = conditionGenerator.getBestCondition(consideredObjects, ruleConditions);
					ruleConditions.addCondition(bestCondition);
					
					for (int objectIndex : consideredObjects) {
						if (!ruleConditions.covers(objectIndex)) {
							consideredObjects.rem(objectIndex); //TODO: check if it does work
						}
					}
				} catch (ElementaryConditionNotFoundException exception) {
					//TODO: handle exception
				}
			} //while
			
			ruleConditionsPruner.prune(ruleConditions); //TODO 
		}
		
		
		return approximatedSetRuleConditions;
	}

}
