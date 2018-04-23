package rk.tools.objectxpath.xpath;

public class XPathNode {
    public XPathNodeType type;
    public NodeRelationship relationship;
    public String name;
    public int startIndex;
    public int endIndex;

    public XPathNode(XPathNodeType type, NodeRelationship relationship, String name, int startIndex,
                     int endIndex) {
        this.type = type;
        this.relationship = relationship;
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
}
