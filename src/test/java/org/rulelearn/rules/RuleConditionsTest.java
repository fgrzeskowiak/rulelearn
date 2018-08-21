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
import org.mockito.Mockito;
import org.rulelearn.data.AttributePreferenceType;
import org.rulelearn.data.EvaluationAttributeWithContext;
import org.rulelearn.data.InformationTable;
import org.rulelearn.types.IntegerFieldFactory;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Tests for {@link RuleConditions}.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
class RuleConditionsTest {

	/**
	 * Test method for {@link RuleConditions#RuleConditions(InformationTable, IntSet)}.
	 */
	@Test
	void testRuleConditions01() {
		try {
			new RuleConditions(null, Mockito.mock(IntSet.class));
			fail("Should not construct rule conditions for null information table.");
		} catch (NullPointerException exception) {
			//exception is correctly thrown => do nothing
		}
	}
	
	/**
	 * Test method for {@link RuleConditions#RuleConditions(InformationTable, IntSet)}.
	 */
	@Test
	void testRuleConditions02() {
		try {
			new RuleConditions(Mockito.mock(InformationTable.class), null);
			fail("Should not construct rule conditions for null indices of positive objects.");
		} catch (NullPointerException exception) {
			//exception is correctly thrown => do nothing
		}
	}
	
