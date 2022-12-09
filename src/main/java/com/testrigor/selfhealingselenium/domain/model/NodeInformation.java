package com.testrigor.selfhealingselenium.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@SuppressWarnings({ "PMD.TooManyFields", "PMD.ShortVariable" })
public class NodeInformation {
	String id;
	String name;
	String type;
	String placeholder;
	String tagName;
	String xpath;
	int posX;
	int posY;
	int width;
	int height;
	int fontSize;
	String display;
	String zIndex;
	String position;
	String overflow;
	@JsonProperty("isVisible")
	boolean isVisible;
	@JsonProperty("isOnScreen")
	boolean isOnScreen;
	@JsonProperty("isInsideScreen")
	boolean isInsideScreen;
	@JsonProperty("isAccessible")
	Boolean isAccessible;
	@JsonProperty("isDummy")
	boolean isDummy;
	String cursor;
	String value;
	String visibility;
	boolean shadowRoot;
	String shadowHost;
	int shadowRootChildrenCount;
	String validationMessage;
	String pseudoBeforeContent;
	String pseudoAfterContent;
	String textTransform;
}
