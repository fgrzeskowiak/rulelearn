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

package org.rulelearn.data.json;

import java.util.List;

import static org.rulelearn.core.Precondition.notNull;

import org.rulelearn.data.Attribute;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Build objects from JSON.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 *
 */
public class ObjectBuilder {
	
	/** 
	 * Default value for this type of a field.
	 */
	public final static String DEFAULT_ENCODING = "UTF-8";
	
	/** 
	 * Default string representation of a missing value.
	 */
	public final static String MISSING_VALUE_STRING = "?";
	
	/**
	 * All attributes which describe objects.
	 */
	protected Attribute [] attributes = null;
	
	/**
	 * Encoding of text data in JSON file.
	 */
	protected String encoding = ObjectBuilder.DEFAULT_ENCODING; 
	
	/**
	 * Constructor initializing this object builder and setting attributes.
	 * 
	 * @param attributes table with attributes
	 * @throws NullPointerException if all or some of the attributes of the constructed information table have not been set
	 */
	public ObjectBuilder (Attribute [] attributes) {
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				if (attribute == null) throw new NullPointerException("At least one attribute is not set.");
			}
			this.attributes = attributes;
		}
		else {
			throw new NullPointerException("Attributes are not set.");
		}
	}
	
	/**
	 * Constructor initializing this object builder and setting attributes together with encoding of loaded JSON files.
	 * 
	 * @param attributes table with attributes
	 * @param encoding string representation of encoding
	 * @throws NullPointerException if all or some of the attributes describing data to be loaded, and/or encoding have not been set
	 */
	public ObjectBuilder (Attribute [] attributes, String encoding) {
		this(attributes);
		notNull(encoding, "Encoding string is null.");
		this.encoding = encoding;
	}
	
	/**
	 * Parses description of all objects from the supplied JSON structure and returns them as a list of {@link String} arrays.
	 * 
	 * @param json a JSON structure representing objects
	 * @return a list of {@link String} arrays representing description of all objects in the file on all attributes
	 */
	public List<String[]> getObjects(JsonElement json) {
		ObjectArrayList<String []> objectList = new ObjectArrayList<String []>();
	
		if ((json != null) && (!json.isJsonNull())) {
			if (json.isJsonArray()) {
				for (int i = 0; i < ((JsonArray)json).size(); i++) {
					objectList.add(getObject(((JsonArray)json).get(i).getAsJsonObject()));
				}
			}
			else {
				objectList.add(getObject(json));
			}
		}
		return objectList;
	}
	
	/**
	 * Parses description of one object from the supplied JSON structure and returns it as a {@link String} array.
	 * 
	 * @param json a JSON structure representing objects
	 * @return a list of {@link String} arrays representing description of all objects in the file on all attributes
	 */
	protected String [] getObject (JsonElement json) {
		String [] object = null;
		
		if (attributes != null) {
			object = new String [attributes.length];
			JsonElement element = null;
			
			for (int i = 0; i < attributes.length; i++) {
				element = json.getAsJsonObject().get(attributes[i].getName());
				if ((element != null) && (element.isJsonPrimitive())) {
					object[i] = element.getAsString();
				}
				else {
					object[i] = ObjectBuilder.MISSING_VALUE_STRING;
				}
			}
		}
		return object;
	}
}
