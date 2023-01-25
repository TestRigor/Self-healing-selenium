package com.testrigor.selfhealingselenium.v3.application.services;

import static com.testrigor.selfhealingselenium.commons.application.parser.NodeInformationDeserializer.deserializeNodeInformation;
import static com.testrigor.selfhealingselenium.commons.application.utils.JsonHelpers.deserializeJson;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;

import com.testrigor.selfhealingselenium.commons.application.NodeFiles;
import com.fasterxml.jackson.core.type.TypeReference;
import com.testrigor.selfhealingselenium.commons.application.parser.NodeGroupParser;
import com.testrigor.selfhealingselenium.commons.domain.model.NodeInformation;

import lombok.NonNull;
import lombok.val;

public class NodeService {
	private static final TypeReference<List<Map<String, Object>>> NODE_INFO_TYPE_REF = new TypeReference<>() {
	};

	private static final String BATCH_NODE_INFORMATION_CALL_SRC = String.format(
		NodeFiles.FILE_NEEDED_FORMAT
			+ "return (new SelfHealingInfoGatherer()).getBatchNodeInfoByXpathes(arguments[1], arguments[0]);",
		NodeFiles.ARRAY_FROM_POLYFILL_SRC,
		NodeFiles.XPATH_EVALUATE_SRC,
		NodeFiles.DOCUMENT_API_SRC
	);

	private final RemoteWebDriver driver;

	public NodeService(RemoteWebDriver aDriver) {
		this.driver = aDriver;
	}

	@SuppressWarnings("unchecked")
	public List<NodeInformation> getBatchNodeInformation(@NonNull Collection<String> xPathCollection) {
		if (xPathCollection.isEmpty()) {
			return new LinkedList<>();
		}

		val parser = new NodeGroupParser(xPathCollection);
		val results = new LinkedList<NodeInformation>();
		val options = new LinkedList<>();

		val pathsOfRoots = new HashMap<>();
		pathsOfRoots.put("roots", Collections.emptyList());
		options.add(pathsOfRoots);
		options.add(parser.getRoot().toMap());

		val args = options.toArray(new Object[0]);
		val jsResultRaw = driver.executeScript(BATCH_NODE_INFORMATION_CALL_SRC, args);

		List<Map<String, Object>> jsResult;

		if (jsResultRaw instanceof String) {
			jsResult = deserializeJson((String) jsResultRaw, NODE_INFO_TYPE_REF);
		} else {
			jsResult = (List<Map<String, Object>>) jsResultRaw;
		}

		for (val entry : jsResult) {
			val info = deserializeNodeInformation(entry);
			if (info.isDummy()) {
				continue;
			}
			results.push(info);
		}

		return results;
	}
}
