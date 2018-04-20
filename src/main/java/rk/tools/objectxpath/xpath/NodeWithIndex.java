package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;
import rk.tools.objectxpath.NodeType;

public class NodeWithIndex extends XPathNode {
    public int index;

    public NodeWithIndex(NodeRelationship relationship, String name, int startIndex,
                         int endIndex, int index) {
        super(NodeType.WITH_INDEX, relationship, name, startIndex, endIndex);
        this.index = index;
    }

    public NodeType getType() {
        return NodeType.WITH_INDEX;
    }
}
