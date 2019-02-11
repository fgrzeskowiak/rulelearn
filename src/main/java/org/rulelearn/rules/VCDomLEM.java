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

import org.rulelearn.approximations.ApproximatedSet;
import org.rulelearn.core.Precondition;
import org.rulelearn.core.ReadOnlyArrayReference;
import org.rulelearn.core.ReadOnlyArrayReferenceLocation;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * VC-DomLEM sequential rule induction algorithm described in:
 * J. Błaszczyński, R. Słowiński, M. Szeląg, Sequential Covering Rule Induction Algorithm for Variable Consistency Rough Set Approaches.
 * Information Sciences, 181, 2011, pp. 987-1002.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class VCDomLEM {
	
	/**
	 * VC-DomLEM parameters used for rule induction (private copy).
	 */
	VCDomLEMParameters vcDomLEMParameters; 
	
	/**
	 * Constructs this rule induction algorithm.
	 * 
	 * @param vcDomLEMParameters parameters used for rule induction
	 * @throws NullPointerException if given object is {@code null} 
	 */
	public VCDomLEM(VCDomLEMParameters vcDomLEMParameters) {
		Precondition.notNull(vcDomLEMParameters, "VC-DomLEM parameters are null.");
		this.vcDomLEMParameters = vcDomLEMParameters; //TODO: copy VC-DomLEM parameters
	}
	
	//TODO: write javadoc
	public RuleSet generateRules(ApproximatedSetProvider approximatedSetProvider, ApproximatedSetRuleDecisionsProvider approximatedSetRuleDecisionsProvider) {
		//TODO: validate method parameters
		RuleType ruleType = RuleType.CERTAIN; //TODO: use field from private VC-DomLEM parameters, once available
		RuleSemantics ruleSemantics = RuleSemantics.AT_LEAST; //TODO: use field from private VC-DomLEM parameters, once available
		AllowedObjectsType allowedObjectsType = AllowedObjectsType.POSITIVE_REGION; //TODO: use field from private VC-DomLEM parameters, once available
		
		List<RuleConditionsWithApproximatedSet> minimalRuleConditionsWithApproximatedSets = new ObjectArrayList<RuleConditionsWithApproximatedSet>(); //rule conditions for approximated sets considered so far
		List<RuleConditions> approximatedSetRuleConditions; //rule conditions for current approximated set
		List<RuleConditionsWithApproximatedSet> verifiedRuleConditionsWithApproximatedSet; //minimal rule conditions for current approximated set
		RuleConditionsWithApproximatedSet ruleConditionsWithApproximatedSet;
		
		int approximatedSetsCount = approximatedSetProvider.getCount(); //supplementary variable
		ApproximatedSet approximatedSet; //supplementary variable
		
		for (int i = 0; i < approximatedSetsCount; i++) {
			approximatedSet = approximatedSetProvider.getApproximatedSet(i);
			approximatedSetRuleConditions = calculateApproximatedSetRuleConditionsList(approximatedSet, ruleType, ruleSemantics, allowedObjectsType);
			
			verifiedRuleConditionsWithApproximatedSet = new ObjectArrayList<RuleConditionsWithApproximatedSet>();
			for (RuleConditions ruleConditions : approximatedSetRuleConditions) { //verify minimality of each rule conditions
				ruleConditionsWithApproximatedSet = new RuleConditionsWithApproximatedSet(ruleConditions, approximatedSet); 
				if (vcDomLEMParameters.getRuleMinimalityChecker().check(minimalRuleConditionsWithApproximatedSets, ruleConditionsWithApproximatedSet)) {
					verifiedRuleConditionsWithApproximatedSet.add(ruleConditionsWithApproximatedSet);
				}
			}
			
			minimalRuleConditionsWithApproximatedSets.addAll(verifiedRuleConditionsWithApproximatedSet);
		}
		
		Rule[] rules = new Rule[minimalRuleConditionsWithApproximatedSets.size()];
		RuleCoverageInformation[] ruleCoverageInformationArray = new RuleCoverageInformation[minimalRuleConditionsWithApproximatedSets.size()];
		int ruleIndex = 0;
		
		for (RuleConditionsWithApproximatedSet minimalRuleConditionsWithApproximatedSet : minimalRuleConditionsWithApproximatedSets ) {
			rules[ruleIndex] = new Rule(ruleType, ruleSemantics, minimalRuleConditionsWithApproximatedSet.getRuleConditions(),
					approximatedSetRuleDecisionsProvider.getRuleDecisions(minimalRuleConditionsWithApproximatedSet.getApproximatedSet()));
			ruleCoverageInformationArray[ruleIndex] = minimalRuleConditionsWithApproximatedSet.getRuleConditions().getRuleCoverageInformation();
			ruleIndex++;
		}
		
		return new RuleSetWithComputableCharacteristics(rules, ruleCoverageInformationArray, true); //TODO: second version of VCDomLEM returning just decision rules
	}
	
	//TODO: write javadoc
	public RuleSet generateRules(ApproximatedSetProvider approximatedSetProvider, ApproximatedSetRuleDecisionsProvider approximatedSetRuleDecisionsProvider,
			DoubleList consistencyThresholds) {
		//TODO: validate method parameters
		//this.vcDomLEMParameters.setConsistencyThresholds(consistencyThresholds); //TODO: overwrite consistency thresholds stored in internal VC-DomLEM parameters
		return this.generateRules(approximatedSetProvider, approximatedSetRuleDecisionsProvider);
	}
	
	//TODO: write javadoc
	private List<RuleConditions> calculateApproximatedSetRuleConditionsList(ApproximatedSet approximatedSet, RuleType ruleType, RuleSemantics ruleSemantics, AllowedObjectsType allowedObjectsType) {
		List<RuleConditions> approximatedSetRuleConditions = new ObjectArrayList<RuleConditions>(); //the result
		
		IntSortedSet indicesOfApproximationObjects = null; //set of objects that need to be covered (each object by at least one rule conditions)
		switch (ruleType) {
		case CERTAIN:
			indicesOfApproximationObjects = approximatedSet.getLowerApproximation();
			break;
		case POSSIBLE:
			indicesOfApproximationObjects = approximatedSet.getUpperApproximation();
			break;
		case APPROXIMATE:
			indicesOfApproximationObjects = approximatedSet.getBoundary();
			break;
		}
		
		IntSet indicesOfObjectsThatCanBeCovered = null; //indices of objects that are allowed to be covered
		if (ruleType == RuleType.CERTAIN) {
			switch (allowedObjectsType) {
			case POSITIVE_REGION:
				indicesOfObjectsThatCanBeCovered = new IntOpenHashSet(); //TODO: give expected
				indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getPositiveRegion());
				indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getNeutralObjects());
				break;
			case POSITIVE_AND_BOUNDARY_REGIONS:
				indicesOfObjectsThatCanBeCovered = new IntOpenHashSet(); //TODO: give expected
				indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getPositiveRegion());
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
		} else { //possible/approximate rule
			indicesOfObjectsThatCanBeCovered = new IntOpenHashSet(); //TODO: give expected
			indicesOfObjectsThatCanBeCovered.addAll(indicesOfApproximationObjects);
			indicesOfObjectsThatCanBeCovered.addAll(approximatedSet.getNeutralObjects());
		}
		
		//TODO: test order of elements!
		IntList setB = new IntArrayList(indicesOfApproximationObjects); //lower/upper approximation objects not already covered by rule conditions induced so far (set B from algorithm description)
		RuleConditions ruleConditions;
		RuleConditionsBuilder ruleConditionsBuilder;
		IntList indicesOfConsideredObjects; //intersection of current set B and set of objects covered by rule conditions
		
		while (!setB.isEmpty()) {
			indicesOfConsideredObjects = new IntArrayList(setB);
			
			ruleConditionsBuilder = new RuleConditionsBuilder(
					indicesOfConsideredObjects, approximatedSet.getInformationTable(),
					approximatedSet.getObjects(), indicesOfApproximationObjects, indicesOfObjectsThatCanBeCovered, approximatedSet.getNeutralObjects(),
					ruleType, ruleSemantics,
					vcDomLEMParameters.getConditionGenerator(), vcDomLEMParameters.getRuleInductionStoppingConditionChecker(), vcDomLEMParameters.getConditionSeparator());
			ruleConditions = ruleConditionsBuilder.build(); //build rule conditions
			
			ruleConditions = vcDomLEMParameters.getRuleConditionsPruner().prune(ruleConditions); //prune built rule conditions by removing redundant elementary conditions
			approximatedSetRuleConditions.add(ruleConditions);
			
			//remove objects covered by the new rule conditions
			//setB = setB \ ruleConditions.getIndicesOfCoveredObjects()
			IntSet setOfIndicesOfCoveredObjects = new IntOpenHashSet(ruleConditions.getIndicesOfCoveredObjects()); //translate list to hash set to accelerate subsequent removeAll method execution
			setB.removeAll(setOfIndicesOfCoveredObjects);
		}
	
		return vcDomLEMParameters.getRuleConditionsSetPruner().prune(approximatedSetRuleConditions, indicesOfApproximationObjects); //remove redundant rules, but keep covered all objects from lower/upper approximation
	}
	
	//TODO: write javadoc
	public VCDomLEMParameters getParameters() {
		return this.getParameters(false); //TODO: return copy of private VC-DomLEM parameters
	}
	
	//TODO: write javadoc
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT) //TODO: is this annotation right?
	public VCDomLEMParameters getParameters(boolean readOnlyResult) {
		return readOnlyResult ? this.vcDomLEMParameters : this.vcDomLEMParameters; //TODO: for readOnlyResult==false, return a copy of private VC-DomLEM parameters
	}
	
}
