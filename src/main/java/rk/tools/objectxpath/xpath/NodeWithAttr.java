package rk.tools.objectxpath.xpath;

import rk.tools.objectxpath.NodeRelationship;
import rk.tools.objectxpath.NodeType;

public class NodeWithAttr extends XPathNode {
    public String attrName;
    public Object attrValue;

    public NodeWithAttr(NodeRelationship relationship, String name, int startIndex,
                        int endIndex, String attrName, Object attrValue) {
        super(relationship, name, startIndex, endIndex);
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    public NodeType getType() {
        return NodeType.WITH_ATTRIBUTE;
    }
}
