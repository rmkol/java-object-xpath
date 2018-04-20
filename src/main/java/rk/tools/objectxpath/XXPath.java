/*
 * Created by Roman Kolesnik on 2018-03-14
 */
package rk.tools.objectxpath;

import rk.tools.objectxpath.exception.InvalidXPathExpressionError;
import rk.tools.objectxpath.xpath.NodeWithAttribute;
import rk.tools.objectxpath.xpath.NodeWithIndex;
import rk.tools.objectxpath.xpath.XPathNode;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static rk.tools.objectxpath.Commons.arrayListOf;
import static rk.tools.objectxpath.Commons.transformList;
import static rk.utils.reflection.ReflectionUtils.*;

public class XXPath {

    static final Logger logger = Logger.getAnonymousLogger();

    static final List<NodeType> NODE_TYPES = arrayListOf(
            NodeType.ROOT,
            NodeType.WITH_ATTRIBUTE,
            NodeType.WITH_INDEX,
            NodeType.SIMPLE,
            NodeType.ATTRIBUTE
    );

    /**
     * {@link Set} of wrapper types considered as 'primitive'.
     */
    private static final Set<Class> primitiveTypes = new HashSet<>();

    static {
        primitiveTypes.add(Boolean.class);
        primitiveTypes.add(Character.class);
        primitiveTypes.add(Byte.class);
        primitiveTypes.add(Short.class);
        primitiveTypes.add(Integer.class);
        primitiveTypes.add(Long.class);
        primitiveTypes.add(Float.class);
        primitiveTypes.add(Double.class);
        primitiveTypes.add(String.class);
    }

    /**
     * Finds XPath node in provided XPath string.
     *
     * @param xPath XPath string
     * @return {@link Optional} of {@link XPathNode}
     */
    private Optional<XPathNode> findNextXPathNode(String xPath) {
        for (NodeType nodeType : NODE_TYPES) {
            Matcher matcher = nodeType.patternChild.matcher(xPath);
            if (nodeType.matches(matcher)) {
                return Optional.of(nodeType.create(matcher));
            }
        }
        return Optional.empty();
    }

    /**
     * Parses provided XPath string and converts it
     * into a list of {@link XPathNode}.
     *
     * @param xPath XPath string
     * @return list of {@link XPathNode}
     */
    private List<XPathNode> parseXPath(String xPath) {
        List<XPathNode> nodes = new LinkedList<>();
        Optional<XPathNode> node_;
        while ((node_ = findNextXPathNode(xPath)).isPresent()) {
            XPathNode node = node_.get();
            nodes.add(node);
            xPath = xPath.substring(node.endIndex);
        }
        return nodes;
    }

    //todo collect multiple results of //
    //todo support any node - * (previous item must be implemented)
    //todo support parent navigation /..
    //todo collection with null items
    //todo map support

    /**
     * Extracts a child node with specific index.
     *
     * @param node  parent node
     * @param index index of a child node
     * @return found child node
     */
    private Optional<Node> getChildNode(Node node, int index) {
        if (node.children.size() == 0) {
            return Optional.empty();
        }
        if (index >= node.children.size()) {
            return Optional.empty();
        }
        return Optional.of(node.children.get(index));
    }

    /**
     * Finds a node with a specific attribute.
     * If provided node represents a simple object,
     * then the method will check that it has an attribute with
     * provided name and value. Otherwise exception will be thrown.
     * <p>
     * If provided node represents a collection
     * then the method will try to find an item which
     * has an attribute with provided name and value. Otherwise
     * Exception will be thrown.
     *
     * @param node      specific object tree node
     * @param attrName  some attribute name
     * @param attrValue some attribute value
     * @return matching node
     */
    private List<Node> getNodeWithAttribute(Node node, String attrName, Object attrValue) {
        //todo primitive error
        //todo allow custom type comparator?
        List<Node> result = arrayListOf();
        if (isCollection(node.value)) { //todo add null support
            for (Node item : node.children) {
                String value = string(getFieldValue(attrName, item.value));
                if (Objects.equals(attrValue, value)) {
                    result.add(item);
                }
            }
            return result;
        }
        String value = string(getFieldValue(attrName, node.value));
        if (Objects.equals(attrValue, value)) {
            result.add(node);
        }
        return result;
    }

