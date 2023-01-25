package com.testrigor.selfhealingselenium.v4.application.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;

import com.testrigor.selfhealingselenium.v4.application.services.SelfHealingService;

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

		switch (method.getName()) {
			case "findElement": {
				WebElement element = findElement((By) args[0]);
				if (element != null) {
					return wrapElement(element, loader);
				}
				return method.invoke(driver, args);
			}
			case "findElements": {
				List<WebElement> elements = findElements((By) args[0]);
				return elements.stream().map(it -> wrapElement(it, loader)).collect(Collectors.toList());
			}
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
}
