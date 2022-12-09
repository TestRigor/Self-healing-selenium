package com.testrigor.selfhealingselenium.application.services;

import static com.testrigor.selfhealingselenium.application.parser.NodeInformationDeserializer.deserializeNodeInformation;
import static com.testrigor.selfhealingselenium.application.utils.JsonHelpers.deserializeJson;
import static com.testrigor.selfhealingselenium.application.utils.ResourceReader.getResourceContent;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.testrigor.selfhealingselenium.application.parser.NodeGroupParser;
import com.testrigor.selfhealingselenium.domain.model.NodeInformation;

import lombok.NonNull;
import lombok.val;

public class NodeService {
	public static final String FILE_NEEDED_FORMAT = "%s;%s;%s; if(!document.evaluate) { wgxpath.install(); };";
	public static final String DOCUMENT_API_SRC = getResourceContent("self-healing-info-gatherer.js");
	public static final String ARRAY_FROM_POLYFILL_SRC = getResourceContent("array.from.polyfill.min.js");
	public static final String XPATH_EVALUATE_SRC = getResourceContent("wgxpath.install.js");

	private static final TypeReference<List<Map<String, Object>>> NODE_INFO_TYPE_REF = new TypeReference<>() {
	};

	private static final String BATCH_NODE_INFORMATION_CALL_SRC = format(
		FILE_NEEDED_FORMAT
			+ "return (new SelfHealingInfoGatherer()).getBatchNodeInfoByXpathes(arguments[1], arguments[0]);",
		ARRAY_FROM_POLYFILL_SRC,
		XPATH_EVALUATE_SRC,
		DOCUMENT_API_SRC
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