	/**
	 * Test method for {@link RuleConditions#RuleConditions(InformationTable, IntSet)},
	 * {@link RuleConditions#getIndicesOfPositiveObjects(), and {@link RuleConditions#getLearningInformationTable()}.
	 */
	@Test
	void testRuleConditions03() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		assertEquals(ruleConditions.getLearningInformationTable(), informationTable);
		assertEquals(ruleConditions.getIndicesOfPositiveObjects(), indicesOfPositiveObjects);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#objectIsPositive(int)}.
	 */
	@Test
	void testObjectIsPositive() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = new IntOpenHashSet();
		indicesOfPositiveObjects.add(3);
		indicesOfPositiveObjects.add(5);
		indicesOfPositiveObjects.add(6);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		assertFalse(ruleConditions.objectIsPositive(0));
		assertTrue(ruleConditions.objectIsPositive(3));
		assertTrue(ruleConditions.objectIsPositive(5));
		assertTrue(ruleConditions.objectIsPositive(6));
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#addCondition(org.rulelearn.rules.Condition)}.
	 */
	@Test
	void testAddCondition01() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		try {
			ruleConditions.addCondition(null);
			fail("Should not add null condition to rule conditions.");
		} catch (NullPointerException exception) {
			//do nothing - exception is correctly thrown
		}
	}

	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#addCondition(org.rulelearn.rules.Condition)}.
	 */
	@Test
	void testAddCondition02() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		assertEquals(ruleConditions.addCondition(Mockito.mock(Condition.class)), 0);
		assertEquals(ruleConditions.getConditions().size(), 1);
		
		assertEquals(ruleConditions.addCondition(Mockito.mock(Condition.class)), 1);
		assertEquals(ruleConditions.getConditions().size(), 2);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#removeCondition(int)}.
	 */
	@Test
	void testRemoveCondition01() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		try {
			ruleConditions.removeCondition(0);
			fail("Should not remove a non-existing condition.");
		} catch (IndexOutOfBoundsException exception) {
			//do nothing - exception is correctly thrown
		}
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#removeCondition(int)}.
	 */
	@Test
	void testRemoveCondition02() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		SimpleConditionAtLeast condition0 = Mockito.mock(SimpleConditionAtLeast.class);
		SimpleConditionAtMost condition1 = Mockito.mock(SimpleConditionAtMost.class);
		SimpleConditionEqual condition2 = Mockito.mock(SimpleConditionEqual.class);
		
		ruleConditions.addCondition(condition0);
		ruleConditions.addCondition(condition1);
		ruleConditions.addCondition(condition2);
		
		assertEquals(ruleConditions.size(), 3);
		
		ruleConditions.removeCondition(1);
		
		assertEquals(ruleConditions.size(), 2);
		assertEquals(ruleConditions.getCondition(0), condition0);
		assertEquals(ruleConditions.getCondition(1), condition2);
	}

	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#getConditions()}.
	 */
	@Test
	void testGetConditions01() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		assertTrue(ruleConditions.getConditions().isEmpty());
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#getConditions())}.
	 */
	@Test
	void testGetConditions02() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		SimpleConditionAtLeast condition0 = Mockito.mock(SimpleConditionAtLeast.class);
		SimpleConditionAtMost condition1 = Mockito.mock(SimpleConditionAtMost.class);
		SimpleConditionEqual condition2 = Mockito.mock(SimpleConditionEqual.class);
		
		ruleConditions.addCondition(condition0);
		ruleConditions.addCondition(condition1);
		ruleConditions.addCondition(condition2);
		
		List<Condition<?>> conditions = ruleConditions.getConditions();
		
		assertEquals(conditions.size(), 3);
		assertEquals(conditions.get(0), condition0);
		assertEquals(conditions.get(1), condition1);
		assertEquals(conditions.get(2), condition2);
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#getCondition(int)}.
	 */
	@Test
	void testGetCondition01() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		try {
			ruleConditions.getCondition(0);
			fail("Should not get a non-existing condition.");
		} catch (IndexOutOfBoundsException exception) {
			//do nothing - exception is correctly thrown
		}
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#getCondition(int)}.
	 */
	@Test
	void testGetCondition02() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		ruleConditions.addCondition(Mockito.mock(Condition.class));
		
		EvaluationAttributeWithContext attributeMock = Mockito.mock(EvaluationAttributeWithContext.class);
		Mockito.when(attributeMock.getAttributeIndex()).thenReturn(1);
		SimpleConditionAtLeast simpleCondition = new SimpleConditionAtLeast(attributeMock, IntegerFieldFactory.getInstance().create(5, AttributePreferenceType.COST));
		ruleConditions.addCondition(simpleCondition);
		
		assertEquals(ruleConditions.getCondition(1), simpleCondition);
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#size()}.
	 */
	@Test
	void testSize() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		ruleConditions.addCondition(Mockito.mock(Condition.class));
		ruleConditions.addCondition(Mockito.mock(Condition.class));
		ruleConditions.addCondition(Mockito.mock(Condition.class));
		
		assertEquals(ruleConditions.size(), 3);
	}
	
	/**
	 * Test method for {@link org.rulelearn.rules.RuleConditions#containsCondition()}.
	 */
	@Test
	void testContainsCondition() {
		InformationTable informationTable = Mockito.mock(InformationTable.class);
		IntSet indicesOfPositiveObjects = Mockito.mock(IntSet.class);
		
		RuleConditions ruleConditions = new RuleConditions(informationTable, indicesOfPositiveObjects);
		
		SimpleConditionAtLeast condition0 = Mockito.mock(SimpleConditionAtLeast.class);
		SimpleConditionAtMost condition1 = Mockito.mock(SimpleConditionAtMost.class);
		SimpleConditionEqual condition2 = Mockito.mock(SimpleConditionEqual.class);
		
		ruleConditions.addCondition(condition0);
		ruleConditions.addCondition(condition1);
		ruleConditions.addCondition(condition2);
		
		assertEquals(ruleConditions.size(), 3);
		
		assertTrue(ruleConditions.containsCondition(condition0));
		assertTrue(ruleConditions.containsCondition(condition1));
		assertTrue(ruleConditions.containsCondition(condition2));
		
		ruleConditions.removeCondition(1);
		
		assertTrue(ruleConditions.containsCondition(condition0));
		assertFalse(ruleConditions.containsCondition(condition1)); //!
		assertTrue(ruleConditions.containsCondition(condition2));
	}

}
