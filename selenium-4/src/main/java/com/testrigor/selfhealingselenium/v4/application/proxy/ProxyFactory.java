package com.testrigor.selfhealingselenium.v4.application.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Stream;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Locatable;

import com.testrigor.selfhealingselenium.v4.application.SelfHealingDriver;

@SuppressWarnings("unchecked")
public final class ProxyFactory {

	private ProxyFactory() {
		super();
	}

	public static <T extends WebDriver> SelfHealingDriver createDriverProxy(ClassLoader loader, InvocationHandler handler, Class<T> clazz) {
		Class<?>[] interfaces = Stream.concat(
			Arrays.stream(clazz.getInterfaces()),
			Stream.of(JavascriptExecutor.class, SelfHealingDriver.class, Interactive.class)
		).distinct().toArray(Class[]::new);

		return (SelfHealingDriver) Proxy.newProxyInstance(loader, interfaces, handler);
	}

	public static <T extends WebElement> T createWebElementProxy(ClassLoader loader, InvocationHandler handler) {
		Class<?>[] interfaces = { WebElement.class, WrapsElement.class, Locatable.class };
		return (T) Proxy.newProxyInstance(loader, interfaces, handler);
	}

	public static <T extends WebDriver.TargetLocator> T createTargetLocatorProxy(ClassLoader loader, InvocationHandler handler) {
		Class<?>[] interfaces = { WebDriver.TargetLocator.class };
		return (T) Proxy.newProxyInstance(loader, interfaces, handler);
	}
}
