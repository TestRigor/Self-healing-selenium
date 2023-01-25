package com.testrigor.selfhealingselenium.commons.application.parser;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.testrigor.selfhealingselenium.commons.application.utils.HtmlConstants;
import com.testrigor.selfhealingselenium.commons.domain.model.NodeInformation;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Log4j2
public class NodeInformationDeserializer {
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
	public static final String KEY_TYPE = "type";
	public static final String KEY_PLACEHOLDER = "placeholder";
	public static final String KEY_TAG_NAME = "tagName";
	public static final String KEY_IS_VISIBLE = "isVisible";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_Z_INDEX = "zIndex";
	public static final String KEY_FONT_SIZE = "fontSize";
	public static final String KEY_POSITION = "position";
	public static final String KEY_IS_ACCESSIBLE = "isAccessible";
	public static final String KEY_IS_INSIDE_SCREEN = "isInsideScreen";
	public static final String KEY_IS_ON_SCREEN = "isOnScreen";
	public static final String KEY_IS_DUMMY = "isDummy";
	public static final String KEY_WIDTH = "width";
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_POS_Y = "y";
	public static final String KEY_POS_X = "x";
	public static final String KEY_XPATH = "xpath";
	public static final String KEY_CURSOR = "cursor";
	public static final String KEY_VALUE = "value";
	public static final String KEY_VISIBILITY = "visibility";
	public static final String KEY_SHADOW_ROOT = "shadowRoot";
	public static final String KEY_SHADOW_HOST = "shadowHost";
	public static final String KEY_SHADOW_ROOT_CHILDREN_COUNT = "shadowRootChildrenCount";
	public static final String KEY_OVERFLOW = "overflow";
	public static final String KEY_VALIDATION_MESSAGE = "validationMessage";
	public static final String KEY_PSEUDO_BEFORE_CONTENT = "pseudoBeforeContent";
	public static final String KEY_PSEUDO_AFTER_CONTENT = "pseudoAfterContent";
	public static final String KEY_TEXT_TRANSFORM = "textTransform";

	public static final NodeInformation NULL_INFO = NodeInformation.builder()
		.xpath("//null")
		.posX(0)
		.posY(0)
		.height(0)
		.width(0)
		.isDummy(true)
		.isVisible(false)
		.display(HtmlConstants.VALUE_NONE)
		.visibility(HtmlConstants.VALUE_HIDDEN)
		.zIndex(HtmlConstants.VALUE_MINUS_ONE)
		.isOnScreen(false)
		.isInsideScreen(false)
		.shadowRoot(false)
		.shadowHost(null)
		.shadowRootChildrenCount(0)
		.isAccessible(null)
		.cursor(HtmlConstants.VALUE_NONE)
		.textTransform(null)
		.build();

	static final int VERY_LARGE = 100;

	private static final String CSS_EM = "em";
	private static final String CSS_PX = "px";
	private static final String CSS_IMPORTANT = "!important";
	private static final String CSS_SEMICOLON = ";";

	private static final List<String> REQUIRED_KEYS = ImmutableList.of(
		KEY_IS_VISIBLE,
		KEY_IS_INSIDE_SCREEN,
		KEY_IS_ON_SCREEN,
		KEY_WIDTH,
		KEY_HEIGHT,
		KEY_POS_Y,
		KEY_POS_X,
		KEY_XPATH
	);

