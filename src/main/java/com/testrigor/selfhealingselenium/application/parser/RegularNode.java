package com.testrigor.selfhealingselenium.application.parser;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RegularNode implements NodePath {
	String path;

	@Override
	public Map<Object, Object> toMap() {
		val map = Maps.newHashMap();
		map.put("path", path);
		map.put("included", true);
		return map;
	}
}
