package rk.tools.objectxpath.xpath;

public class NodeWithAttribute extends XPathNode {
    public String attrName;
    public Object attrValue;

    public NodeWithAttribute(XPathNodeType type, NodeRelationship relationship, String name,
                             int startIndex, int endIndex, String attrName, Object attrValue) {
        super(type, relationship, name, startIndex, endIndex);
        this.attrName = attrName;
        this.attrValue = attrValue;
    }
}
