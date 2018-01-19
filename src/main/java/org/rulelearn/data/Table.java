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

import java.util.ArrayList;
import java.util.List;
import org.rulelearn.core.ReadOnlyArrayReference;
import org.rulelearn.core.ReadOnlyArrayReferenceLocation;
import org.rulelearn.types.Field;

/**
 * Table storing data, i.e., fields corresponding to objects and attributes.
 * Each field is identified by object's index and attribute's index. 
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class Table {
	
	/**
	 * All attributes for which this table stores values.
	 */
	protected Attribute[] attributes;
	/**
	 * All fields stored in this table, indexed by object's index and attribute's index.
	 */
	protected Field[][] fields;
	/**
	 *  Mapper from object's index to its unique id.
	 */
	protected Index2IdMapper mapper;
	
	/**
	 * Constructs this table. It is assumed that the number of attributes is equal to the number of fields
	 * for each object, and, moreover, that type of a field corresponding to i-th attribute
	 * is the same as the type of field returned by {@link Attribute#getValueType()}.
	 * 
	 * @param attributes attributes corresponding two columns of this table
	 * @param fields fields corresponding two rows of this table
	 * @param mapper translator of object's index (being meaningful in this table only)
	 *        to unique object's id (being meaningful in general)
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	public Table(Attribute[] attributes, List<Field[]> fields, Index2IdMapper mapper) {
		this(attributes, fields, mapper, false);
	}
	
	/**
	 * Constructs this table. It is assumed that the number of attributes is equal to the number of fields
	 * for each object, and, moreover, that type of a field corresponding to i-th attribute
	 * is the same as the type of field returned by {@link Attribute#getValueType()}.<br>
	 * <br>
	 * This constructor can be used in certain circumstances to accelerate object construction.
	 * 
	 * @param attributes attributes corresponding two columns of this table
	 * @param fields fields corresponding two rows of this table
	 * @param mapper translator of object's index, which is meaningful in this table only,
	 *        to unique object's id, which is meaningful in general
	 * @param accelerateByReadOnlyParams tells if construction of this object should be accelerated by assuming that the given reference
	 *        to an array of attributes and references to arrays of fields present at the given list are not going to be used outside this class
	 *        to modify that arrays (and thus, this object does not need to clone the arrays for internal use)
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.INPUT)
	public Table(Attribute[] attributes, List<Field[]> fields, Index2IdMapper mapper, boolean accelerateByReadOnlyParams) {
		if (attributes == null) {
			throw new NullPointerException("Attributes are null.");	
		}
		
		this.attributes = accelerateByReadOnlyParams ? attributes : attributes.clone();
		this.fields = new Field[fields.size()][];
		
		if (accelerateByReadOnlyParams) {
			for (int i = 0; i < this.fields.length; i++) {
				this.fields[i] = fields.get(i);
			}
		} else {
			for (int i = 0; i < this.fields.length; i++) {
				this.fields[i] = fields.get(i).clone();
			}
		}
		
		if (mapper == null) {
			throw new NullPointerException("Mapper is null.");	
		}
		this.mapper = mapper;
	}
	
	/**
	 * Gets field of this table for the object and attribute identified by the given indices
	 * 
	 * @param objectIndex index of an object (row of the table)
	 * @param attributeIndex index of an attribute (column of the table)
	 * @return field of this table corresponding to given indices
	 */
	public Field getField(int objectIndex, int attributeIndex) {
		return this.fields[objectIndex][attributeIndex];
	}
		
	/**
	 * Gets fields of this table for the object identified by the given index
	 * 
	 * @param objectIndex index of an object (row of the table)
	 * @return fields of this table corresponding to given index
	 */
	public Field[] getFields(int objectIndex) {
		return this.fields[objectIndex].clone();
	}
	
	/**
	 * Gets fields of this table for the object identified by the given index
	 * 
	 * @param objectIndex index of an object (row of the table)
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only array, or should return a safe array (that can be
	 *        modified outside this object), at the cost of returning the result slower
	 * @return fields of this table corresponding to given index
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT)
	public Field[] getFields(int objectIndex, boolean accelerateByReadOnlyResult) {
		return accelerateByReadOnlyResult ? this.fields[objectIndex] : this.fields[objectIndex].clone();
	}
	
	/**
	 * Selects rows of this table that correspond to objects with given indices.
	 * Returns new table concerning a subset of objects (rows).
	 *  
	 * @param objectIndices indices of objects to select to new information table (indices can repeat)
	 * @return sub-table of this table, containing only rows corresponding to objects whose index is on the given list
	 * 
	 * @throws NullPointerException if given array with object indices is {@code null}
	 * @throws IndexOutOfBoundsException if any of the given indices does not match the number of considered objects 
	 */
	public Table select(int[] objectIndices) {
		int[] newObjectIndex2Id = new int[objectIndices.length]; //data for new mapper
		List<Field[]> newFields = new ArrayList<Field[]>();
		
		for (int i = 0; i < objectIndices.length; i++) {
			newFields.add(this.fields[objectIndices[i]]); //just copy reference to an array with fields
			newObjectIndex2Id[i] = this.mapper.getId(objectIndices[i]);
		}
		
		return new Table(this.attributes, newFields, new Index2IdMapper(newObjectIndex2Id));
	}
	
	/**
	 * Gets number of objects stored in this table.
	 * 
	 * @return number of objects stored in this table
	 */
	public int getNumberOfObjects() {
		return this.fields.length;
	}
	
	/**
	 * Gets number of attributes stored in this table.
	 * 
	 * @return number of attributes stored in this table
	 */
	public int getNumberOfAttributes() {
		return this.attributes.length;
	}
	
	/**
	 * Gets attributes for which this table stores values.
	 * 
	 * @return attributes for which this table stores values
	 */
	public Attribute[] getAttributes () {
		return this.attributes.clone();
	}
	
	/**
	 * Gets attributes for which this table stores values.
	 *  
	 * @return array with attributes for which this table stores values
	 * @param accelerateByReadOnlyResult tells if this method should return the result faster,
	 *        at the cost of returning a read-only array, or should return a safe array (that can be
	 *        modified outside this object), at the cost of returning the result slower
	 */
	@ReadOnlyArrayReference(at = ReadOnlyArrayReferenceLocation.OUTPUT)
	public Attribute[] getAttributes(boolean accelerateByReadOnlyResult) {
		return accelerateByReadOnlyResult ? this.attributes : this.attributes.clone();
	}	
	
	/**
	 * Gets mapper that maps indices of objects stored in this table to their unique ids.
	 * 
	 * @return mapper that maps indices of objects stored in this table to their unique ids
	 */
	public Index2IdMapper getIndex2IdMapper() {
		return this.mapper;
	}
	
}
