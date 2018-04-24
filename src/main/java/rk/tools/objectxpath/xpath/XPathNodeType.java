package rk.tools.objectxpath.xpath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents different XPath node types.
 */
public enum XPathNodeType {
    PARENT_NODE("/(\\.\\.)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    PARENT_NODE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    },
    ANY_NODE("/(\\*)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    ANY_NODE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    },
    /**
     * Any node identified by name and index.
     * <p>'/*[2]'</p>
     */
    ANY_NODE_WITH_INDEX("/(\\*)\\[([0-9]+)]") {
        @Override
        public XPathNode create(Matcher matcher) {
            int nodeIndex = Integer.parseInt(matcher.group(2));
            return new NodeWithIndex(
                    ANY_NODE_WITH_INDEX,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher),
                    nodeIndex
            );
        }
    },
    /**
     * Any node identified by name and some attribute's value.
     * <p>'/*[@model='m1']'</p>
     */
    ANY_NODE_WITH_ATTRIBUTE("/(\\*)\\[@(.*)='(.*)']") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attr = matcher.group(2);
            Object attrValue = matcher.group(3);
            return new NodeWithAttribute(
                    ANY_NODE_WITH_ATTRIBUTE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher),
                    attr,
                    attrValue
            );
        }
    },
    ROOT_NODE("(^/$)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    ROOT_NODE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    },
    /**
     * Simple node which is identified only by name.
     * <p>'/car'</p>
     */
    SIMPLE_NODE("/([a-zA-Z0-9_-]+)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    SIMPLE_NODE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    },
    /**
     * A node which is identified by name and index.
     * <p>'/car[2]'</p>
     */
    NODE_WITH_INDEX("/([a-zA-Z0-9_-]+)\\[([0-9]+)]") {
        @Override
        public XPathNode create(Matcher matcher) {
            int nodeIndex = Integer.parseInt(matcher.group(2));
            return new NodeWithIndex(
                    NODE_WITH_INDEX,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher),
                    nodeIndex
            );
        }
    },
    /**
     * A node which is identified by name and some attribute's value.
     * <p>'/car[@model='m1']'</p>
     */
    NODE_WITH_ATTRIBUTE("/([a-zA-Z0-9_-]+)\\[@(.*)='(.*)']") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attr = matcher.group(2);
            Object attrValue = matcher.group(3);
            return new NodeWithAttribute(
                    NODE_WITH_ATTRIBUTE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher),
                    attr,
                    attrValue
            );
        }
    },
    /**
     * Attribute node.
     * <p>'/car/@model'</p>
     */
    NODE_ATTRIBUTE("/@([a-zA-Z0-9_-]+)") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attribute = matcher.group(1);
            return new AttributeNode(
                    attribute,
                    nodeRelationship(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    };

    public final Pattern pattern;

    XPathNodeType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Creates a node based on provided matcher.
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return instance of {@link XPathNode} or it's subclasses
     */
    public abstract XPathNode create(Matcher matcher);

    /**
     * Checks whether provided matcher matches node search pattern.
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return {@code true} if provided matcher matches node's regex patter
     * and {@code false} otherwise
     */
    public boolean matches(Matcher matcher) {
        return matcher.find() && (matcher.start() == 0 || matcher.start() == 1);
    }

    /**
     * Resolves node relationship to previous node.
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return node relationship to previous node
     */
    private static NodeRelationship nodeRelationship(Matcher matcher) {
        return matcher.start() == 0 ? NodeRelationship.CHILD : NodeRelationship.DESCENDANT;
    }

    /**
     * Resolves node start index (index of the first character of node substring in XPath string).
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return node start index
     */
    private static int nodeStartIndex(Matcher matcher) {
        return matcher.start();
    }

    /**
     * Resolves node end index (index of the last character of node substring in XPath string).
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return node end index
     */
    private static int nodeEndIndex(Matcher matcher) {
        return matcher.end();
    }

    /**
     * Extracts node name from provided matcher.
     *
     * @param matcher a matcher created based on node's regex pattern
     * @return node name
     */
    private static String nodeName(Matcher matcher) {
        return matcher.group(1);
    }
}
