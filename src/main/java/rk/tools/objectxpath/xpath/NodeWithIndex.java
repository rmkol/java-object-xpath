package rk.tools.objectxpath.xpath;

public class NodeWithIndex extends XPathNode {
    public int index;

    public NodeWithIndex(XPathNodeType type, NodeRelationship relationship, String name,
                         int startIndex, int endIndex, int index) {
        super(type, relationship, name, startIndex, endIndex);
        this.index = index;
    }
}
