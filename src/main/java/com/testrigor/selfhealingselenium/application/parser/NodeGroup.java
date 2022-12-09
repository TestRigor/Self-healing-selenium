package com.testrigor.selfhealingselenium.application.parser;

import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@SuppressWarnings({ "PMD.ShortVariable", "PMD.ShortMethodName" })
public final class NodeGroup implements NodePath {

	private static final Pattern ID_PATTERN = Pattern.compile("(\\[@id=.*?\\])");
	private static final Pattern ID_VALUE_PATTERN = Pattern.compile("'(.*?)'");
	private static final String DELIMITER = "/";

	String path;
	Map<String, NodePath> nodes = new HashMap<>();
	@Setter
	boolean included;
	@Setter
	String id;

	private NodeGroup() {
		super();
	}

	private NodeGroup(String aPath, String aId) {
		this();
		path = aPath;
		id = aId;
	}

	public static NodeGroup of(String path, Set<String> usedIds) {
		if (isBlank(path)) {
			throw new IllegalArgumentException("Missing path");
		}
		String id = EMPTY;
		val matcher = ID_VALUE_PATTERN.matcher(path);

		if (matcher.find()) {
			id = matcher.group().replaceAll("'", "");
			if (!usedIds.add(id)) {
				id = EMPTY;
			}
		}

		return new NodeGroup(path, id);
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void addNodes(String[] tokens, Set<String> usedIds) {
		boolean createRegularNode = true;
		int lastIndex = (tokens.length - 1);
		for (int i = 0; i < tokens.length; i++) {
			val matcher = ID_PATTERN.matcher(tokens[i]);
			if (matcher.find()) {
				val currentPath = String.join(DELIMITER, Arrays.copyOfRange(tokens, 0, i + 1));
				NodeGroup nodeGroup = (NodeGroup) nodes.getOrDefault(currentPath, NodeGroup.of(currentPath, usedIds));

				if (i == lastIndex) {
					nodeGroup.setIncluded(true);
				}

				int remainingFromIndex = (i + 1);
				if (remainingFromIndex < lastIndex + 1) {
					String[] remaining = Arrays.copyOfRange(tokens, i + 1, lastIndex + 1);
					nodeGroup.addNodes(remaining, usedIds);
				}
				nodes.put(currentPath, nodeGroup);
				createRegularNode = false;
				break;
			}
		}

		if (createRegularNode) {
			val currentPath = String.join(DELIMITER, tokens);
			nodes.put(currentPath, new RegularNode(currentPath));
		}
	}

	@Override public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		NodeGroup nodeGroup = (NodeGroup) other;
		return path.equals(nodeGroup.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public Map<Object, Object> toMap() {
		val map = Maps.newHashMap();
		val parsedNodes = getNodes().values().stream()
			.map(NodePath::toMap)
			.collect(toCollection(ArrayList::new));

		map.put("path", path);
		map.put("included", included);
		map.put("nodes", parsedNodes);
		if (isNotBlank(id)) {
			map.put("id", id);
		}
		return map;
	}
}
