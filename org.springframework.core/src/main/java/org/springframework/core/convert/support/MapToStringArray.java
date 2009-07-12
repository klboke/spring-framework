/*
 * Copyright 2004-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.core.convert.support;

import java.lang.reflect.Array;
import java.util.Map;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;

/**
 * Converts a Map to a String array, where each element in the array
 * is of the format key=value.
 * @author Keith Donald
 * @since 3.0
 */
@SuppressWarnings("unchecked")
class MapToStringArray implements ConversionExecutor {

	private TypeDescriptor targetType;

	private GenericTypeConverter conversionService;

	private MapEntryConverter entryConverter;

	public MapToStringArray(TypeDescriptor sourceType, TypeDescriptor targetType, GenericTypeConverter conversionService) {
		this.targetType = targetType;
		this.conversionService = conversionService;
		this.entryConverter = createEntryConverter();
	}

	private MapEntryConverter createEntryConverter() {
		if (targetType.isMapEntryTypeKnown()) {
			ConversionExecutor keyConverter = conversionService.getConversionExecutor(targetType.getMapKeyType(),
					TypeDescriptor.valueOf(String.class));
			ConversionExecutor valueConverter = conversionService.getConversionExecutor(targetType.getMapValueType(),
					TypeDescriptor.valueOf(String.class));
			return new MapEntryConverter(keyConverter, valueConverter);
		} else {
			return MapEntryConverter.NO_OP_INSTANCE;
		}
	}

	public Object execute(Object source) throws ConversionFailedException {
		Map sourceMap = (Map) source;
		Object array = Array.newInstance(targetType.getElementType(), sourceMap.size());
		int i = 0;
		for (Object entry : sourceMap.entrySet()) {
			Map.Entry mapEntry = (Map.Entry) entry;
			Object key = mapEntry.getKey();
			Object value = mapEntry.getValue();
			String property = entryConverter.convertKey(key) + "=" + entryConverter.convertValue(value);
			Array.set(array, i, property);
			i++;
		}
		return array;
	}

}
