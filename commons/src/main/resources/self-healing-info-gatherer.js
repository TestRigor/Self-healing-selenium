

function SelfHealingInfoGatherer() {
  if (!String.prototype.startsWith) {
    Object.defineProperty(String.prototype, "startsWith", {
      value: function(search, rawPos) {
        var pos = rawPos > 0 ? rawPos|0 : 0;
        return this.substring(pos, pos + search.length) === search;
      }
    });
  }
}

SelfHealingInfoGatherer.prototype.findNodeInsideShadowDom = function (nodes, xpath) {
  var splitXPath = xpath.split("/");
  var firstNode = splitXPath ? splitXPath[1].toUpperCase() : xpath;
  var match = firstNode.match(/\[(.*?)]/);
  var nodeName = match ? firstNode.split("[")[0] : firstNode;
  var index = match ? (match[1] - 1) : 0;
  var nodesFiltered = Array.from(nodes).filter(function(item) {
    return item.nodeName === nodeName;
  });
  return { node: nodesFiltered[index], xpath: xpath.replace("/" + splitXPath[1], ".") };
};

SelfHealingInfoGatherer.prototype.findShadowHost = function(roots) {
  var length = roots.length;
  var currentRoot;
  for (var i = 0; i < length; i++) {
    var xpath = roots[i];
    if (!currentRoot || xpath.startsWith("/html")) {
      currentRoot = this.findNodeByXpath(xpath);
    } else {
      var newRoot = this.findNodeByXpath(xpath, currentRoot.shadowRoot);
      if (newRoot) {
        currentRoot = newRoot;
      }
    }
  }
  return (currentRoot || {});
};

SelfHealingInfoGatherer.prototype.findShadowRoot = function(roots) {
  return this.findShadowHost(roots).shadowRoot;
};

SelfHealingInfoGatherer.prototype.findNodeByXpath = function (xpath, shadowRoot, context) {
  try {
    var xpathToUse = xpath;
    if (context) {
      xpathToUse = "./" + xpath;
    }
    var currentContext = context || document;
    if (shadowRoot) {
      var result = this.findNodeInsideShadowDom(shadowRoot.childNodes, xpath);
      if (result) {
        currentContext = result.node;
        xpathToUse = result.xpath;
      }
    }
    return document
        .evaluate(xpathToUse, currentContext, function(prefix) { if (prefix === "svg") { return "http://www.w3.org/2000/svg"; } else { return null; } }, XPathResult.FIRST_ORDERED_NODE_TYPE, null)
        .singleNodeValue;
  } catch (error) {
    this.log("Couldn't evaluate xpath " + xpath);
  }
};

SelfHealingInfoGatherer.prototype.getNode = function(nodeToGet, parentNode, pathPrefix, windowRect, results, shadowRootXPath, shadowRoot) {
  var self = this;
  var path = nodeToGet.path;
  var xpath = pathPrefix + "/" + nodeToGet.path;

  if (path.startsWith("//") || path.startsWith("/html") || shadowRoot) {
    xpath = nodeToGet.path;
  }

  var node;
  if (nodeToGet.id) {
    node = document.getElementById(nodeToGet.id);
  }
  if (!node) {
    node = this.findNodeByXpath(path, shadowRoot, parentNode);
  }

  if (node && nodeToGet.included) {
    var nodeInfo;
    try {
      nodeInfo = self.getNodeInfo(node, windowRect, shadowRootXPath);
    } catch(err) {
      self.log("getNodeInfo error: " + err + ". path: " + path + ". xpath: " + xpath + ". shadowRootXPath: " + shadowRootXPath);
      nodeInfo = self.getDummyInfo();
    }
    nodeInfo.xpath = xpath;
    results.push(nodeInfo);
  }
  if (nodeToGet.nodes) {
    nodeToGet.nodes.forEach(function(nestedNodeToGet) {
      self.getNode(nestedNodeToGet, node, xpath, windowRect, results, shadowRootXPath, shadowRoot);
    });
  }
};