	@NonNull
	public static NodeInformation deserializeNodeInformation(@NonNull Map<String, Object> object) {
		for (val key : REQUIRED_KEYS) {
			if (!object.containsKey(key)) {
				return NULL_INFO;
			}
		}

		final Boolean isAccessibleOpt = object.containsKey(KEY_IS_ACCESSIBLE)
			? (Boolean) object.get(KEY_IS_ACCESSIBLE)
			: null;

		final boolean isDummy = object.containsKey(KEY_IS_DUMMY) && (Boolean) object.get(KEY_IS_DUMMY);
		final int shadowRootChildrenCount = (object.containsKey(KEY_SHADOW_ROOT_CHILDREN_COUNT))
			&& (object.get(KEY_SHADOW_ROOT_CHILDREN_COUNT) != null)
			? ((Number) object.get(KEY_SHADOW_ROOT_CHILDREN_COUNT)).intValue()
			: 0;

		final int fontSize = convertFontSizeToPixels(getValueOrNull(object.get(KEY_FONT_SIZE)));

		val info = NodeInformation.builder()
			.id((String) object.get(KEY_ID))
			.name((String) object.get(KEY_NAME))
			.type((String) object.get(KEY_TYPE))
			.placeholder((String) object.get(KEY_PLACEHOLDER))
			.tagName((String) object.get(KEY_TAG_NAME))
			.xpath((String) object.get(KEY_XPATH))
			.posX(((Number) object.get(KEY_POS_X)).intValue())
			.posY(((Number) object.get(KEY_POS_Y)).intValue())
			.height(((Number) object.get(KEY_HEIGHT)).intValue())
			.width(((Number) object.get(KEY_WIDTH)).intValue())
			.isDummy(isDummy)
			.isVisible((Boolean) object.get(KEY_IS_VISIBLE))
			.display(getValueOrNull(object.get(KEY_DISPLAY)))
			.zIndex(getValueOrNull(object.get(KEY_Z_INDEX)))
			.fontSize(fontSize)
			.position(getValueOrNull(object.get(KEY_POSITION)))
			.isOnScreen((Boolean) object.get(KEY_IS_ON_SCREEN))
			.isInsideScreen((Boolean) object.get(KEY_IS_INSIDE_SCREEN))
			.isAccessible(isAccessibleOpt)
			.shadowRoot((Boolean) object.get(KEY_SHADOW_ROOT))
			.shadowHost(getValueOrNull(object.get(KEY_SHADOW_HOST)))
			.shadowRootChildrenCount(shadowRootChildrenCount)
			.cursor(getValueOrNull(object.get(KEY_CURSOR)))
			.value(getValueOrNull(object.get(KEY_VALUE)))
			.visibility(getValueOrNull(object.get(KEY_VISIBILITY)))
			.overflow(getValueOrNull(object.get(KEY_OVERFLOW)))
			.validationMessage(getValueOrNull(object.get(KEY_VALIDATION_MESSAGE)))
			.pseudoBeforeContent(getValueOrNull(object.get(KEY_PSEUDO_BEFORE_CONTENT)))
			.pseudoAfterContent(getValueOrNull(object.get(KEY_PSEUDO_AFTER_CONTENT)))
			.textTransform(getValueOrNull(object.get(KEY_TEXT_TRANSFORM)))
			.build();

		return info;
	}

	static int convertFontSizeToPixels(String fontSizeStrRaw) {
		if (isBlank(fontSizeStrRaw)) {
			return 0;
		}

		String fontSizeStr = trim(fontSizeStrRaw);

		if (endsWithIgnoreCase(fontSizeStr, CSS_IMPORTANT)) {
			fontSizeStr = trim(removeEndIgnoreCase(fontSizeStr, CSS_IMPORTANT));
		}

		if (endsWithIgnoreCase(fontSizeStr, CSS_SEMICOLON)) {
			fontSizeStr = trim(removeEndIgnoreCase(fontSizeStr, CSS_SEMICOLON));
		}

		if (endsWithIgnoreCase(fontSizeStr, "vw")) {
			// This is percentages of viewport, instead of getting the viewport size and calculating we just put it as a large value to hint a header
			return VERY_LARGE;
		}

		if (endsWithIgnoreCase(fontSizeStr, CSS_PX)) {
			String pxSizeStr = removeEndIgnoreCase(fontSizeStr, CSS_PX);
			final int pxValue = toInt(pxSizeStr, HtmlConstants.DEFAULT_FONT_SIZE);
			return pxValue;
		}

		if (endsWithIgnoreCase(fontSizeStr, CSS_EM)) {
			String emSizeStr = removeEndIgnoreCase(fontSizeStr, CSS_EM);
			final double emValue = toDouble(emSizeStr, 1.0);

			// Standard default font size is 16
			final int pxValue = (int) Math.round(HtmlConstants.DEFAULT_FONT_SIZE * emValue);
			return pxValue;
		}

		return 0;
	}

	static String getValueOrNull(Object obj) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof String) {
			return (String) obj;
		}

		return String.valueOf(obj);
	}
}
