package com.testrigor.selfhealingselenium.commons.application.parser;

import java.util.Map;

public interface NodePath {
	String getPath();

	Map<Object, Object> toMap();
}
