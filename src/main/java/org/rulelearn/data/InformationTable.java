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

package org.rulelearn.data;

import java.util.List;
import org.rulelearn.core.InvalidValueException;
import org.rulelearn.core.ReadOnlyArrayReference;
import org.rulelearn.core.ReadOnlyArrayReferenceLocation;
import org.rulelearn.types.EvaluationField;
import org.rulelearn.types.Field;
import org.rulelearn.types.IdentificationField;


/**
 * Information table composed of fields storing identifiers/evaluations of all considered objects on all specified attributes, among which we distinguish:<br>
 * (1) identification attributes,<br>
 * (2) evaluation attributes: condition, decision and description ones,<br>
 * both active and non-active.<br>
 * Each field is identified by object's index and attribute's index.
 * An information table is allowed to have zero or exactly one active decision attribute.
 * An object's evaluation on this attribute may, e.g., indicate decision class to which this object is assigned.
 * An information table is allowed to have zero or exactly one active identification attribute whose values are object identifiers.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class InformationTable {
	
	/**
	 * All attributes of this information table (regardless of their type and regardless of the fact if they are active or not).
	 */
	protected Attribute[] attributes;
	
	/**
	 * Mapper translating object's index into its globally unique id.
	 */
	protected Index2IdMapper mapper;
	
	/**
	 * Sub-table, corresponding to active condition attributes only.
	 * This sub-table is used in calculations. Equals to {@code null} if there are no active condition attributes.
	 */
	protected Table activeConditionFields = null;
	/**
	 * Sub-table, corresponding to all attributes which are either not active or description ones.
	 * This sub-table is not used in calculations. It stores values of such supplementary attributes
	 * mainly for the on-screen presentation of data and their write-back to file.
	 * Equals to {@code null} if there are no supplementary attributes.
	 */
	protected Table notActiveOrDescriptionFields = null;
	
	/**
	 * Contains evaluations of objects on the only active decision attribute.
	 * Can be {@code null}, e.g., if this information table stores evaluations of test objects (for which decisions are unknown).
	 */
	protected EvaluationField[] activeDecisionAttributeFields = null;
	
	/**
	 * Index of the only active decision attribute used in calculations. If there is no such attribute, equals to -1.
	 */
	protected int activeDecisionAttributeIndex = -1;
	
	/**
	 * Contains identifiers of objects assigned to them by the only active identification attribute.
	 * Can be {@code null}, if there is no such attribute.
	 */
	protected IdentificationField[] activeIdentificationAttributeFields = null;
	
	/**
	 * Index of the only active identification attribute used to identify objects. If there is no such attribute, equals to -1.
	 */
	protected int activeIdentificationAttributeIndex = -1;
	
	/**
	 * Maps global index of an attribute of this information table to encoded local index of an active condition attribute (called AC-attribute)
	 * or to encoded local index of a non-active/description attribute (called NA/D-attribute).
	 * Moreover, marks the only active decision attribute (called AD-attribute) and the only active identification attribute (called AI-attribute),
	 * if there are such attributes.
	 * Mapping of a global index of an AC-attribute or NA/D-attribute is done as follows. Suppose that the 3-rd global attribute (having global index 2) is the first AC-attribute (having local index 0).
	 * Then, {@code attributeMap[2]} encodes 0.<br>
	 * Encoding of a local index of an AC-attribute is done by adding 1.<br>
	 * Encoding of a local index of a NA/D-attribute is done by subtracting 1.<br>
	 * If attributeMap[i]==0, then the global attribute having global index i is an AD-attribute or an AI-attribute
	 * (one can verify which of the two cases is true by comparing i with {@link #activeDecisionAttributeIndex} and {@link #activeIdentificationAttributeIndex}).<br>
	 * <br>
	 * Suppose there are eight attributes:<br>
	 * - attr1: active {@link EvaluationAttribute} of type {@link AttributeType#CONDITION},<br>
	 * - attr2: active {@link EvaluationAttribute} of type {@link AttributeType#DESCRIPTION},<br>
	 * - attr3: non-active {@link EvaluationAttribute} of type {@link AttributeType#CONDITION},<br>
	 * - attr4: active {@link EvaluationAttribute} of type {@link AttributeType#CONDITION},<br>
	 * - attr5: active {@link EvaluationAttribute} of type {@link AttributeType#DECISION},<br>
	 * - attr6: non-active {@link EvaluationAttribute} of type {@link AttributeType#DECISION},<br>
	 * - attr7: non-active {@link EvaluationAttribute} of type {@link AttributeType#DESCRIPTION},<br>
	 * - attr8: active {@link IdentificationAttribute},<br>
	 * - attr9: non-active {@link IdentificationAttribute}.<br>
	 * <br>
	 * Then, the map should be the following:<br>
	 * attributeMap = [1, -1, -2, 2, 0, -3, -4, 0, -5].
	 */
	protected int[] attributeMap;
	
	/**
	 * Protected constructor for internal use only. Sets all data fields of this information table.
	 * 
	 * @param attributes all attributes of constructed information table (identification and evaluation (condition/decision/description) ones, both active and non-active)
	 * @param mapper translator of object's index, which is meaningful in this information table only,
	 *        to unique object's id, which is meaningful in general
	 * @param activeConditionEvaluations sub-table corresponding to active condition attributes
	 * @param notActiveOrDescriptionEvaluations sub-table corresponding to non-active/description attributes
	 * @param activeDecisionAttributeFields list of decisions concerning subsequent objects
	 * @param activeDecisionAttributeIndex index of the only active decision attribute used in calculations
	 * @param activeIdentificationAttributeFields list of identifiers of subsequent objects
	 * @param activeIdentificationAttributeIndex index of the only active identification attribute
	 * @param attributeMap see {@link #attributeMap}
	 * @param accelerateByReadOnlyParams tells if construction of this information table should be accelerated by assuming that the given references
	 *        to arrays are not going to be used outside this class
	 *        to modify that arrays (and thus, this object does not need to clone the arrays for internal use)
	 */
	protected InformationTable(Attribute[] attributes, Index2IdMapper mapper, Table activeConditionEvaluations, Table notActiveOrDescriptionEvaluations,
			EvaluationField[] activeDecisionAttributeFields, int activeDecisionAttributeIndex,
			IdentificationField[] activeIdentificationAttributeFields, int activeIdentificationAttributeIndex,
			int[] attributeMap, boolean accelerateByReadOnlyParams) {
		this.attributes = accelerateByReadOnlyParams ? attributes : attributes.clone();
		this.mapper = mapper;
		this.activeConditionFields = activeConditionEvaluations;
		this.notActiveOrDescriptionFields = notActiveOrDescriptionEvaluations;
		
		this.activeDecisionAttributeFields = accelerateByReadOnlyParams ? activeDecisionAttributeFields : activeDecisionAttributeFields.clone();
		this.activeDecisionAttributeIndex = activeDecisionAttributeIndex;
		
		this.activeIdentificationAttributeFields = accelerateByReadOnlyParams ? activeIdentificationAttributeFields : activeIdentificationAttributeFields.clone();
		this.activeIdentificationAttributeIndex = activeIdentificationAttributeIndex;
		
		this.attributeMap = accelerateByReadOnlyParams ? attributeMap : attributeMap.clone();
	}
	
	
	/**
	 * Information table constructor. Assumes that the type of fields in i-th column is compatible with the type of attribute at i-th position.
	 * 
	 * @param attributes all attributes of constructed information table (identification and evaluation (condition/decision/description) ones, both active and non-active)
	 * @param fields list of fields of subsequent objects; each array contains subsequent fields of a single object (row) in this information table;
	 *        it is assumed that each array is of the same length (i.e., the number of fields of each object is the same)
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidValueException if there is more than one active decision attribute
	 * @throws InvalidValueException if there is more than one active identification attribute
	 */
	public InformationTable(Attribute[] attributes, List<Field[]> fields) {
		this(attributes, fields, false);
	}
	
	/**
	 * Information table constructor. Assumes that the type of fields in i-th column is compatible with the type of attribute at i-th position.<br>
	 * <br>
	 * This constructor can be used in certain circumstances to accelerate information table construction (by not cloning arrays).
	 * 
	 * @param attributes all attributes of constructed information table (identification and evaluation (condition/decision/description) ones, both active and non-active)
	 * @param listOfFields list of fields of subsequent objects; each array contains subsequent fields of a single object (row) in this information table;
	 *        it is assumed that each array is of the same length (i.e., the number of fields of each object is the same)
	 * @param accelerateByReadOnlyParams tells if construction of this object should be accelerated by assuming that the given reference
	 *        to an array of attributes and references to arrays of fields present at the given list are not going to be used outside this class
	 *        to modify that arrays (and thus, this object does not need to clone the arrays for internal use)
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidValueException if the number of attributes and the number of fields corresponding to one object
	 *         (i.e., stored in a single array) do not match
	 * @throws InvalidValueException if there is more than one active decision attribute
	 * @throws InvalidValueException if there is more than one active identification attribute
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.INPUT)
	public InformationTable(Attribute[] attributes, List<Field[]> listOfFields, boolean accelerateByReadOnlyParams) {
		if (listOfFields.size() > 0 && attributes.length != listOfFields.get(0).length) {
			throw new InvalidValueException("The number of attributes and the number of objects' fields in an information table do not match.");
		}
		
		int numberOfActiveConditionAttributes = 0;
		int numberOfActiveDecisionAttributes = 0;
		int numberOfActiveIdentificationAttributes = 0;
		
		for (int i = 0; i < attributes.length; i++) { //scout attributes first
			if (isActiveConditionAttribute(attributes[i])) {
				numberOfActiveConditionAttributes++;
			} else if (isActiveDecisionAttribute(attributes[i])) {
				numberOfActiveDecisionAttributes++;
				if (numberOfActiveDecisionAttributes > 1) {
					throw new InvalidValueException("The number of active decision attributes is greater than 1.");
				}
			} else if (isActiveIdentificationAttribute(attributes[i])) {
				numberOfActiveIdentificationAttributes++;
				if (numberOfActiveIdentificationAttributes > 1) {
					throw new InvalidValueException("The number of active identification attributes is greater than 1.");
				}
			}
		}
		
		int numberOfNotActiveOrDescriptionAttributes = attributes.length - numberOfActiveConditionAttributes - numberOfActiveDecisionAttributes - numberOfActiveIdentificationAttributes;
		
		boolean hasActiveConditionAttributes = numberOfActiveConditionAttributes > 0;
		boolean hasActiveDecisionAttribute = numberOfActiveDecisionAttributes > 0;
		boolean hasActiveIdentificationAttribute = numberOfActiveIdentificationAttributes > 0;
		boolean hasNotActiveOrDescriptionAttributes = numberOfNotActiveOrDescriptionAttributes > 0;
		
		Attribute[] activeConditionAttributes = hasActiveConditionAttributes ? new Attribute[numberOfActiveConditionAttributes] : null;
		Attribute[] notActiveOrDescriptionAttributes = hasNotActiveOrDescriptionAttributes ? new Attribute[numberOfNotActiveOrDescriptionAttributes] : null;
		
		int activeConditionAttributeIndex = 0;
		int notActiveOrDescriptionAttributeIndex = 0;
		
		this.attributeMap = new int[attributes.length];
		
		//split attributes into two tables + mark active decision attribute (if there is one) and active identification attribute (if there is one)
		for (int i = 0; i < attributes.length; i++) {
			if (isActiveConditionAttribute(attributes[i])) {
				activeConditionAttributes[activeConditionAttributeIndex] = attributes[i];
				this.attributeMap[i] = this.encodeActiveConditionAttributeIndex(activeConditionAttributeIndex);
				activeConditionAttributeIndex++;
			} else if (isActiveDecisionAttribute(attributes[i])) {
				this.activeDecisionAttributeIndex = i;
				this.attributeMap[i] = 0; //no encoding
			} else if (isActiveIdentificationAttribute(attributes[i])) {
				this.activeIdentificationAttributeIndex = i;
				this.attributeMap[i] = 0; //no encoding
			}else { //not active or description attribute
				notActiveOrDescriptionAttributes[notActiveOrDescriptionAttributeIndex] = attributes[i];
				this.attributeMap[i] = this.encodeNotActiveOrDescriptionAttributeIndex(notActiveOrDescriptionAttributeIndex);
				notActiveOrDescriptionAttributeIndex++;
			}
		}
		
		EvaluationField[][] activeConditionFieldsArray = hasActiveConditionAttributes ? new EvaluationField[listOfFields.size()][] : null;
		EvaluationField[] activeDecisionAttributeFields = hasActiveDecisionAttribute ? new EvaluationField[listOfFields.size()] : null;
		IdentificationField[] activeIdentificationAttributeFields = hasActiveIdentificationAttribute ? new IdentificationField[listOfFields.size()] : null;
		Field[][] notActiveOrDescriptionFieldsArray = hasNotActiveOrDescriptionAttributes ? new Field[listOfFields.size()][] : null;
		
		EvaluationField[] activeConditionFields = null;
		EvaluationField activeDecisionField = null;
		IdentificationField activeIdentificationField = null;
		Field[] notActiveOrDescriptionFields = null;
		
		int rowIndex = 0;
		
		//split fields into two tables + collect decisions (if any) and identifiers (if any)
		for (Field[] fields : listOfFields) { //choose a row (single object)
			if (hasActiveConditionAttributes) {
				activeConditionFields = new EvaluationField[numberOfActiveConditionAttributes];
				activeConditionAttributeIndex = 0;
			}
			if (hasNotActiveOrDescriptionAttributes) {
				notActiveOrDescriptionFields = new Field[numberOfNotActiveOrDescriptionAttributes];
				notActiveOrDescriptionAttributeIndex = 0;
			}
			
			for (int i = 0; i < attributes.length; i++) { //choose column (single attribute)
				if (isActiveConditionAttribute(attributes[i])) {
					activeConditionFields[activeConditionAttributeIndex++] = (EvaluationField)fields[i];
				} else if (isActiveDecisionAttribute(attributes[i])) { //should be true at most once per row
					activeDecisionField = (EvaluationField)fields[i];
				} else if (isActiveIdentificationAttribute(attributes[i])) { //should be true at most once per row
					activeIdentificationField = (IdentificationField)fields[i];
				} else { //not active or description attribute
					notActiveOrDescriptionFields[notActiveOrDescriptionAttributeIndex++] = fields[i];
				} 
			}
			
			if (hasActiveConditionAttributes) {
				activeConditionFieldsArray[rowIndex] = activeConditionFields;
			}
			if (hasActiveDecisionAttribute) {
				activeDecisionAttributeFields[rowIndex] = activeDecisionField;
			}
			if (hasActiveIdentificationAttribute) {
				activeIdentificationAttributeFields[rowIndex] = activeIdentificationField;
			}
			if (hasNotActiveOrDescriptionAttributes) {
				notActiveOrDescriptionFieldsArray[rowIndex] = notActiveOrDescriptionFields;
			}
			
			rowIndex++;
		}
		
		this.attributes = accelerateByReadOnlyParams ? attributes : attributes.clone(); //remember all attributes
		//map each object (row of this information table) to a unique id, and remember that mapping
		this.mapper = new Index2IdMapper(UniqueIdGenerator.getInstance().getUniqueIds(listOfFields.size()), true);
		
		this.activeConditionFields = hasActiveConditionAttributes ? new Table(activeConditionAttributes, activeConditionFieldsArray, this.mapper, true) : null;
		this.activeDecisionAttributeFields = hasActiveDecisionAttribute ? activeDecisionAttributeFields : null;
		this.activeIdentificationAttributeFields = hasActiveIdentificationAttribute ? activeIdentificationAttributeFields : null;
		this.notActiveOrDescriptionFields = hasNotActiveOrDescriptionAttributes ? new Table(notActiveOrDescriptionAttributes, notActiveOrDescriptionFieldsArray, this.mapper, true) : null;
	}
	
	/**
	 * Gets sub-table of this information table, corresponding to active condition attributes only.
	 * If there are no such attributes, then returns {@code null}.
	 * 
	 * @return sub-table of this information table, corresponding to active condition attributes only
	 */
	public Table getActiveConditionFields() {
		return this.activeConditionFields;
	}

	/**
	 * Gets sub-table of this information table, corresponding to all attributes which are either not active or description ones.
	 * If there are no such attributes, then returns {@code null}.
	 * 
	 * @return sub-table of this information table, corresponding to all attributes which are either not active or description ones
	 */
	public Table getNotActiveOrDescriptionFields() {
		return this.notActiveOrDescriptionFields;
	}
	
	/**
	 * Gets array of decisions concerning subsequent objects of this information table; i-th entry stores decision concerning i-th object.
	 * This array stores evaluations of objects on the only active decision attribute.
	 * Can be {@code null}, e.g., if this information table stores evaluations of test objects (for which decisions are unknown).
	 * 
	 * @return array of decisions concerning subsequent objects of this information table
	 */
	public EvaluationField[] getDecisions() {
		return this.getDecisions(false);
	}
	
	/**
	 * Gets array of decisions concerning subsequent objects of this information table; i-th entry stores decision concerning i-th object.
	 * This array stores evaluations of objects on the only active decision attribute.
	 * Can be {@code null}, e.g., if this information table stores evaluations of test objects (for which decisions are unknown).
	 * 
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only array, or should return a safe array (that can be
	 *        modified outside this object), at the cost of returning the result slower
	 * @return array of decisions concerning subsequent objects of this information table
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT)
	public EvaluationField[] getDecisions(boolean accelerateByReadOnlyResult) {
		return accelerateByReadOnlyResult ? this.activeDecisionAttributeFields : this.activeDecisionAttributeFields.clone();
	}
	
	/**
	 * Gets decision available for an object with given index.
	 * 
	 * @param objectIndex index of an object in this information table
	 * @return decision available for an object with given index, or {@code null} if there is no such decision
	 * @throws IndexOutOfBoundsException if given object index does not correspond to any object for which this table stores fields
	 */
	public EvaluationField getDecision(int objectIndex) {
		if (this.activeDecisionAttributeFields != null) {
			return this.activeDecisionAttributeFields[objectIndex];
		} else {
			return null;
		}
	}
	
	/**
	 * Gets identifiers of objects assigned to them by the only active identification attribute.
	 * Can be {@code null}, if there is no active identification attribute.
	 * 
	 * @return array of identifiers of subsequent objects of this information table
	 */
	public IdentificationField[] getIdentifiers() {
		return this.getIdentifiers(false);
	}
	
	/**
	 * Gets identifiers of objects assigned to them by the only active identification attribute.
	 * Can be {@code null}, if there is no active identification attribute.
	 * 
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only array, or should return a safe array (that can be
	 *        modified outside this object), at the cost of returning the result slower
	 * @return array of identifiers of subsequent objects of this information table
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT)
	public IdentificationField[] getIdentifiers(boolean accelerateByReadOnlyResult) {
		return accelerateByReadOnlyResult ? this.activeIdentificationAttributeFields : this.activeIdentificationAttributeFields.clone();
	}
	
	/**
	 * Gets identifier available for an object with given index.
	 * 
	 * @param objectIndex index of an object in this information table
	 * @return identifier available for an object with given index, or {@code null} if there is no such identifier
	 * @throws IndexOutOfBoundsException if given object index does not correspond to any object for which this table stores fields
	 */
	public IdentificationField getIdentifier(int objectIndex) {
		if (this.activeIdentificationAttributeFields != null) {
			return this.activeIdentificationAttributeFields[objectIndex];
		} else {
			return null;
		}
	}

	/**
	 * Gets all attributes of this information table (regardless of their type and regardless of the fact if they are active or not).
	 *  
	 * @return array with all attributes of this information table
	 */
	public Attribute[] getAttributes() {
		return this.attributes.clone();
	}
	
	/**
	 * Gets all attributes of this information table (regardless of their type and regardless of the fact if they are active or not).<br>
	 * <br>
	 * This method can be used in certain circumstances to accelerate calculations.
	 *  
	 * @return array with all attributes of this information table
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only array, or should return a safe array (that can be
	 *        modified outside this object), at the cost of returning the result slower
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT)
	public Attribute[] getAttributes(boolean accelerateByReadOnlyResult) {
		return accelerateByReadOnlyResult ? this.attributes : this.attributes.clone();
	}
	
	/**
	 * Gets mapper that maps indices of objects stored in this information table to their globally unique ids.
	 * 
	 * @return mapper that maps indices of objects stored in this information table to their globally unique ids
	 */
	public Index2IdMapper getIndex2IdMapper() {
		return this.mapper;
	}
	
	/**
	 * Gets field of this information table for the object and attribute identified by the given indices.
	 * 
	 * @param objectIndex index of an object (row of the table)
	 * @param attributeIndex index of an attribute (column of the table)
	 * @return field of this information table corresponding to given indices
	 * 
	 * @throws IndexOutOfBoundsException if given object index does not correspond to any object for which this table stores fields
	 * @throws IndexOutOfBoundsException if given attribute index does not correspond to any attribute for which this table stores fields
	 */
	public Field getField(int objectIndex, int attributeIndex) {
		if (this.attributeMap[attributeIndex] > 0) { //active condition attribute
			return this.activeConditionFields.getField(objectIndex, this.decodeActiveConditionAttributeIndex(this.attributeMap[attributeIndex]));
		} else if (this.attributeMap[attributeIndex] == 0) { //active decision/identification attribute
			if (attributeIndex == this.activeDecisionAttributeIndex) {
				return this.activeDecisionAttributeFields[objectIndex];
			} else {
				return this.activeIdentificationAttributeFields[objectIndex];
			}
		} else { //not active or description attribute
			return this.notActiveOrDescriptionFields.getField(objectIndex, this.decodeNotActiveOrDescriptionAttributeIndex(this.attributeMap[attributeIndex]));
		}
	}
	
	private int encodeActiveConditionAttributeIndex(int index) {
		return index + 1;
	}
	
	private int decodeActiveConditionAttributeIndex(int encodedIndex) {
		return encodedIndex - 1;
	}
	
	private int encodeNotActiveOrDescriptionAttributeIndex(int index) {
		return -index - 1;
	}
	
	private int decodeNotActiveOrDescriptionAttributeIndex(int encodedIndex) {
		return -encodedIndex - 1;
	}
	
	/**
	 * Tells if given attribute is an active condition attribute.
	 * 
	 * @param attribute attribute to check
	 * @return {@code true} if given attribute is an active condition attribute, {@code false otherwise}
	 */
	private boolean isActiveConditionAttribute(Attribute attribute) {
		if (attribute instanceof EvaluationAttribute) {
			return ((EvaluationAttribute)attribute).getType() == AttributeType.CONDITION && attribute.isActive();
		}
		else {
			return false;
		}
	}
	
	/**
	 * Tells if given attribute is an active decision attribute.
	 * 
	 * @param attribute attribute to check
	 * @return {@code true} if given attribute is an active decision attribute, {@code false otherwise}
	 */
	private boolean isActiveDecisionAttribute(Attribute attribute) {
		if (attribute instanceof EvaluationAttribute) {
			return ((EvaluationAttribute)attribute).getType() == AttributeType.DECISION && attribute.isActive();
		}
		else {
			return false;
		}
	}
	
	/**
	 * Tells if given attribute is an active identification attribute.
	 * 
	 * @param attribute attribute to check
	 * @return {@code true} if given attribute is an active identification attribute, {@code false otherwise}
	 */
	private boolean isActiveIdentificationAttribute(Attribute attribute) {
		if (attribute instanceof IdentificationAttribute) {
			return attribute.isActive();
		}
		else {
			return false;
		}
	}
	
	/**
	 * Selects rows of this information table that correspond to objects with given indices.
	 * Returns new information table concerning a subset of objects (rows).
	 * 
	 * @param objectIndices indices of objects to select to new information table (indices can repeat)
	 * @return sub-table of this information table, containing only rows corresponding to objects whose index is in the given array
	 * 
	 * @throws NullPointerException if given array with object indices is {@code null}
	 * @throws IndexOutOfBoundsException if any of the given indices does not match the number of considered objects
	 */
	public InformationTable select(int[] objectIndices) {
		return select(objectIndices, false);
	}
	
	/**
	 * Selects rows of this information table that correspond to objects with given indices.
	 * Returns new information table concerning a subset of objects (rows).
	 * 
	 * @param objectIndices indices of objects to select to new information table (indices can repeat)
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only information table, or should return a safe information table (that can be modified),
	 *        at the cost of returning the result slower
	 * @return sub-table of this information table, containing only rows corresponding to objects whose index is in the given array
	 * 
	 * @throws NullPointerException if given array with object indices is {@code null}
	 * @throws IndexOutOfBoundsException if any of the given indices does not match the number of considered objects
	 */
	public InformationTable select(int[] objectIndices, boolean accelerateByReadOnlyResult) {
		Index2IdMapper newMapper = null;
		
		Table newActiveConditionEvaluations = null;
		if (this.activeConditionFields != null) {
			newActiveConditionEvaluations = this.activeConditionFields.select(objectIndices, accelerateByReadOnlyResult);
			newMapper = newActiveConditionEvaluations.getIndex2IdMapper(); //use already calculated mapper
		}
		
		Table newNotActiveOrDescriptionEvaluations = null;
		if (this.notActiveOrDescriptionFields != null) {
			newNotActiveOrDescriptionEvaluations = this.notActiveOrDescriptionFields.select(objectIndices, accelerateByReadOnlyResult);
			if (newMapper == null) {
				newMapper = newNotActiveOrDescriptionEvaluations.getIndex2IdMapper(); //use already calculated mapper
			}
		}
		
		EvaluationField[] newActiveDecisionAttributeFields = null;
		if (this.activeDecisionAttributeFields != null) {
			newActiveDecisionAttributeFields = new EvaluationField[objectIndices.length];
			for (int i = 0; i < objectIndices.length; i++) {
				newActiveDecisionAttributeFields[i] = this.activeDecisionAttributeFields[objectIndices[i]];
			}
		}
		
		IdentificationField[] newActiveIdentificationAttributeFields = null;
		if (this.activeIdentificationAttributeFields != null) {
			newActiveIdentificationAttributeFields = new IdentificationField[objectIndices.length];
			for (int i = 0; i < objectIndices.length; i++) {
				newActiveIdentificationAttributeFields[i] = this.activeIdentificationAttributeFields[objectIndices[i]];
			}
		}
		
		return new InformationTable(this.attributes, newMapper, newActiveConditionEvaluations, newNotActiveOrDescriptionEvaluations,
				newActiveDecisionAttributeFields, this.activeDecisionAttributeIndex, newActiveIdentificationAttributeFields, this.activeIdentificationAttributeIndex, this.attributeMap, accelerateByReadOnlyResult);
	}
	
	/**
	 * Gets number of objects stored in this information table.
	 * 
	 * @return number of objects stored in this information table
	 */
	public int getNumberOfObjects() {
		return this.activeConditionFields.getNumberOfObjects();
	}
	
	/**
	 * Gets number of attributes stored in this information table.
	 * 
	 * @return number of attributes stored in this information table
	 */
	public int getNumberOfAttributes() {
		return this.attributes.length;
	}

	/**
	 * Gets index of the only active decision attribute used in calculations. If there is no such attribute, returns -1.
	 * 
	 * @return index of the only active decision attribute used in calculations, or -1 if there is no such attribute
	 */
	public int getActiveDecisionAttributeIndex() {
		return this.activeDecisionAttributeIndex;
	}
	
	/**
	 * Gets index of the only active identification attribute. If there is no such attribute, returns -1.
	 * 
	 * @return index of the only active identification attribute, or -1 if there is no such attribute
	 */
	public int getActiveIdentificationAttributeIndex() {
		return this.activeIdentificationAttributeIndex;
	}
}