SelfHealingInfoGatherer.prototype.buildXPath = function(element, checkForShort, shadowRoot) {
  if (!element || (shadowRoot && (element === shadowRoot))) {
    return "";
  }

  if (element instanceof DocumentFragment) {
    element = element.host;
  }

  if (!element.tagName || !element.parentNode) {
    return "";
  }

  var tag = element.tagName.toLowerCase();

  if (checkForShort && element.id) {
    var allWithId = document.querySelectorAll("[id=" + element.id + "]");
    if (allWithId.length === 1) {
      return "//" + tag + "[@id='" + element.id + "']";
    }
  }

  var sameTagSiblings = Array.from(element.parentNode.childNodes)
      .filter(function(elem) {
        return elem.tagName === element.tagName;
      });

  var idx = sameTagSiblings.indexOf(element);

  var tagToUse = tag;

  if (tag.includes && tag.includes(":")) {
    tagToUse = "*[name()='" + tag + "']";
  } else if (tag === "svg") {
    tagToUse = tag + ":" + tag;
  }

  var result = this.buildXPath(element.parentNode, checkForShort, shadowRoot)
      + "/" + tagToUse
      + "[" + (idx + 1) + "]";

  return result;
};

SelfHealingInfoGatherer.prototype.log = function (message) {
  if (console.debug) {
    console.debug("{7D404D1B-EC46-42D1-BF8B-76C9F31435CF}" + message);
  }
};

SelfHealingInfoGatherer.prototype.isNodeContainsOther = function (parent, child) {
  if (!parent) {
    this.log("missing parent");
    return false;
  }

  if (!child) {
    this.log("missing child");
    return false;
  }

  var current = child;

  do {
    // Check parent or DOCUMENT_FRAGMENT_NODE, see https://developer.mozilla.org/en-US/docs/Web/API/Node/nodeType#Node_type_varants
    if (parent.isSameNode(current) || (current.nodeType === Node.DOCUMENT_FRAGMENT_NODE)) {
      return true;
    }

    current = current.parentNode;
  } while (current);

  return false;
};

SelfHealingInfoGatherer.prototype.getDummyInfo = function () {
  return {
    xpath: null,
    x: 0,
    y: 0,
    width: 0,
    height: 0,
    isVisible: false,
    isOnScreen: false,
    isInsideScreen: false,
    isAccessible: false,      // optional
    isDummy: true,            // optional
    shadowRoot: false,         // optional
    shadowHost: null,         // optional
  };
};

SelfHealingInfoGatherer.prototype.getRectCenter = function (rect) {
  return {
    x: Math.round(rect.x + (rect.width / 2)),
    y: Math.round(rect.y + (rect.height / 2)),
  };
};

SelfHealingInfoGatherer.prototype.getWindowRect = function () {
  return {
    x: window.pageXOffset,
    y: window.pageYOffset,
    width: window.innerWidth,
    height: window.innerHeight,
  };
};

SelfHealingInfoGatherer.prototype.hasShadowRoot = function (node) {
  if (!node) {
    return false;
  }
  return !!node["shadowRoot"];
};

SelfHealingInfoGatherer.prototype.isPointInsideRect = function (x, y, rect) {
  return (x >= rect.x)
      && (x <= rect.x + rect.width)
      && (y >= rect.y)
      && (y <= rect.y + rect.height);
};

SelfHealingInfoGatherer.prototype.hasRectIntersection = function (rect1, rect2) {
  return this.isPointInsideRect(rect1.x, rect1.y, rect2)
      || this.isPointInsideRect(rect1.x + rect1.width, rect1.y, rect2)
      || this.isPointInsideRect(rect1.x, rect1.y + rect1.height, rect2)
      || this.isPointInsideRect(rect1.x + rect1.width, rect1.y + rect1.height, rect2);
};

SelfHealingInfoGatherer.prototype.isInsideRect = function (innerRect, outerRect) {
  return this.isPointInsideRect(innerRect.x, innerRect.y, outerRect)
      && this.isPointInsideRect(innerRect.x + innerRect.width, innerRect.y, outerRect)
      && this.isPointInsideRect(innerRect.x, innerRect.y + innerRect.height, outerRect)
      && this.isPointInsideRect(innerRect.x + innerRect.width, innerRect.y + innerRect.height, outerRect);
};

SelfHealingInfoGatherer.prototype.getRect = function (node, windowRect) {
  var boundingClientRect = node.getBoundingClientRect();
  var x = windowRect.x + boundingClientRect.left;
  var y = windowRect.y + boundingClientRect.top;
  var width = boundingClientRect.width;
  var height = boundingClientRect.height;

  return {
    x: x,
    y: y,
    width: width,
    height: height,
  };
};

