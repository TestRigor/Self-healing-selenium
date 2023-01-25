package com.testrigor.selfhealingselenium.v4.application.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.testrigor.selfhealingselenium.v4.application.services.SelfHealingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebElementProxyHandler extends SeleniumHandlerImpl implements InvocationHandler {

	private final WebElement delegate;

	public WebElementProxyHandler(WebElement aDelegate, SelfHealingService aManager) {
		super(aManager);
		this.delegate = aDelegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if ("findElement".equals(method.getName())) {
				WebElement element = findElement((By) args[0]);
				return Optional.ofNullable(element).map(it -> wrapElement(it, loader)).orElse(null);
			}
			if ("findElements".equals(method.getName())) {
				List<WebElement> elements = findElements((By) args[0]);
				return elements.stream().map(it -> wrapElement(it, loader)).collect(Collectors.toList());
			}
			if ("getWrappedElement".equals(method.getName())) {
				return delegate;
			}
			return method.invoke(delegate, args);
		} catch (WebDriverException ex) {
			throw ex.getCause();
		}
	}
}
