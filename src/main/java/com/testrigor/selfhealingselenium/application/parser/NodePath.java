package com.testrigor.selfhealingselenium.application.parser;

import java.util.Map;

public interface NodePath {
	String getPath();

	Map<Object, Object> toMap();
}
