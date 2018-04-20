package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;
import rk.tools.objectxpath.NodeType;

public class AttributeNode extends XPathNode {
    public AttributeNode(String name, int startIndex, int endIndex) {
        super(NodeType.ATTRIBUTE, NodeRelationship.CHILD, name, startIndex, endIndex);
    }
}
