package com.testrigor.selfhealingselenium.application.proxy;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@SuppressWarnings("PMD.ShortVariable")
public interface SeleniumHandler {

	WebElement findElement(By by);

	List<WebElement> findElements(By by);

	WebElement wrapElement(WebElement element, ClassLoader loader);

	WebDriver.TargetLocator wrapTarget(WebDriver.TargetLocator locator, ClassLoader loader);
}
