package com.testrigor.selfhealingselenium.application.utils;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import lombok.NonNull;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class ResourceReader {
	public static String getResourceContent(@NonNull String relativePath) {
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(relativePath);
			return Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
