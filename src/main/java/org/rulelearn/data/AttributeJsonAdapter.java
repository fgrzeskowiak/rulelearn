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

import org.rulelearn.types.ElementList;
import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.EnumerationFieldJson;
import org.rulelearn.types.Field;
import org.rulelearn.types.FieldJson;
import org.rulelearn.types.UnknownSimpleField;

import com.squareup.moshi.*;

/**
 * JSON adapter for {@link Attribute}. 
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 *
 */
public class AttributeJsonAdapter {
	
	@ToJson EnumerationFieldJson fieldToJson (EnumerationField field) {
		EnumerationFieldJson json = new EnumerationFieldJson();
		json.type = field.getClass().getName();
		ElementList list = field.getElementList();
		String [] domain = new String [list.getSize()];
		for (int i = 0; i < list.getSize(); i++) {
			domain[i] = list.getElement(i);
		}
		json.domain = domain;
		return json; 
	}
	
	@ToJson String fieldToJson (UnknownSimpleField field) {
		return field.getClass().getName();
	}
	
	@ToJson FieldJson fieldToJson (Field field) {
		if (field instanceof EnumerationField) 
			return fieldToJson((EnumerationField)field);
		else {
			FieldJson json = new FieldJson();
			json.type = field.getClass().getName();
			return json;
		}
	}
	
	@ToJson EnumerationFieldJson fieldToJson (FieldJson json) {
		EnumerationFieldJson ejson;
		if (json instanceof EnumerationFieldJson) {
			ejson = (EnumerationFieldJson)json;
		}
		else {
			ejson = new EnumerationFieldJson();
			ejson.type = json.type;
			ejson.domain = null;
		}
		return ejson;
	}
		
	@FromJson Field fieldFromJson (Field field) {
		return null;
	}
	
	@FromJson UnknownSimpleField fieldFromJson (UnknownSimpleField field) {
		return null;
	}
}
