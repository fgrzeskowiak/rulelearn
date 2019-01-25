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

import static org.rulelearn.core.Precondition.notNull;

import java.io.Reader;

import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * Parser of objects stored in JSON format.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 *
 */
public class ObjectParser {
	
	/** 
	 * Default encoding.
	 */
	public final static String DEFAULT_ENCODING = "UTF-8";
	
	/** 
	 * Default string representation of a missing value.
	 */
	protected final static String MISSING_VALUE_STRING = "?";
	
	/**
	 * All attributes which describe objects.
	 */
	protected Attribute [] attributes = null;
	
	/**
	 * Encoding of text data in JSON.
	 */
	protected String encoding = ObjectParser.DEFAULT_ENCODING;
	
	/**
	 * String representation of a missing value in JSON.
	 */
	protected String missingValueString = ObjectParser.MISSING_VALUE_STRING;
	
	/**
	 * Constructor initializing this object parser and setting attributes.
	 * 
	 * @param attributes table with attributes
	 * @throws NullPointerException if all or some of the attributes of the constructed information table have not been set
	 */
	public ObjectParser (Attribute [] attributes) {
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
	 * Constructor initializing this object parser, setting attributes, and missing values string (i.e., string representing missing value).
	 * 
	 * @param attributes table with attributes
	 * @param missingValueString string representations of missing value
	 * @throws NullPointerException if all or some of attributes of the constructed object parser, and/or missing value string have not been set
	 */
	public ObjectParser (Attribute [] attributes, String missingValueString) {
		this(attributes);
		notNull(missingValueString, "Missing value string is null.");
		this.missingValueString = missingValueString;
	}
	
	/**
	 * Constructor initializing this object parser, setting attributes, and missing values string (i.e., string representing missing value).
	 * 
	 * @param attributes table with attributes
	 * @param missingValueString string representations of missing value
	 * @param encoding string representation of encoding
	 * @throws NullPointerException if all or some of attributes of the constructed object parser, and/or missing value string, and/or encoding have not been set
	 */
	public ObjectParser (Attribute [] attributes, String missingValueString, String encoding) {
		this(attributes, missingValueString);
		notNull(encoding, "Encoding string is null.");
		this.encoding = encoding;
	}
	
	/**
	 * Parses content from reader {@link Reader} and constructs an information table {@link InformationTable} with parsed objects. 
	 * @param reader a reader with content to be parsed
	 * @return information table {@link InformationTable} with parsed objects
	 */
	public InformationTable parseObjects (Reader reader) {
		notNull(reader, "Reader with content to be parsed is null.");
		InformationTable iTable = null;
		
		JsonElement json = getJSON(reader);
		if ((json != null) && (!json.isJsonNull())) {
			// separator passed to InforamtionTableBuilder is irrelevant here
			InformationTableBuilder iTBuilder  = new InformationTableBuilder(this.attributes, ",", 
													new String [] {ObjectParser.MISSING_VALUE_STRING}); 
			if (json.isJsonArray()) {
				for (int i = 0; i < ((JsonArray)json).size(); i++) {
					iTBuilder.addObject(parseObject(((JsonArray)json).get(i).getAsJsonObject()));
				}
			}
			else {
				iTBuilder.addObject(parseObject(json));
			}
			iTable = iTBuilder.build();
		}
		
		return iTable;
	}
	
	/**
	 * Parses content from reader {@link Reader} into JSON structure {@link JsonElement}. 
	 * @param reader a reader with content to be parsed
	 * @return parsed JSON structure {@link JsonElement}
	 */
	protected JsonElement getJSON (Reader reader) {
		notNull(reader, "Reader with content to be parsed is null.");
		JsonReader jsonReader = new JsonReader(reader);
		notNull(jsonReader, "Could not initialize JsonReader.");
		JsonParser jsonParser = new JsonParser();
		
		return jsonParser.parse(jsonReader);
	}
	
	/**
	 * Parses description of one object from the supplied JSON structure and returns it as a {@link String} array.
	 * 
	 * @param json a JSON structure representing objects
	 * @return a list of {@link String} arrays representing description of all objects in the file on all attributes
	 */
	protected String [] parseObject (JsonElement json) {
		notNull(json, "JSON strucure with objects to be parsed is null.");
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
					object[i] = ObjectParser.MISSING_VALUE_STRING;
				}
			}
		}
		
		return object;
	}
}
