package com.testrigor.selfhealingselenium.commons.application;

import static com.testrigor.selfhealingselenium.commons.application.utils.ResourceReader.getResourceContent;

public final class NodeFiles {
	public static final String FILE_NEEDED_FORMAT = "%s;%s;%s; if(!document.evaluate) { wgxpath.install(); };";
	public static final String DOCUMENT_API_SRC = getResourceContent("self-healing-info-gatherer.js");
	public static final String ARRAY_FROM_POLYFILL_SRC = getResourceContent("array.from.polyfill.min.js");
	public static final String XPATH_EVALUATE_SRC = getResourceContent("wgxpath.install.js");
}
