package com.testrigor.selfhealingselenium.application.proxy;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.collect.ImmutableMap;
import com.testrigor.selfhealingselenium.application.services.SelfHealingService;
import com.testrigor.selfhealingselenium.domain.model.Action;
import com.testrigor.selfhealingselenium.domain.model.ActionType;
import com.testrigor.selfhealingselenium.domain.model.Locator;
import com.testrigor.selfhealingselenium.domain.model.LocatorType;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@FieldDefaults(level = PROTECTED, makeFinal = true)
@Slf4j
@SuppressWarnings("PMD.ShortVariable")
public class SeleniumHandlerImpl implements SeleniumHandler {

	private static final Map<Class<?>, LocatorType> LOCATOR_MAPPER = ImmutableMap.<Class<?>, LocatorType>builder()
		.put(By.ById.class, LocatorType.ID)
		.put(By.ByClassName.class, LocatorType.CLASS)
		.put(By.ByCssSelector.class, LocatorType.CSS_SELECTOR)
		.put(By.ByName.class, LocatorType.NAME)
		.put(By.ByXPath.class, LocatorType.XPATH)
		.put(By.ByLinkText.class, LocatorType.LINK_TEXT)
		.put(By.ByPartialLinkText.class, LocatorType.PARTIAL_LINK_TEXT)
		.put(By.ByTagName.class, LocatorType.TAG_NAME)
		.build();

	SelfHealingService selfHealingService;
	RemoteWebDriver driver;

	public SeleniumHandlerImpl(SelfHealingService aSelfHealingService) {
		this.selfHealingService = aSelfHealingService;
		this.driver = aSelfHealingService.getDriver();
	}

	@Override
	public WebElement findElement(By by) {
		val locator = new Locator(LOCATOR_MAPPER.get(by.getClass()), locatorValue(by));
		val action = new Action(ActionType.FIND, locator);
		try {
			val element = driver.findElement(by);
			selfHealingService.saveAction(action);

			return element;
		} catch (NoSuchElementException noSuchElementException) {
			log.info("Trying to self heal with locator {} and value {}", locator.getType(), locator.getValue());
			val healedLocator = selfHealingService.getHealedLocator(locator)
				.orElseThrow(() -> noSuchElementException);
			return driver.findElement(healedLocator);
		}
	}

	public static String getMethodName(final int depth) {
		return StackWalker
			.getInstance()
			.walk(stream -> stream.skip(depth).findFirst().get())
			.getMethodName();
	}

	@Override
	public List<WebElement> findElements(By by) {
		return driver.findElements(by);
	}

	@Override
	public WebElement wrapElement(WebElement element, ClassLoader loader) {
		WebElementProxyHandler elementProxyHandler = new WebElementProxyHandler(element, selfHealingService);
		return ProxyFactory.createWebElementProxy(loader, elementProxyHandler);
	}

	@Override
	public WebDriver.TargetLocator wrapTarget(WebDriver.TargetLocator locator, ClassLoader loader) {
		TargetLocatorProxyInvocationHandler handler = new TargetLocatorProxyInvocationHandler(locator, selfHealingService);
		return ProxyFactory.createTargetLocatorProxy(loader, handler);
	}

	private String locatorValue(By by) {
		String[] locatorParts = by.toString().split(":", 2);
		return StringUtils.trim(locatorParts[1]);
	}
}
