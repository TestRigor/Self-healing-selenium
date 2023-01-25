package com.testrigor.selfhealingselenium.commons.domain.model;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocatorType {
	XPATH("xpath"),
	ID("id"),
	CLASS("class"),
	NAME("name"),
	TAG_NAME("tag_name"),
	CSS_SELECTOR("css_selector"),
	LINK_TEXT("link_text"),
	PARTIAL_LINK_TEXT("partial_link_text");


	@JsonValue
	String value;
	@JsonCreator
	public static LocatorType fromName(String name) {
		return Stream.of(values()).filter(status -> status.getValue().equals(name))
			.findFirst().orElseGet(() -> valueOf(name));
	}
}
