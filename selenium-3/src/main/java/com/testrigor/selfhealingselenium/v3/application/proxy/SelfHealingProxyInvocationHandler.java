package com.testrigor.selfhealingselenium.v3.application.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;

import com.testrigor.selfhealingselenium.v3.application.services.SelfHealingService;

public class SelfHealingProxyInvocationHandler extends SeleniumHandlerImpl implements InvocationHandler {

	SelfHealingService manager;

	public SelfHealingProxyInvocationHandler(SelfHealingService aManager) {
		super(aManager);

		this.manager = aManager;
	}

	@Override
	@SuppressWarnings("PMD.CyclomaticComplexity")
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		if (method.getName().startsWith("find")) {
			By locator = detectLocator(method.getName(), args[0]);
			switch (method.getName()) {
				case "findElementById":
				case "findElementByClassName":
				case "findElementByLinkText":
				case "findElementByPartialLinkText":
				case "findElementByName":
				case "findElementByXPath":
				case "findElementByTagName":
				case "findElementByCssSelector":
				case "findElement":
					WebElement element = findElement(locator);
					if (element != null) {
						return wrapElement(element, loader);
					}
					return null;
				case "findElementsById":
				case "findElementsByClassName":
				case "findElementsByLinkText":
				case "findElementsByPartialLinkText":
				case "findElementsByName":
				case "findElementsByXPath":
				case "findElementsByTagName":
				case "findElementsByCssSelector":
				case "findElements":
					List<WebElement> elements = findElements((By) args[0]);
					return elements.stream().map(it -> wrapElement(it, loader)).collect(Collectors.toList());
				default:
					return method.invoke(driver, args);
			}
		}

		switch (method.getName()) {
			case "getManager":
				return manager;
			case "getDelegate":
				return driver;
			case "setTestCaseName": {
				manager.setTestCaseName((String) args[0]);
				return null;
			}
			case "switchTo":
				TargetLocator switched = (TargetLocator) method.invoke(driver, args);
				return wrapTarget(switched, loader);
			default:
				return method.invoke(driver, args);
		}
	}

	@SuppressWarnings("PMD.CyclomaticComplexity")
	private By detectLocator(String methodName, Object value) {
		switch (methodName) {
			case "findElementById":
			case "findElementsById":
				return By.id(String.valueOf(value));
			case "findElementByClassName":
			case "findElementsByClassName":
				return By.className(String.valueOf(value));
			case "findElementByLinkText":
			case "findElementsByLinkText":
				return By.linkText(String.valueOf(value));
			case "findElementByPartialLinkText":
			case "findElementsByPartialLinkText":
				return By.partialLinkText(String.valueOf(value));
			case "findElementByName":
			case "findElementsByName":
				return By.name(String.valueOf(value));
			case "findElementByXPath":
			case "findElementsByXPath":
				return By.xpath(String.valueOf(value));
			case "findElementByTagName":
			case "findElementsByTagName":
				return By.tagName(String.valueOf(value));
			case "findElementByCssSelector":
			case "findElementsByCssSelector":
				return By.cssSelector(String.valueOf(value));
			default:
				return (By) value;
		}
	}

}
