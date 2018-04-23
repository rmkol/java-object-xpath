package rk.tools.objectxpath.xpath;

public class AttributeNode extends XPathNode {
    public AttributeNode(String name, int startIndex, int endIndex) {
        super(XPathNodeType.ATTRIBUTE, NodeRelationship.CHILD, name, startIndex, endIndex);
    }
}
