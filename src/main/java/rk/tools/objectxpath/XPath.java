/*
 * Created by Roman Kolesnik on 2018-03-14
 */
package rk.tools.objectxpath;

import rk.tools.objectxpath.xpath.NodeWithAttr;
import rk.tools.objectxpath.xpath.NodeWithIndex;
import rk.tools.objectxpath.xpath.XPathNode;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static rk.tools.objectxpath.Commons.arrayListOf;
import static rk.tools.objectxpath.Commons.transformList;
import static rk.utils.reflection.ReflectionUtils.getAllFieldsOf;
import static rk.utils.reflection.ReflectionUtils.getFieldValue;

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

    public Object process(String xPath, Object object) {
        List<XPathNode> xPathNodes = parseXPath(xPath);
        Node root = objectToTree(object);
        for (XPathNode xPathNode : xPathNodes) {
            if (root.name.equals(xPathNode.name)) {
                continue;
            }
            Optional<Node> found = findNextNode(root, xPathNode);
            if (found.isPresent()) {
                root = found.get();
            } else {
                throw new IllegalStateException("couldn't find node by XPath " + xPath); //todo custom exception
            }
            if (xPathNode.getType() == NodeType.WITH_INDEX) {
                int index = ((NodeWithIndex) xPathNode).index - 1;
                if (root.children.size() == 0) {
                    throw new IllegalStateException(root.path + " has no children"); //todo custom exception
                }
                if (index >= root.children.size()) {
                    throw new IllegalStateException(root.path + " has only " + root.children.size() + " children"); //todo custom exception
                }
                root = root.children.get(index);
                continue;
            }
            if (xPathNode.getType() == NodeType.WITH_ATTRIBUTE) {
                //todo primitive error
                String attrName = ((NodeWithAttr) xPathNode).attrName;
                Object attrValue = ((NodeWithAttr) xPathNode).attrValue;
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
                    if (foundNode != null) {
                        root = foundNode;
                        continue;
                    }
                    throw new IllegalStateException("couldn't find item with '" + attrName + "' = " + "'" + attrValue + "' in " + root.path); //todo custom exception
                }
                String value = String.valueOf(getFieldValue(attrName, root.value));
                if (Objects.equals(attrValue, value)) {
                    continue;
                }
                throw new IllegalStateException(root.path + " doesn't have attribute '" + attrName + "' with value " + "'" + attrValue + "'"); //todo custom exception
            }
        }
        return root.value;
    }

    private Optional<Node> findAttribute(Node node, String name) {
        for (Node attr : node.attributes) {
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

    private boolean isMap(Object object) {
        return Map.class.isAssignableFrom(object.getClass());
    }

    //todo consider moving to ReflectionUtils
    private boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    private boolean isCollection(Object object) {
        return Collection.class.isAssignableFrom(object.getClass());
    }

    //todo consider moving to ReflectionUtils
    private boolean isMap(Field field) {
        return Map.class.isAssignableFrom(field.getType());
    }

    //TODO allow to customize primitive types
    private static boolean isPrimitive(Field field) {
        Class type = field.getType();
        return type.isPrimitive() || primitiveTypes.contains(type);
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

    private List<Field> removePrimitiveFieldsFrom(List<Field> fields) {
        List<Field> primitiveFields = arrayListOf();
        fields.removeIf(field -> {
            if (isPrimitive(field)) {
                primitiveFields.add(field);
                return true;
            }
            return false;
        });
        return primitiveFields;
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
        List<Field> children = getAllFieldsOf(object);
        List<Field> attributes = removePrimitiveFieldsFrom(children);
        root.attributes = transformList(attributes, field ->
                toNode(root, getFieldValue(field, object), field.getName(), null));
        root.children = transformList(children, field ->
                toNode(root, getFieldValue(field, object), field.getName(), null));
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
        List<Field> children = getAllFieldsOf(value.getClass());
        List<Field> attributes = removePrimitiveFieldsFrom(children);
        node.attributes = transformList(attributes, field ->
                toNode(node, getFieldValue(field, value), field.getName(), null));
        node.children = transformList(children, field ->
                toNode(node, getFieldValue(field, value), field.getName(), null));
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
        List<Node> attributes = arrayListOf();
        List<Node> children = arrayListOf();

        @Override
        public String toString() {
            return path;
        }
    }
}
