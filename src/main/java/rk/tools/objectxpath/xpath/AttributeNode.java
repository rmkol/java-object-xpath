package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;

public class AttributeNode extends XPathNode {
    public AttributeNode(String name, int startIndex, int endIndex) {
        super(XPathNodeType.ATTRIBUTE, NodeRelationship.CHILD, name, startIndex, endIndex);
    }
}
