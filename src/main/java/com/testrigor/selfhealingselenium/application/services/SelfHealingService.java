package com.testrigor.selfhealingselenium.application.services;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.testrigor.selfhealingselenium.domain.model.Action;
import com.testrigor.selfhealingselenium.domain.model.Locator;
import com.testrigor.selfhealingselenium.domain.model.NodeInformation;
import com.testrigor.selfhealingselenium.infrastructure.adapters.TestrigorAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

@FieldDefaults(level = PRIVATE)
public class SelfHealingService {

	private static final Config DEFAULT_CONFIG = ConfigFactory.systemProperties().withFallback(
		ConfigFactory.load("application.properties").withFallback(ConfigFactory.load()));

	final NodeService nodeService;
	final TestrigorAdapter adapter;
	@Getter final RemoteWebDriver driver;
	@Setter
	String testCaseName;

	public SelfHealingService(RemoteWebDriver delegate, String apiToken) {

		this.driver = delegate;
		this.adapter = new TestrigorAdapter(DEFAULT_CONFIG, apiToken);
		this.nodeService = new NodeService(delegate);
	}

	public List<NodeInformation> getPageNodes() {
		val xPaths = adapter.getNodesXPaths(driver.getPageSource());
		return nodeService.getBatchNodeInformation(xPaths);
	}

	public void saveAction(Action action) {
		val pageNodes = getPageNodes();
		adapter.saveLocator(action, pageNodes, driver.getPageSource(), testCaseName);
	}

	public Optional<By> getHealedLocator(Locator locator) {
		val pageNodes = getPageNodes();
		val healedLocator = adapter.getHealedLocator(locator, pageNodes, driver.getPageSource(), testCaseName);
		By byLocator;
		String value = healedLocator.getValue();
		switch (healedLocator.getType()) {
			case ID:
				byLocator = By.id(value);
				break;
			case XPATH:
				byLocator = By.xpath(value);
				break;
			case CSS_SELECTOR:
				byLocator = By.cssSelector(value);
				break;
			case TAG_NAME:
				byLocator = By.tagName(value);
				break;
			case LINK_TEXT:
				byLocator = By.linkText(value);
				break;
			case NAME:
				byLocator = By.name(value);
				break;
			case CLASS:
				byLocator = By.className(value);
				break;
			case PARTIAL_LINK_TEXT:
				byLocator = By.partialLinkText(value);
				break;
			default:
				return Optional.empty();
		}
		return Optional.of(byLocator);
	}
}
