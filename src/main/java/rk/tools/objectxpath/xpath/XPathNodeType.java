package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents different XPath node types.
 */
public enum XPathNodeType {
    ANY("/(\\*)", "//(\\*)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    ANY,
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
    ANY_WITH_INDEX("/(\\*)\\[([0-9]+)]", "//(\\*)\\[([0-9]+)]") {
        @Override
        public XPathNode create(Matcher matcher) {
            int nodeIndex = Integer.parseInt(matcher.group(2));
            return new NodeWithIndex(
                    ANY_WITH_INDEX,
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
    ANY_WITH_ATTRIBUTE("/(\\*)\\[@(.*)='(.*)']", "//(\\*)\\[@(.*)='(.*)']") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attr = matcher.group(2);
            Object attrValue = matcher.group(3);
            return new NodeWithAttribute(
                    ANY_WITH_ATTRIBUTE,
                    nodeRelationship(matcher),
                    nodeName(matcher),
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher),
                    attr,
                    attrValue
            );
        }
    },
    ROOT("(^/$)", "") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    ROOT,
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
    SIMPLE("/([a-zA-Z]+)", "//([a-zA-Z]+)") {
        @Override
        public XPathNode create(Matcher matcher) {
            return new XPathNode(
                    SIMPLE,
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
    WITH_INDEX("/([a-zA-Z]+)\\[([0-9]+)]", "//([a-zA-Z]+)\\[([0-9]+)]") {
        @Override
        public XPathNode create(Matcher matcher) {
            int nodeIndex = Integer.parseInt(matcher.group(2));
            return new NodeWithIndex(
                    WITH_INDEX,
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
    WITH_ATTRIBUTE("/([a-zA-Z]+)\\[@(.*)='(.*)']", "//([a-zA-Z]+)\\[@(.*)='(.*)']") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attr = matcher.group(2);
            Object attrValue = matcher.group(3);
            return new NodeWithAttribute(
                    WITH_ATTRIBUTE,
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
    ATTRIBUTE("/@([a-zA-Z]+)", "//@([a-zA-Z]+)") {
        @Override
        public XPathNode create(Matcher matcher) {
            String attribute = matcher.group(1);
            return new AttributeNode(
                    attribute,
                    nodeStartIndex(matcher),
                    nodeEndIndex(matcher)
            );
        }
    };

    public final Pattern patternChild;
    public final Pattern patternDescendant;

    XPathNodeType(String patternChild, String patternDescendant) {
        this.patternChild = Pattern.compile(patternChild);
        this.patternDescendant = Pattern.compile(patternDescendant);
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
