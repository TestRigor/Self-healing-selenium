package com.testrigor.selfhealingselenium.application.parser;

import java.util.Collection;
import java.util.HashSet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NodeGroupParser {
	Collection<String> paths;
	NodeGroup root;

	private NodeGroupParser() {
		super();
	}

	public NodeGroupParser(Collection<String> aPaths) {
		this();
		paths = aPaths;

		initialize();
	}

	private void initialize() {
		val usedIds = new HashSet<String>();
		this.root = NodeGroup.of("root", usedIds);
		paths.stream()
			.map((v) -> v.split("/"))
			.forEach((tokens) -> root.addNodes(tokens, usedIds));
	}
}