    public Object process(String xPath, Object object) {
        checkXpathExpression(xPath);
        List<XPathNode> xPathNodes = parseXPath(xPath);
        List<Node> nodes = arrayListOf(objectToTree(object));
        List<Node> result = arrayListOf();
        while (xPathNodes.size() > 0) {
            XPathNode xPathNode = xPathNodes.remove(0);
            if (xPathNode.type == NodeType.ROOT) {
                result.add(nodes.get(0));
                break;
            }
            for (Node node : Lists.removeAll(nodes)) {
                List<Node> descendants = findNextNode(node, xPathNode);
                for (Node descendant : descendants) {
                    node = descendant;
                    if (xPathNode.type == NodeType.WITH_INDEX) {
                        Optional<Node> child = getChildNode(node, ((NodeWithIndex) xPathNode).index - 1);
                        if (child.isPresent()) {
                            nodes.add(child.get());
                            if (xPathNodes.size() == 0) { //last XPath node
                                result.add(child.get());
                            }
                        }
                    } else if (xPathNode.type == NodeType.WITH_ATTRIBUTE) {
                        List<Node> _nodes = getNodeWithAttribute(node, ((NodeWithAttribute) xPathNode).attrName,
                                ((NodeWithAttribute) xPathNode).attrValue);
                        nodes.addAll(_nodes);
                        if (xPathNodes.size() == 0) { //last XPath node
                            result.addAll(_nodes);
                        }
                    } else {
                        nodes.add(node);
                        if (xPathNodes.size() == 0) { //last XPath node
                            result.add(node);
                        }
                    }
                }
            }
        }
        result.removeIf(node -> node.value == null);
        if (result.size() == 0) {
            return null;
        }
        if (result.size() == 1) {
            return result.get(0).value;
        }
        return result.stream()
                .map(node -> node.value)
                .collect(Collectors.toList());
    }

    private void checkXpathExpression(String expression) throws InvalidXPathExpressionError {
        try {
            XPathFactory.newInstance().newXPath().compile(expression);
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionError(expression);
        }
    }

    private List<Node> findAttributeNode(Node node, XPathNode xPathNode) {
        List<Node> nodes = arrayListOf();
        for (Node attr : node.children) {
            if (attr.name.equals(xPathNode.name)) {
                nodes.add(attr);
            }
            if (xPathNode.relationship == NodeRelationship.DESCENDANT) {
                attr.children.forEach(child->{
                    nodes.addAll(findAttributeNode(child, xPathNode));
                });
            }
        }
        return nodes;
    }

    private List<Node> findNextNode(Node parent, XPathNode xPathNode) {
        if (xPathNode.type == NodeType.ATTRIBUTE) {
            return findAttributeNode(parent, xPathNode);
        }
        List<Node> nodes = arrayListOf();
        for (Node child : parent.children) {
            if (child.name.equals(xPathNode.name)) {
                nodes.add(child);
            }
            if (xPathNode.relationship == NodeRelationship.DESCENDANT) { //todo check for primitive node
                nodes.addAll(findNextNode(child, xPathNode));
            }
        }
        return nodes;
    }

    /**
     * Creates a 'tree' from an object.
     *
     * @param object an object for which a tree will created
     * @return tree root node
     */
    private Node objectToTree(Object object) {
        Node root = new Node();
        root.name = getNameFor(object);
        root.value = object;
        root.path = "/" + root.name;
        if (isPrimitive(object) || isMap(object) || isCollection(object)) {
            return root;
        }
        root.children = transformList(getAllFieldsOf(object),
                field -> toNode(root, getFieldValue(field, object), field.getName(), null));
        return root;
    }

    private Node toNode(Node parent, Object value, String name, Integer index) {
        logger.info("processing node " + name + " with parent " + parent);

        Node node = new Node();
        node.name = name;
        node.value = value;
        node.path = index == null
                ? parent.path + "/" + name
                : parent.path + "/" + name + "[" + index + "]";
        node.parent = parent;
        if (null == value || isPrimitive(value)) {
            return node;
        }
        if (isMap(value)) {
            return node; //todo populate children somehow
        }
        if (isCollection(value)) {
            Collection collection = (Collection) value;
            Iterator iterator = collection.iterator();
            int i = 1;
            while (iterator.hasNext()) {
                Object item = iterator.next();
                //todo add null support here
                node.children.add(toNode(node, item, getNameFor(item), i++));
            }
            return node;
        }
        node.children = transformList(getAllFieldsOf(value.getClass()),
                field -> toNode(node, getFieldValue(field, value), field.getName(), null));
        return node;
    }

    //TODO allow to customize primitive types
    private static boolean isPrimitive(Object object) {
        Class type = object.getClass();
        return type.isPrimitive() || primitiveTypes.contains(type);
    }

    private String getNameFor(Object object) {
        String name = object.getClass().getSimpleName();
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private String string(Object object) {
        return String.valueOf(object);
    }

    /**
     * Represents object tree node.
     */
    private class Node {
        Node parent;
        String name;
        String path;
        Object value;
        List<Node> children = arrayListOf();

        @Override
        public String toString() {
            return path;
        }
    }
}
