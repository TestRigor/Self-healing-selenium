package com.testrigor.selfhealingselenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.testrigor.selfhealingselenium.application.SelfHealingDriver;
import com.testrigor.selfhealingselenium.application.proxy.ProxyFactory;
import com.testrigor.selfhealingselenium.application.proxy.SelfHealingProxyInvocationHandler;
import com.testrigor.selfhealingselenium.application.services.SelfHealingService;

public class TestRigor {

	public static SelfHealingDriver selfHeal(RemoteWebDriver originalDriver, String apiToken) {
		return create(originalDriver, apiToken);
	}

	static SelfHealingDriver create(RemoteWebDriver delegate, String apiToken) {
		SelfHealingService selfHealingEngine = new SelfHealingService(delegate, apiToken);
		return create(selfHealingEngine);
	}

	public static SelfHealingDriver create(SelfHealingService manager) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<? extends WebDriver> driverClass = manager.getDriver().getClass();
		SelfHealingProxyInvocationHandler handler = new SelfHealingProxyInvocationHandler(manager);
		return ProxyFactory.createDriverProxy(classLoader, handler, driverClass);
	}
}
