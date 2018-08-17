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

package org.rulelearn.measures.dominance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rulelearn.approximations.Union;
import org.rulelearn.approximations.Union.UnionType;
import org.rulelearn.data.Decision;
import org.rulelearn.data.DecisionDistribution;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.dominance.DominanceConesDecisionDistributions;
import org.rulelearn.measures.ConsistencyMeasureType;

/**
 * Tests for {@link EpsilonConsistencyMeasure}.
 *
 * @author Jerzy Błaszczyński <jurek.blaszczynski@cs.put.poznan.pl>
 * @author Marcin Szeląg <marcin.szelag@cs.put.poznan.pl>
 */
class EpsilonConsistencyMeasureTest {

	private EpsilonConsistencyMeasure measure;
	
	@Mock
	private InformationTableWithDecisionDistributions informationTableMock;
	@Mock
	private Decision class1, class2, class3;
	@Mock
	private Union unionAtLeast2Mock, unionAtLeast3Mock, unionAtMost1Mock;
	@Mock
	private DominanceConesDecisionDistributions dominanceCDDMock;
	@Mock
	private DecisionDistribution coneDecisionClassDistribution3, coneDecisionClassDistribution4, coneDecisionClassDistribution5;
	@Mock
	private Set<Decision> decisionClassDistributionSet3, decisionClassDistributionSet4, decisionClassDistributionSet5;
	@Mock
	private Iterator<Decision> decisionClassDistributionIterator3, decisionClassDistributionIterator4, decisionClassDistributionIterator5;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		this.measure = new EpsilonConsistencyMeasure();
		// mock unions
		when(this.unionAtLeast2Mock.getUnionType()).thenReturn(UnionType.AT_LEAST);
		when(this.unionAtLeast3Mock.getUnionType()).thenReturn(UnionType.AT_LEAST);
		when(this.unionAtMost1Mock.getUnionType()).thenReturn(UnionType.AT_MOST);
		when(this.unionAtLeast2Mock.getComplementarySetSize()).thenReturn(1);
		when(this.unionAtLeast3Mock.getComplementarySetSize()).thenReturn(2);
		when(this.unionAtMost1Mock.getComplementarySetSize()).thenReturn(7);
		when(this.unionAtLeast2Mock.isDecisionNegative(class1)).thenReturn(true);
		when(this.unionAtLeast2Mock.isDecisionNegative(class2)).thenReturn(false);
		when(this.unionAtLeast2Mock.isDecisionNegative(class3)).thenReturn(false);
		when(this.unionAtLeast3Mock.isDecisionNegative(class1)).thenReturn(true);
		when(this.unionAtLeast3Mock.isDecisionNegative(class2)).thenReturn(true);
		when(this.unionAtLeast3Mock.isDecisionNegative(class3)).thenReturn(false);
		when(this.unionAtMost1Mock.isDecisionNegative(class1)).thenReturn(false);
		when(this.unionAtMost1Mock.isDecisionNegative(class2)).thenReturn(true);
		when(this.unionAtMost1Mock.isDecisionNegative(class3)).thenReturn(true);
		// mock information table
		when(this.unionAtLeast2Mock.getInformationTable()).thenReturn(this.informationTableMock);
		when(this.unionAtLeast3Mock.getInformationTable()).thenReturn(this.informationTableMock);
		when(this.unionAtMost1Mock.getInformationTable()).thenReturn(this.informationTableMock);
		when(this.informationTableMock.getDominanceConesDecisionDistributions()).thenReturn(this.dominanceCDDMock);
		// mock class distribution in positive inverted dominance cone based on inconsistent object 3 for union at least 3
		when(this.dominanceCDDMock.getPositiveInvDConeDecisionClassDistribution(2)).thenReturn(coneDecisionClassDistribution3);
		when(this.coneDecisionClassDistribution3.getDecisions()).thenReturn(this.decisionClassDistributionSet3);
		when(this.decisionClassDistributionSet3.iterator()).thenReturn(decisionClassDistributionIterator3);
		when(this.decisionClassDistributionIterator3.hasNext()).thenReturn(true, true, true, false);
		when(this.decisionClassDistributionIterator3.next()).thenReturn(class1, class2, class3);
		when(this.coneDecisionClassDistribution3.getCount(class1)).thenReturn(1);
		when(this.coneDecisionClassDistribution3.getCount(class2)).thenReturn(1);
		when(this.coneDecisionClassDistribution3.getCount(class3)).thenReturn(4);
		// mock class distribution in positive inverted dominance cone based on consistent object 4 for union at least 2
		when(this.dominanceCDDMock.getPositiveInvDConeDecisionClassDistribution(3)).thenReturn(coneDecisionClassDistribution4);
		when(this.coneDecisionClassDistribution4.getDecisions()).thenReturn(this.decisionClassDistributionSet4);
		when(this.decisionClassDistributionSet4.iterator()).thenReturn(decisionClassDistributionIterator4);
		when(this.decisionClassDistributionIterator4.hasNext()).thenReturn(true, true, true, false);
		when(this.decisionClassDistributionIterator4.next()).thenReturn(class1, class2, class3);
		when(this.coneDecisionClassDistribution4.getCount(class1)).thenReturn(1);
		when(this.coneDecisionClassDistribution4.getCount(class2)).thenReturn(1);
		when(this.coneDecisionClassDistribution4.getCount(class3)).thenReturn(3);
		// mock class distribution in negative dominance cone based on inconsistent object 5 for union at most 1 
		when(this.dominanceCDDMock.getNegativeDConeDecisionClassDistribution(4)).thenReturn(coneDecisionClassDistribution5);
		when(this.coneDecisionClassDistribution5.getDecisions()).thenReturn(this.decisionClassDistributionSet5);
		when(this.decisionClassDistributionSet5.iterator()).thenReturn(decisionClassDistributionIterator5);
		when(this.decisionClassDistributionIterator5.hasNext()).thenReturn(true, true, true, false);
		when(this.decisionClassDistributionIterator5.next()).thenReturn(class1, class2, class3);
		when(this.coneDecisionClassDistribution5.getCount(class1)).thenReturn(1);
		when(this.coneDecisionClassDistribution5.getCount(class2)).thenReturn(1);
		when(this.coneDecisionClassDistribution5.getCount(class3)).thenReturn(1);
	}

	/**
	 * Test for method {@link org.rulelearn.measures.dominance.EpsilonConsistencyMeasure#getType()}.
	 */
	@Test
	void testGetType() {
		assertEquals(ConsistencyMeasureType.COST, this.measure.getType());
	}

	/**
	 * Test for method {@link org.rulelearn.measures.dominance.EpsilonConsistencyMeasure#calculateConsistency(int, Union)}.
	 */
	@Test
	void testEpsilonOnObject3withRespectToUnionAtLeast3() {
		assertEquals(1.0, this.measure.calculateConsistency(2, this.unionAtLeast3Mock));
	}
	
	/**
	 * Test for method {@link org.rulelearn.measures.dominance.EpsilonConsistencyMeasure#calculateConsistency(int, Union)}.
	 */
	@Test
	void testEpsilonOnObject4withRespectToUnionAtLeast2() {
		assertEquals(1.0, this.measure.calculateConsistency(3, this.unionAtLeast2Mock));
	}
	
	/**
	 * Test for method {@link org.rulelearn.measures.dominance.EpsilonConsistencyMeasure#calculateConsistency(int, Union)}.
	 */
	@Test
	void testEpsilonOnObject5withRespectToUnionAtMost1() {
		assertEquals(((double)2)/7, this.measure.calculateConsistency(4, this.unionAtMost1Mock));
	}
}
