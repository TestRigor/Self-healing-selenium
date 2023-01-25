package com.testrigor.selfhealingselenium.v4.application;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PrintsPage;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.testrigor.selfhealingselenium.v4.application.services.SelfHealingService;

public interface SelfHealingDriver extends WebDriver, JavascriptExecutor, HasCapabilities, HasVirtualAuthenticator, Interactive, PrintsPage, TakesScreenshot {

	SelfHealingService getManager();

	<T extends RemoteWebDriver> T getDelegate();

	void setTestCaseName(String name);
}
