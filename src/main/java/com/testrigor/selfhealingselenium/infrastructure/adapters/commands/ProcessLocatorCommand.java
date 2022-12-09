package com.testrigor.selfhealingselenium.infrastructure.adapters.commands;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.testrigor.selfhealingselenium.domain.model.LocatorType;
import com.testrigor.selfhealingselenium.domain.model.NodeInformation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessLocatorCommand {
	LocatorType locatorType;
	String locatorValue;
	String testCaseName;
	List<NodeInformation> pageNodes;
	String pageSource;
}