SelfHealingInfoGatherer.prototype.isNodeChecked = function (node) {
  if(!node) {
    return false;
  }

  if ((node.type === "checkbox") || (node.type === "radio")) {
    return !!node.checked;
  } else {
    return node.getAttribute("aria-checked") === "true";
  }
};

SelfHealingInfoGatherer.prototype.getNodeInfo = function (node, windowRect, shadowRootXPath) {
  if (!node) {
    return this.getDummyInfo();
  }

  if (!windowRect) {
    windowRect = this.getWindowRect();
  }

  var rect = this.getRect(node, windowRect);
  rect.isInsideScreen = this.isInsideRect(rect, windowRect);
  rect.isOnScreen = rect.isInsideScreen || this.hasRectIntersection(rect, windowRect);
  if (node.value instanceof Object) {
    rect.value = (node.value && node.value.label) ? node.value.label : null;
  } else {
    rect.value = node.value;
  }
  rect.checked = this.isNodeChecked(node);
  rect.isVisible = !!(node.offsetWidth || node.offsetHeight || node.getClientRects().length);
  rect.shadowRoot = this.hasShadowRoot(node);
  rect.shadowHost = shadowRootXPath;
  if (rect.shadowRoot) {
    rect.shadowRootChildrenCount = node.childElementCount;
  }
  //html5 form validation
  rect.validationMessage = node.validationMessage;

  if (rect.isVisible) {
    var style = window.getComputedStyle(node);

    rect.cursor = style.getPropertyValue("cursor");
    rect.display = style.getPropertyValue("display");
    rect.fontSize = style.getPropertyValue("font-size");
    rect.zIndex = style.getPropertyValue("z-index");
    rect.position = style.getPropertyValue("position");
    rect.visibility = style.getPropertyValue("visibility");
    rect.overflow = style.getPropertyValue("overflow");
    rect.textTransform = style.getPropertyValue("text-transform");

    var pseudoBefore = window.getComputedStyle(node, ":before");
    if (pseudoBefore) {
      rect.pseudoBeforeContent = pseudoBefore.getPropertyValue("content");
    }
    var pseudoAfter = window.getComputedStyle(node, ":after");
    if (pseudoAfter) {
      rect.pseudoAfterContent = pseudoAfter.getPropertyValue("content");
    }
  }

  if (rect.isOnScreen && rect.isVisible) {
    var center = this.getRectCenter(rect);
    var relativeCenter = {
      x: center.x - windowRect.x,
      y: center.y - windowRect.y,
    };

    if (this.isPointInsideRect(center.x, center.y, windowRect)) {
      var nodeAtCenter = document.elementFromPoint(relativeCenter.x, relativeCenter.y);
      rect.isAccessible = !!nodeAtCenter && (this.isNodeContainsOther(node, nodeAtCenter)
          || this.isNodeContainsOther(nodeAtCenter, node)
          || this.isNodeContainsOther(node.parentNode, nodeAtCenter.parentNode));
    } else {
      this.log("Skipping isAccessible cause center of node is out of screen", node);
    }
  }

  return rect;
};

SelfHealingInfoGatherer.prototype.findElementByCoordinatesRecursive = function(x, y, currentDoc) {
  var elem = currentDoc.elementFromPoint(x, y);
  if (elem && (elem instanceof HTMLIFrameElement)) {
    var frameRect = elem.getBoundingClientRect();
    elem = this.findElementByCoordinatesRecursive(x - frameRect.x, y - frameRect.y, elem.contentWindow.document);
  }
  return elem;
};

SelfHealingInfoGatherer.prototype.getBatchNodeInfoByXpathes = function(root, shadowDom) {
  var self = this;

  if (!(root && root.nodes)) {
    return [];
  }

  var shadowRoot = null;
  var shadowRootXPath;
  if (shadowDom && shadowDom.roots && shadowDom.roots.length) {
    shadowRoot = self.findShadowRoot(shadowDom.roots);
    shadowRootXPath = shadowDom.roots[shadowDom.roots.length - 1];
  }
  var windowRect = self.getWindowRect();
  var results = [];

  root.nodes.forEach(function(nodeRef) {
    self.getNode(nodeRef, null, "", windowRect, results, shadowRootXPath, shadowRoot);
  });

  return results;
};
