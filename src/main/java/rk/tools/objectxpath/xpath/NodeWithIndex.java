package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;

public class NodeWithIndex extends XPathNode {
    public int index;

    public NodeWithIndex(XPathNodeType type, NodeRelationship relationship, String name,
                         int startIndex, int endIndex, int index) {
        super(type, relationship, name, startIndex, endIndex);
        this.index = index;
    }
}
