package com.testrigor.selfhealingselenium.application.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;

import com.testrigor.selfhealingselenium.TestRigor;
import com.testrigor.selfhealingselenium.application.SelfHealingDriver;
import com.testrigor.selfhealingselenium.application.services.SelfHealingService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TargetLocatorProxyInvocationHandler implements InvocationHandler {

	private final TargetLocator delegate;
	private final SelfHealingService manager;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			Object result = method.invoke(delegate, args);
			boolean isProxy = result instanceof SelfHealingDriver;
			boolean isWebDriver = result instanceof WebDriver;
			if (isWebDriver && !isProxy) {
				return TestRigor.create(manager);
			} else {
				return result;
			}
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}
