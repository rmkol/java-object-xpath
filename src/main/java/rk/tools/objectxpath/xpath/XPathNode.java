package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;
import rk.tools.objectxpath.NodeType;

public class XPathNode {
    public NodeRelationship relationship;
    public String name;
    public int startIndex;
    public int endIndex;

    public XPathNode(NodeRelationship relationship, String name, int startIndex, int endIndex) {
        this.relationship = relationship;
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public NodeType getType() {
        return NodeType.SIMPLE;
    }
}
