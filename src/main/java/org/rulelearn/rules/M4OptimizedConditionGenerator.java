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

import org.rulelearn.core.InvalidSizeException;
import org.rulelearn.core.InvalidValueException;
import org.rulelearn.core.Precondition;
import org.rulelearn.data.AttributePreferenceType;
import org.rulelearn.data.EvaluationAttribute;
import org.rulelearn.data.EvaluationAttributeWithContext;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.Table;
import org.rulelearn.measures.Measure.MeasureType;
import org.rulelearn.rules.MonotonicConditionAdditionEvaluator.MonotonicityType;
import org.rulelearn.types.EvaluationField;
import org.rulelearn.types.KnownSimpleField;
import org.rulelearn.types.SimpleField;

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Condition generator taking advantage of the assumption that for any gain/cost-type condition attribute having {@link SimpleField} evaluations (i.e., evaluations which can be completely ordered),
 * the order of elementary conditions involving that attribute, implied by each considered condition addition evaluator, is consistent with the preference order in the value set of that attribute.<br>
 * <br>
 * For example, given attribute q with integer values, the following monotonic relationships are assumed:
 * <ul>
 *   <li>the better the attribute value t, the lower (less preferred) the evaluation of elementary condition "q(x) is at least as good as t" calculated by each gain-type condition addition evaluator,</li>
 *   <li>the better the attribute value t, the lower (more preferred) the evaluation of elementary condition "q(x) is at least as good as t" calculated by each cost-type condition addition evaluator,</li>
 *   <li>the worse the attribute value t, the lower (less preferred) the evaluation of elementary condition "q(x) is at most as good as t" calculated by each gain-type condition addition evaluator,</li>
 *   <li>the worse the attribute value t, the lower (more preferred) the evaluation of elementary condition "q(x) is at most as good as t" calculated by each cost-type condition addition evaluator,</li>
 * </ul>
 * where q(x) denotes value (evaluation) of object x with respect to attribute q.
 * 
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class M4OptimizedConditionGenerator extends AbstractConditionGeneratorWithEvaluators {

	enum ConditionComparisonResult {
		CANDIDATE_CONDITION_IS_BETTER,
		CANDIDATE_CONDITION_IS_EQUAL,
		CANDIDATE_CONDITION_IS_WORSE
	}
	
	abstract class ConditionLimitingEvaluationInterval {
		KnownSimpleField sufficientEvaluation;
		KnownSimpleField insufficientEvaluation;
		
		abstract boolean includes(KnownSimpleField evaluation, int compareToMultiplier);
	}
	
	final class RestrictingConditionLimitingEvaluationInterval extends ConditionLimitingEvaluationInterval {
		//sufficientEvaluation denotes sufficientlyRestrictiveEvaluation
		//insufficientEvaluation denotes insufficientlyRestrictiveEvaluation
		KnownSimpleField sufficientlyRestrictiveEvaluation;
		KnownSimpleField insufficientlyRestrictiveEvaluation; //TODO
		
		/**
		 * Checks if given evaluation is inside open interval<br>
		 * {@code (insufficientlyRestrictiveEvaluation, sufficientlyRestrictiveEvaluation)}, for condition of type >=, or<br>
		 * inside interval {@code (sufficientlyRestrictiveEvaluation, insufficientlyRestrictiveEvaluation)}, for condition of type <=.
		 * Assumes sufficientlyRestrictiveEvaluation is not {@code null}.
		 * 
		 * @param evaluation evaluation to be checked for its inclusion inside this interval
		 * @param compareToMultiplier multiplier taking into account both rule's semantics and attribute's preference type;
		 *        enables to compare given evaluation to limits of this interval just as if >= condition would be searched for 
		 * @return {@code true} if given evaluation is inside this interval,
		 *         {@code false} otherwise
		 */
		boolean includes(KnownSimpleField evaluation, int compareToMultiplier) {
			return (insufficientlyRestrictiveEvaluation == null || evaluation.compareTo(insufficientlyRestrictiveEvaluation) * compareToMultiplier > 0) &&
					(evaluation.compareTo(sufficientlyRestrictiveEvaluation) * compareToMultiplier < 0);
		}
	}
	
	final class GeneralizingConditionLimitingEvaluationInterval extends ConditionLimitingEvaluationInterval {
		//sufficientEvaluation denotes sufficientlyGeneralEvaluation
		//insufficientEvaluation denotes insufficientlyGeneralEvaluation
		KnownSimpleField sufficientlyGeneralEvaluation;
		KnownSimpleField insufficientlyGeneralEvaluation; //TODO
		
		/**
		 * Checks if given evaluation is inside open interval<br>
		 * {@code (sufficientlyGeneralEvaluation, insufficientlyGeneralEvaluation)}, for condition of type >=, or<br>
		 * inside interval {@code (insufficientlyGeneralEvaluation, sufficientlyGeneralEvaluation)}, for condition of type <=.
		 * Assumes sufficientlyGeneralEvaluation is not {@code null}.
		 * 
		 * @param evaluation evaluation to be checked for its inclusion inside this interval
		 * @param compareToMultiplier multiplier taking into account both rule's semantics and attribute's preference type;
		 *        enables to compare given evaluation to limits of this interval just as if >= condition would be searched for 
		 * @return {@code true} if given evaluation is inside this interval,
		 *         {@code false} otherwise
		 */
		boolean includes(KnownSimpleField evaluation, int compareToMultiplier) {
			return (evaluation.compareTo(sufficientlyGeneralEvaluation) * compareToMultiplier > 0) &&
					(insufficientlyGeneralEvaluation == null || evaluation.compareTo(insufficientlyGeneralEvaluation) * compareToMultiplier < 0);
		}
	}
	
	final class ConditionWithEvaluations {
		Condition<EvaluationField> condition;
		double[] evaluations;
		int validEvaluationsCount;
		RuleConditions ruleConditions;
		
		ConditionWithEvaluations(RuleConditions ruleConditions) {
			condition = null;
			evaluations = new double[conditionAdditionEvaluators.length];
			validEvaluationsCount = 0;
			this.ruleConditions = ruleConditions;
		}
		
		void setCondition(Condition<EvaluationField> condition) {
			this.condition = condition;
			this.validEvaluationsCount = 0;
		}
		
		void copy(ConditionWithEvaluations conditionWithEvaluations) {
			this.condition = conditionWithEvaluations.condition;
			for (int i = 0; i < conditionWithEvaluations.validEvaluationsCount; i++) {
				this.evaluations[i] = conditionWithEvaluations.evaluations[i];
			}
			this.validEvaluationsCount = conditionWithEvaluations.validEvaluationsCount;
			//this.ruleConditions = conditionWithEvaluations.ruleConditions; //not necessary as both objects are defined for the same rule conditions
		}
		
		double getEvaluation(int evaluationIndex) {
			if (evaluationIndex < validEvaluationsCount) {
				return evaluations[evaluationIndex];
			}
			if (evaluationIndex == validEvaluationsCount) {
				evaluations[evaluationIndex] = conditionAdditionEvaluators[evaluationIndex].evaluateWithCondition(ruleConditions, condition);
				validEvaluationsCount++;
				return evaluations[evaluationIndex];
			} else { //not subsequent evaluation is retrieved
				throw new InvalidValueException("Not subsequent evaluation of condition by condition addittion evaluator is being retrieved.");
			}
		}
	}
	
	/**
	 * Tells if among considered monotonic condition addition evaluators there is at least one evaluator with monotonicity type different than the other evaluators.
	 */
	boolean containsEvaluatorsOfDifferentMonotonicityType;
	
	/**
	 * Constructor for this condition generator. Stores given monotonic condition addition evaluators for use in {@link #getBestCondition(IntList, RuleConditions)}.
	 * 
	 * @param conditionAdditionEvaluators array with monotonic condition addition evaluators used lexicographically
	 * 
	 * @throws NullPointerException if given array or any of its elements is {@code null}
	 * @throws NullPointerException if type of any condition addition evaluator is {@code null}
	 * @throws NullPointerException if monotonicity type of any condition addition evaluator is {@code null}
	 * @throws InvalidSizeException if given array is empty
	 * @throws InvalidValueException if more than one switch of monotonicity type occurred when iterating from the first to the last of given monotonic condition addition evaluators
	 */
	public M4OptimizedConditionGenerator(MonotonicConditionAdditionEvaluator[] conditionAdditionEvaluators) {
		super(conditionAdditionEvaluators);
		for (MonotonicConditionAdditionEvaluator conditionEvaluator : conditionAdditionEvaluators) {
			Precondition.notNull(conditionEvaluator.getMonotonictyType(), "Monotonicty type of a monotonic condition addition evaluator is null.");
		}
		
		int monotonicityTypeSwitchCount = 0; //tells how many times monotonicity type is switched when iterating from the first to the last evaluator
		this.containsEvaluatorsOfDifferentMonotonicityType = false;
		for (int i = 1; i < conditionAdditionEvaluators.length; i++) {
			if (conditionAdditionEvaluators[i].getMonotonictyType() != conditionAdditionEvaluators[i-1].getMonotonictyType()) {
				this.containsEvaluatorsOfDifferentMonotonicityType = true;
				monotonicityTypeSwitchCount++;
				//break;
			}
		}
		
		if (monotonicityTypeSwitchCount > 1) {
			throw new InvalidValueException("More than one switch of monotonicity type occurred when iterating from the first to the last monotonic condition addition evaluator.");
		}
	}
	
	/**
	 * Tells if attributes already present in rule conditions can be skipped when generating next best condition.
	 * 
	 * @return {@code false}
	 */
	boolean skipUsedAttributes() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * During search for the best condition, scans all active condition attributes. For each such an attribute (for one column of considered learning information able),
	 * optimizes scanning of values of considered objects by skipping not relevant values.
	 * During scanning of values in one column, elementary conditions are lexicographically evaluated by the condition addition evaluators that are set in constructor.
	 * Moreover, it is assumed that evaluations of elementary conditions are monotonically dependent on the preference order of an attribute.
	 * This dependency enables to skip checking some conditions and speed up search for the best condition.
	 * 
	 * @param consideredObjects {@inheritDoc}
	 * @param ruleConditions {@inheritDoc}
	 * @return {@inheritDoc}
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws ElementaryConditionNotFoundException when it is impossible to find any new condition that could be added to given rule conditions
	 */
	@Override
	public Condition<EvaluationField> getBestCondition(IntList consideredObjects, RuleConditions ruleConditions) {
		Precondition.notNull(consideredObjects, "List of objects considered in m4-optimized condition generator is null.");
		Precondition.notNull(ruleConditions, "Rule conditions considered in m4-optimized condition generator are null.");
		
		//cast on array of monotonic evaluators (this should work, as parameter passed in constructor is of type MonotonicConditionAdditionEvaluator[])
		MonotonicConditionAdditionEvaluator[] conditionAdditionEvaluators = (MonotonicConditionAdditionEvaluator[])this.conditionAdditionEvaluators;
		MonotonicityType firstEvaluatorMonotonicityType = conditionAdditionEvaluators[0].getMonotonictyType();
		
		ConditionWithEvaluations bestConditionWithEvaluations = new ConditionWithEvaluations(ruleConditions);
		ConditionWithEvaluations candidateConditionWithEvaluations = new ConditionWithEvaluations(ruleConditions);
		Condition<EvaluationField> candidateCondition = null;
		
		InformationTable learningInformationTable = ruleConditions.getLearningInformationTable();
		Table<EvaluationAttribute, EvaluationField> data = learningInformationTable.getActiveConditionAttributeFields();
		EvaluationAttribute[] activeConditionAttributes = data.getAttributes(true);
		
		EvaluationField objectEvaluation;
		KnownSimpleField extremeLimitingEvaluation; //least/most (depending on the type of the first condition addition evaluator) restrictive limiting evaluation found so far
		KnownSimpleField candidateLimitingEvaluation; //current limiting evaluation to be compared with the most restrictive one
		RestrictingConditionLimitingEvaluationInterval restrictingConditionLimitingEvaluationInterval = null;
		GeneralizingConditionLimitingEvaluationInterval generalizingConditionLimitingEvaluationInterval = null;
		
		switch (firstEvaluatorMonotonicityType) {
		case IMPROVES_WITH_NUMBER_OF_COVERED_OBJECTS:
			generalizingConditionLimitingEvaluationInterval = new GeneralizingConditionLimitingEvaluationInterval();
			break;
		case DETERIORATES_WITH_NUMBER_OF_COVERED_OBJECTS:
			restrictingConditionLimitingEvaluationInterval = new RestrictingConditionLimitingEvaluationInterval();
			break;
		}
		
		int consideredObjectsCount = consideredObjects.size();
		int consideredObjectsSuccessfullIndex;
		int globalAttributeIndex;
		int compareToMultiplier;
		ConditionComparisonResult candidateVSBestConditionComparisonResult;
		boolean checkLessExtremeEvaluations;
		
		//go through active condition attributes
		for (int localActiveConditionAttributeIndex = 0; localActiveConditionAttributeIndex < activeConditionAttributes.length; localActiveConditionAttributeIndex++) {
			globalAttributeIndex = learningInformationTable.translateLocalActiveConditionAttributeIndex2GlobalAttributeIndex(localActiveConditionAttributeIndex);
			//current attribute should be considered
			if (!(ruleConditions.hasConditionForAttribute(globalAttributeIndex) && this.skipUsedAttributes())) {
				//optimization is possible for current attribute - it is a criterion whose evaluations can be linearly ordered
				if (activeConditionAttributes[localActiveConditionAttributeIndex].getPreferenceType() != AttributePreferenceType.NONE &&
						activeConditionAttributes[localActiveConditionAttributeIndex].getValueType() instanceof SimpleField) { //or KnownSimpleField
					
					extremeLimitingEvaluation = null;
					consideredObjectsSuccessfullIndex = -1; //index of consideredObjects corresponding to the first non-missing evaluation
					
					for (int i = 0; i < consideredObjectsCount; i++) {
						objectEvaluation = data.getField(consideredObjects.getInt(i), localActiveConditionAttributeIndex);
						if (objectEvaluation instanceof KnownSimpleField) { //non-missing evaluation found
							extremeLimitingEvaluation = (KnownSimpleField)objectEvaluation;
							consideredObjectsSuccessfullIndex = i; //remember last considered index, so next search can start from the following index
							break;
						}
					}
					
					if (consideredObjectsSuccessfullIndex >= 0) {
						//first, calculate multiplier used to compare two evaluations; if candidateEval.compareTo(referenceEval) * compareToMultiplier > 0, 
						//then candidateEval is more restrictive limiting evaluation than referenceEval, w.r.t. the constructed condition;
						//multiplier takes into account both rule's semantics and attribute's preference type
						compareToMultiplier = (activeConditionAttributes[localActiveConditionAttributeIndex].getPreferenceType() == AttributePreferenceType.GAIN ? 1 : -1) *
								(ruleConditions.getRuleSemantics() == RuleSemantics.AT_LEAST ? 1 : -1);
						
						//second, iterate through all considered objects to calculate least/most restrictive limiting evaluation of a condition,
						//taking into account rule's semantics and attribute's preference type
						for (int i = consideredObjectsSuccessfullIndex + 1; i < consideredObjectsCount; i++) { //continue loop at next index
							objectEvaluation = data.getField(consideredObjects.getInt(i), localActiveConditionAttributeIndex);
							if (objectEvaluation instanceof KnownSimpleField) { //non-missing evaluation found
								candidateLimitingEvaluation = (KnownSimpleField)objectEvaluation;
								
								switch (firstEvaluatorMonotonicityType) {
								case IMPROVES_WITH_NUMBER_OF_COVERED_OBJECTS:
									if (candidateLimitingEvaluation.compareTo(extremeLimitingEvaluation) * compareToMultiplier < 0) { //less restrictive limiting evaluation found
										extremeLimitingEvaluation = candidateLimitingEvaluation;
									}
									break;
								case DETERIORATES_WITH_NUMBER_OF_COVERED_OBJECTS:
									if (candidateLimitingEvaluation.compareTo(extremeLimitingEvaluation) * compareToMultiplier > 0) { //more restrictive limiting evaluation found
										extremeLimitingEvaluation = candidateLimitingEvaluation;
									}
									break;
								}
							}
						}
						
						//set limiting evaluations
						switch (firstEvaluatorMonotonicityType) {
						case IMPROVES_WITH_NUMBER_OF_COVERED_OBJECTS:
							//limitingEvaluationOfGeneralizingCondition.mostGeneral = extremeLimitingEvaluation;
							generalizingConditionLimitingEvaluationInterval.sufficientlyGeneralEvaluation = extremeLimitingEvaluation;
							generalizingConditionLimitingEvaluationInterval.insufficientlyGeneralEvaluation = null;
							break;
						case DETERIORATES_WITH_NUMBER_OF_COVERED_OBJECTS:
							//limitingEvaluationOfRestrictingCondition.mostRestrictiveEvaluation = extremeLimitingEvaluation;
							restrictingConditionLimitingEvaluationInterval.sufficientlyRestrictiveEvaluation = extremeLimitingEvaluation;
							restrictingConditionLimitingEvaluationInterval.insufficientlyRestrictiveEvaluation = null;
							break;
						}
						
						//at this point, least/most restrictive limiting evaluation among considered objects, for current criterion, has been calculated, so one can construct candidate condition
						candidateCondition = constructCondition(ruleConditions.getRuleType(), ruleConditions.getRuleSemantics(),
								activeConditionAttributes[localActiveConditionAttributeIndex], extremeLimitingEvaluation, globalAttributeIndex); //construct extreme condition
						candidateConditionWithEvaluations.setCondition(candidateCondition); //reset candidate condition with evaluations
						
						//compare candidate and best condition w.r.t. the first evaluator only
						candidateVSBestConditionComparisonResult = compareCandidateAndBestCondition(candidateConditionWithEvaluations, bestConditionWithEvaluations, 1);
						//candidateVSBestCondition = setBestCondition(ruleConditions, candidateCondition, bestConditionWithEvaluations, 1); //if result is > 0, then best condition was already updated inside the method

						checkLessExtremeEvaluations = true;
						switch (candidateVSBestConditionComparisonResult) {
						case CANDIDATE_CONDITION_IS_BETTER: //candidate condition is better already w.r.t. the first evaluator
							bestConditionWithEvaluations.copy(candidateConditionWithEvaluations); //update best condition
							checkLessExtremeEvaluations = containsEvaluatorsOfDifferentMonotonicityType ? true : false;
							break;
						case CANDIDATE_CONDITION_IS_EQUAL:
							candidateVSBestConditionComparisonResult = compareCandidateAndBestCondition(candidateConditionWithEvaluations, bestConditionWithEvaluations,
									conditionAdditionEvaluators.length); //compare conditions w.r.t. all evaluators (using already stored evaluations for the first evaluator)
							if (candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER) {
								bestConditionWithEvaluations.copy(candidateConditionWithEvaluations); //update best condition
							}
							checkLessExtremeEvaluations = containsEvaluatorsOfDifferentMonotonicityType ? true : false;
							break;
						case CANDIDATE_CONDITION_IS_WORSE: //candidate condition is worse already w.r.t. the first evaluator
							checkLessExtremeEvaluations = false; //go to next active condition attribute as for the current attribute best condition cannot be improved
							break;
						}
						
						if (checkLessExtremeEvaluations) {
							for (int consideredObjectIndex : consideredObjects) {
								objectEvaluation = data.getField(consideredObjectIndex, localActiveConditionAttributeIndex);
								if (objectEvaluation instanceof KnownSimpleField) { //non-missing evaluation found
									candidateLimitingEvaluation = (KnownSimpleField)objectEvaluation;
									//check if current evaluation is strictly inside current range of interest
									switch (firstEvaluatorMonotonicityType) {
									case IMPROVES_WITH_NUMBER_OF_COVERED_OBJECTS:
										if (generalizingConditionLimitingEvaluationInterval.includes(candidateLimitingEvaluation, compareToMultiplier)) {
											candidateCondition = constructCondition(ruleConditions.getRuleType(), ruleConditions.getRuleSemantics(),
													activeConditionAttributes[localActiveConditionAttributeIndex], candidateLimitingEvaluation, globalAttributeIndex);
											candidateConditionWithEvaluations.setCondition(candidateCondition); //reset candidate condition with evaluations
											candidateVSBestConditionComparisonResult = compareCandidateAndBestCondition(candidateConditionWithEvaluations, bestConditionWithEvaluations,
													conditionAdditionEvaluators.length); //compare conditions w.r.t. all evaluators (using already stored evaluations for the first evaluator)
											
											if (candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER) {
												bestConditionWithEvaluations.copy(candidateConditionWithEvaluations); //update best condition
											}
											
											if (candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER ||
													candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_EQUAL) {
												generalizingConditionLimitingEvaluationInterval.sufficientlyGeneralEvaluation = candidateLimitingEvaluation;
											} else {
												generalizingConditionLimitingEvaluationInterval.insufficientlyGeneralEvaluation = candidateLimitingEvaluation;
											}
										}
										break;
									case DETERIORATES_WITH_NUMBER_OF_COVERED_OBJECTS:
										if (restrictingConditionLimitingEvaluationInterval.includes(candidateLimitingEvaluation, compareToMultiplier)) {
											candidateCondition = constructCondition(ruleConditions.getRuleType(), ruleConditions.getRuleSemantics(),
													activeConditionAttributes[localActiveConditionAttributeIndex], candidateLimitingEvaluation, globalAttributeIndex);
											candidateConditionWithEvaluations.setCondition(candidateCondition); //reset candidate condition with evaluations
											candidateVSBestConditionComparisonResult = compareCandidateAndBestCondition(candidateConditionWithEvaluations, bestConditionWithEvaluations,
													conditionAdditionEvaluators.length); //compare conditions w.r.t. all evaluators (using already stored evaluations for the first evaluator)
											
											if (candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER) {
												bestConditionWithEvaluations.copy(candidateConditionWithEvaluations); //update best condition
											}
											
											if (candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER ||
													candidateVSBestConditionComparisonResult == ConditionComparisonResult.CANDIDATE_CONDITION_IS_EQUAL) {
												restrictingConditionLimitingEvaluationInterval.sufficientlyRestrictiveEvaluation = candidateLimitingEvaluation;
											} else {
												restrictingConditionLimitingEvaluationInterval.insufficientlyRestrictiveEvaluation = candidateLimitingEvaluation;
											}
										}
										break;
									}
								} //if
							} //for
						} else {
							//continue; //go to next active condition attribute as for the current attribute best condition cannot be improved
						}
					} else {
						//continue; //go to next active condition attribute as for the current attribute all considered objects miss an evaluation (have missing value)
					}
				} else { //proceed without optimization
					for (int consideredObjectIndex : consideredObjects) {
						objectEvaluation = data.getField(consideredObjectIndex, localActiveConditionAttributeIndex);
						//TODO: iterate through considered objects, without any optimization (check all conditions)
					}
					//TODO: skip evaluation if missing
					//TODO: handle PairField evaluations (decomposition!)
				}
			} //if
		} //for
		
		return bestConditionWithEvaluations.condition;
	}
	
	/**
	 * Compares candidate versus best condition found so far to establish which one of the two is better.
	 * Lexicographically employs considered condition addition evaluators.
	 * 
	 * @param candidateConditionWithEvaluations candidate condition (with its already calculated evaluations), to be compared with currently best condition
	 * @param bestConditionWithEvaluations currently best condition (with its already calculated evaluations)
	 * @param usedEvaluatorsCount number of consecutive condition addition evaluators used in the comparison; has to be {@code >= 0} and {@code <= conditionAdditionEvaluators.length}
	 * 
	 * @return {@link ConditionComparisonResult#CANDIDATE_CONDITION_IS_BETTER} if given candidate condition is better than given best condition,
	 *         {@link ConditionComparisonResult#CANDIDATE_CONDITION_IS_WORSE} if given candidate condition is worse than given best condition,
	 *         {@link ConditionComparisonResult#CANDIDATE_CONDITION_IS_EQUAL} otherwise
	 */
	ConditionComparisonResult compareCandidateAndBestCondition(ConditionWithEvaluations candidateConditionWithEvaluations, ConditionWithEvaluations bestConditionWithEvaluations, int usedEvaluatorsCount) {
		double candidateConditionEvaluation;
		double bestConditionEvaluation;
		
		for (int i = 0; i < usedEvaluatorsCount; i++) {
			candidateConditionEvaluation = candidateConditionWithEvaluations.getEvaluation(i);
			bestConditionEvaluation = bestConditionWithEvaluations.getEvaluation(i);
			
			if (candidateConditionEvaluation > bestConditionEvaluation) {
				if (conditionAdditionEvaluators[i].getType() == MeasureType.GAIN) { //candidate condition is better at i-th evaluation
					return ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER;
				} else { //COST
					return ConditionComparisonResult.CANDIDATE_CONDITION_IS_WORSE;
				}
			}
			
			if (candidateConditionEvaluation < bestConditionWithEvaluations.evaluations[i]) {
				if (conditionAdditionEvaluators[i].getType() == MeasureType.GAIN) { //candidate condition is worse at i-th evaluation
					return ConditionComparisonResult.CANDIDATE_CONDITION_IS_WORSE;
				} else { //COST
					return ConditionComparisonResult.CANDIDATE_CONDITION_IS_BETTER;
				}
			}
		} //for
		
		return ConditionComparisonResult.CANDIDATE_CONDITION_IS_EQUAL; //neither condition is better
	}
	
	Condition<EvaluationField> constructCondition(RuleType ruleType, RuleSemantics ruleSemantics, EvaluationAttribute evaluationAttribute, 
			EvaluationField limitingEvaluation, int globalAttributeIndex) {
		switch (ruleType) {
		case CERTAIN:
			return constructCertainRuleCondition(ruleSemantics, evaluationAttribute, limitingEvaluation, globalAttributeIndex);
		case POSSIBLE:
			constructPossibleRuleCondition(ruleSemantics, evaluationAttribute, limitingEvaluation, globalAttributeIndex);
		default:
			throw new InvalidValueException("Cannot construct condition if rule type is neither certain nor possible.");
		}
	}
	
	Condition<EvaluationField> constructCertainRuleCondition(RuleSemantics ruleSemantics, EvaluationAttribute evaluationAttribute, EvaluationField limitingEvaluation, int globalAttributeIndex) {
		switch (ruleSemantics) {
		case AT_LEAST:
			switch (evaluationAttribute.getPreferenceType()) {
			case GAIN:
				return new ConditionAtLeastThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case COST:
				return new ConditionAtMostThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case NONE:
				return new ConditionEqualThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			default:
				throw new NullPointerException("Attribute preference type is null.");
			}
		case AT_MOST:
			switch (evaluationAttribute.getPreferenceType()) {
			case GAIN:
				return new ConditionAtMostThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case COST:
				return new ConditionAtLeastThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case NONE:
				return new ConditionEqualThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			default:
				throw new NullPointerException("Attribute preference type is null.");
			}
		case EQUAL:
			return new ConditionEqualThresholdVSObject<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
		default:
			throw new NullPointerException("Rule semantics is null.");
		}
	}
	
	Condition<EvaluationField> constructPossibleRuleCondition(RuleSemantics ruleSemantics, EvaluationAttribute evaluationAttribute, EvaluationField limitingEvaluation, int globalAttributeIndex) {
		switch (ruleSemantics) {
		case AT_LEAST:
			switch (evaluationAttribute.getPreferenceType()) {
			case GAIN:
				return new ConditionAtLeastObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case COST:
				return new ConditionAtMostObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case NONE:
				return new ConditionEqualObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			default:
				throw new NullPointerException("Attribute preference type is null.");
			}
		case AT_MOST:
			switch (evaluationAttribute.getPreferenceType()) {
			case GAIN:
				return new ConditionAtMostObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case COST:
				return new ConditionAtLeastObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			case NONE:
				return new ConditionEqualObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
			default:
				throw new NullPointerException("Attribute preference type is null.");
			}
		case EQUAL:
			return new ConditionEqualObjectVSThreshold<EvaluationField>(new EvaluationAttributeWithContext(evaluationAttribute, globalAttributeIndex), limitingEvaluation);
		default:
			throw new NullPointerException("Rule semantics is null.");
		}
	}
	
}
