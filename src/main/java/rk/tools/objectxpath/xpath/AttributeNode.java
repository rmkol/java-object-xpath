package rk.tools.objectxpath.xpath;

public class AttributeNode extends XPathNode {
    public AttributeNode(String name, NodeRelationship relationship, int startIndex, int endIndex) {
        super(XPathNodeType.ATTRIBUTE, relationship, name, startIndex, endIndex);
    }
}