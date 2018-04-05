/*
 * Created by Roman Kolesnik on 2018-03-14
 */
package rk.tools.objectxpath;

import rk.tools.objectxpath.xpath.NodeWithAttr;
import rk.tools.objectxpath.xpath.NodeWithIndex;
import rk.tools.objectxpath.xpath.XPathNode;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static rk.tools.objectxpath.Commons.arrayListOf;
import static rk.tools.objectxpath.Commons.transformList;
import static rk.utils.reflection.ReflectionUtils.*;

public class XPath {

    static final Logger logger = Logger.getAnonymousLogger();

    static final List<NodeType> NODE_TYPES = arrayListOf(
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

    Optional<XPathNode> getNextXPathNode(String xPath) {
        for (NodeType nodeType : NODE_TYPES) {
            Matcher matcher = nodeType.patternChild.matcher(xPath);
            if (nodeType.matches(matcher)) {
                return Optional.of(nodeType.create(matcher));
            }
        }
        return Optional.empty();
    }

    List<XPathNode> parseXPath(String xPath) {
        List<XPathNode> nodes = arrayListOf();
        Optional<XPathNode> node_;
        while ((node_ = getNextXPathNode(xPath)).isPresent()) {
            XPathNode node = node_.get();
            nodes.add(node);
            xPath = xPath.substring(node.endIndex);
        }
        return nodes;
    }

    //todo support root node - /
    //todo collect multiple results of //
    //todo support any node - * (previous item must be implemented)
    //todo support parent navigation /..
    //todo collection with null items

    private Node getNodeWithIndex(Node root, int index) {
        if (root.children.size() == 0) {
            throw new IllegalStateException(root.path + " has no children"); //todo custom exception
        }
        if (index >= root.children.size()) {
            throw new IllegalStateException(root.path + " has only " + root.children.size() + " children"); //todo custom exception
        }
        return root.children.get(index);
    }

    private Node getNodeWithAttribute(Node root, String attrName, Object attrValue) {
        //todo primitive error
        //todo allow custom type comparator?
        if (isCollection(root.value)) { //todo add null support
            Node foundNode = null;
            for (Node item : root.children) {
                String value = String.valueOf(getFieldValue(attrName, item.value));
                if (Objects.equals(attrValue, value)) {
                    foundNode = item;
                    break;
                }
            }
            if (foundNode == null) {
                throw new IllegalStateException("couldn't find item with '" + attrName + "' = " + "'" + attrValue + "' in " + root.path); //todo custom exception
            }
            return foundNode;
        }
        String value = String.valueOf(getFieldValue(attrName, root.value));
        if (Objects.equals(attrValue, value)) {
            return root;
        }
        throw new IllegalStateException(root.path + " doesn't have attribute '" + attrName + "' with value " + "'" + attrValue + "'"); //todo custom exception
    }

    public Object process(String xPath, Object object) {
        List<XPathNode> xPathNodes = parseXPath(xPath);
        Node root = objectToTree(object);
        for (XPathNode xPathNode : xPathNodes) {
            if (root.name.equals(xPathNode.name)) {
                continue;
            }
            Optional<Node> node = findNextNode(root, xPathNode);
            if (node.isPresent()) {
                root = node.get();
            } else {
                throw new IllegalStateException("couldn't find node by XPath " + xPath); //todo custom exception
            }
            if (xPathNode.getType() == NodeType.WITH_INDEX) {
                root = getNodeWithIndex(root, ((NodeWithIndex) xPathNode).index - 1);
                continue;
            }
            if (xPathNode.getType() == NodeType.WITH_ATTRIBUTE) {
                root = getNodeWithAttribute(root, ((NodeWithAttr) xPathNode).attrName,
                        ((NodeWithAttr) xPathNode).attrValue);
            }
        }
        return root.value;
    }

    private Optional<Node> findAttribute(Node node, String name) {
        for (Node attr : node.children) {
            if (attr.name.equals(name)) {
                return Optional.of(attr);
            }
        }
        return Optional.empty();
    }

    private Optional<Node> findNextNode(Node parent, XPathNode xPathNode) {
        if (xPathNode.getType() == NodeType.ATTRIBUTE) {
            return findAttribute(parent, xPathNode.name);
        }
        for (Node child : parent.children) {
            if (child.name.equals(xPathNode.name)) {
                return Optional.of(child);
            }
            if (xPathNode.relationship == NodeRelationship.DESCENDANT) { //todo check for primitive node
                Optional<Node> descendant = findNextNode(child, xPathNode);
                if (descendant.isPresent()) {
                    return descendant;
                }
            }
        }
        return Optional.empty();
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
