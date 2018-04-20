package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;
import rk.tools.objectxpath.NodeType;

public class XPathNode {
    public NodeType type;
    public NodeRelationship relationship;
    public String name;
    public int startIndex;
    public int endIndex;

    public XPathNode(NodeType type, NodeRelationship relationship, String name, int startIndex,
                     int endIndex) {
        this.type = type;
        this.relationship = relationship;
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
}
