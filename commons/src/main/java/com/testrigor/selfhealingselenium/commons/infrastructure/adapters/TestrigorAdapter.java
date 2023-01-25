package com.testrigor.selfhealingselenium.commons.infrastructure.adapters;

import java.util.List;

import com.testrigor.selfhealingselenium.commons.domain.model.Action;
import com.testrigor.selfhealingselenium.commons.domain.model.Locator;
import com.testrigor.selfhealingselenium.commons.domain.model.NodeInformation;

public interface TestrigorAdapter {

	List<String> getNodesXPaths(String pageSource);

	void saveLocator(Action action, List<NodeInformation> pageNodes, String pageSource, String testCaseName);

	Locator getHealedLocator(Locator locator, List<NodeInformation> pageNodes, String pageSource, String testCaseName);
}
