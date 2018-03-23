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

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.rulelearn.data.AttributePreferenceType;
import org.rulelearn.data.AttributeType;
import org.rulelearn.data.EvaluationAttribute;
import org.rulelearn.data.EvaluationAttributeWithContext;
import org.rulelearn.types.EvaluationField;
import org.rulelearn.types.IntegerField;
import org.rulelearn.types.IntegerFieldFactory;
import org.rulelearn.types.RealField;
import org.rulelearn.types.RealFieldFactory;
import org.rulelearn.types.UnknownSimpleFieldMV15;
import org.rulelearn.types.UnknownSimpleFieldMV2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Tests for [@link Rule}.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
class RuleTest {
	
	private Rule getTestRule1() {
		List<Condition<? extends EvaluationField>> conditions = new ObjectArrayList<>();
		Condition<? extends EvaluationField> condition1 = 		
				new SimpleConditionAtLeast(
						new EvaluationAttributeWithContext(
								new EvaluationAttribute(
										"attr1",
										true,
										AttributeType.CONDITION,
										IntegerFieldFactory.getInstance().create(IntegerField.DEFAULT_VALUE, AttributePreferenceType.GAIN),
										new UnknownSimpleFieldMV2(),
										AttributePreferenceType.GAIN),
								1),
						IntegerFieldFactory.getInstance().create(3, AttributePreferenceType.GAIN));
		Condition<? extends EvaluationField> condition2 = 
				new SimpleConditionAtMost(
						new EvaluationAttributeWithContext(
								new EvaluationAttribute(
										"attr3",
										true,
										AttributeType.CONDITION,
										RealFieldFactory.getInstance().create(RealField.DEFAULT_VALUE, AttributePreferenceType.COST),
										new UnknownSimpleFieldMV15(),
										AttributePreferenceType.COST),
								3),
						RealFieldFactory.getInstance().create(-2, AttributePreferenceType.COST));
		conditions.add(condition1);
		conditions.add(condition2);
		
		SimpleConditionAtLeast decision = null;
		List<Condition<? extends EvaluationField>> decisions = new ObjectArrayList<>();
		decisions.add(decision);
		
		Rule rule = new Rule(RuleType.CERTAIN, RuleSemantics.AT_LEAST, IntegerFieldFactory.getInstance().create(0, AttributePreferenceType.GAIN), conditions, decisions);
		return rule;
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getType()}.
	 */
	@Test
	void testGetType() {
		SimpleConditionAtLeast condition = null;
		List<Condition<? extends EvaluationField>> conditions = new ObjectArrayList<>();
		conditions.add(condition);
		
		SimpleConditionAtLeast decision = null;
		List<Condition<? extends EvaluationField>> decisions = new ObjectArrayList<>();
		decisions.add(decision);
		
		Rule rule = new Rule(RuleType.CERTAIN, RuleSemantics.AT_LEAST, IntegerFieldFactory.getInstance().create(0, AttributePreferenceType.GAIN), conditions, decisions);
		assertEquals(rule.getType(), RuleType.CERTAIN);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getSemantics()}.
	 */
	@Test
	void testGetSemantics() {
		SimpleConditionAtLeast condition = null;
		List<Condition<? extends EvaluationField>> conditions = new ObjectArrayList<>();
		conditions.add(condition);
		
		SimpleConditionAtLeast decision = null;
		List<Condition<? extends EvaluationField>> decisions = new ObjectArrayList<>();
		decisions.add(decision);
		
		Rule rule = new Rule(RuleType.CERTAIN, RuleSemantics.AT_MOST, IntegerFieldFactory.getInstance().create(0, AttributePreferenceType.GAIN), conditions, decisions);
		assertEquals(rule.getSemantics(), RuleSemantics.AT_MOST);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getInherentDecision()}.
	 */
	@Test
	void testGetInherentDecision() {
		List<Condition<? extends EvaluationField>> conditions = new ObjectArrayList<>(); //empty list of conditions
		
		SimpleConditionAtLeast decision = null;
		List<Condition<? extends EvaluationField>> decisions = new ObjectArrayList<>();
		decisions.add(decision);
		
		Rule rule = new Rule(RuleType.CERTAIN, RuleSemantics.AT_MOST, IntegerFieldFactory.getInstance().create(1, AttributePreferenceType.COST), conditions, decisions);
		assertEquals(rule.getInherentDecision(), IntegerFieldFactory.getInstance().create(1, AttributePreferenceType.COST));
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getConditions()}.
	 */
	@Test
	void testGetConditions() {
		Rule rule = getTestRule1();
		assertEquals(rule.getSemantics(), RuleSemantics.AT_LEAST);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getConditions(boolean)}.
	 */
	@Test
	void testGetConditionsBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getDecisions()}.
	 */
	@Test
	void testGetDecisions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getDecisions(boolean)}.
	 */
	@Test
	void testGetDecisionsBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#getDecision()}.
	 */
	@Test
	void testGetDecision() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#toString()}.
	 */
	@Test
	void testToString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#covers(int, org.rulelearn.data.InformationTable)}.
	 */
	@Test
	void testCovers() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#decisionsMatchedBy(int, org.rulelearn.data.InformationTable)}.
	 */
	@Test
	void testDecisionsMatchedBy() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.rulelearn.rules.Rule#supportedBy(int, org.rulelearn.data.InformationTable)}.
	 */
	@Test
	void testSupportedBy() {
		fail("Not yet implemented");
	}

}
