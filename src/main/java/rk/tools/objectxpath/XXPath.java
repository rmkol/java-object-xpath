/*
 * Created by Roman Kolesnik on 2018-03-14
 */
package rk.tools.objectxpath;

import rk.tools.objectxpath.exception.InvalidXPathExpressionError;
import rk.tools.objectxpath.xpath.NodeWithAttribute;
import rk.tools.objectxpath.xpath.NodeWithIndex;
import rk.tools.objectxpath.xpath.XPathNode;
import rk.tools.objectxpath.xpath.XPathNodeType;
import rk.utils.reflection.ReflectionUtils;
import rk.utils.reflection.exception.FieldNotFoundError;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static rk.tools.objectxpath.Commons.arrayListOf;
import static rk.tools.objectxpath.Commons.transformList;
import static rk.utils.reflection.ReflectionUtils.*;

public class XXPath {

    static final Logger logger = Logger.getAnonymousLogger();

    static final List<XPathNodeType> NODE_TYPES = arrayListOf( //order is important here
            XPathNodeType.ROOT,
            XPathNodeType.ANY_WITH_ATTRIBUTE,
            XPathNodeType.ANY_WITH_INDEX,
            XPathNodeType.WITH_ATTRIBUTE,
            XPathNodeType.WITH_INDEX,
            XPathNodeType.ANY,
            XPathNodeType.SIMPLE,
            XPathNodeType.ATTRIBUTE
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
        for (XPathNodeType nodeType : NODE_TYPES) {
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

    //todo map support
    //todo support any node - * (previous item must be implemented)
    //todo support parent navigation /..
    //todo collection with null items

    private boolean nodeHasAttribute(Node node, String attrName, Object attrValue) {
        return node.value != null && getFieldValue(attrName, node.value)
                .filter(attr -> Objects.equals(attrValue, attr)).isPresent();
    }

    private Optional<Node> getNodeWithIndex(List<Node> nodes, int index) {
        if (nodes.size() == 0) {
            return Optional.empty();
        }
        if (index >= nodes.size()) {
            return Optional.empty();
        }
        return Optional.of(nodes.get(index));
    }

    public Object process(String xPath, Object object) {
        checkXpathExpression(xPath);
        List<XPathNode> xPathNodes = parseXPath(xPath);
        List<Node> nodes = arrayListOf(objectToTree(object));
        List<Node> result = arrayListOf();
        while (xPathNodes.size() > 0) {
            XPathNode xPathNode = xPathNodes.remove(0);
            boolean lastXpathNode = xPathNodes.isEmpty();
            if (xPathNode.type == XPathNodeType.ROOT) {
                result.add(nodes.get(0));
                break;
            }
            for (Node node : Lists.removeAll(nodes)) {
                List<Node> descendants = findNextNode(node, xPathNode);
                if (descendants.size() == 1) {
                    node = descendants.get(0);
                    if (xPathNodeWithIndex(xPathNode)) { //one node returned, pick a child with index
                        int index = ((NodeWithIndex) xPathNode).index - 1;
                        Optional<Node> child = getNodeWithIndex(node.children, index);
                        if (child.isPresent()) {
                            nodes.add(child.get());
                            if (lastXpathNode) {
                                result.add(child.get());
                            }
                        }
                        continue;
                    }
                    if (nodeMatchesXpathNode(node, xPathNode)) {
                        nodes.add(node);
                        if (lastXpathNode) {
                            result.add(node);
                        }
                    }
                } else {
                    if (xPathNodeWithIndex(xPathNode)) { //multiple nodes returned, pick a node with index
                        int index = ((NodeWithIndex) xPathNode).index - 1;
                        Optional<Node> _node = getNodeWithIndex(descendants, index);
                        if (_node.isPresent()) {
                            nodes.add(_node.get());
                            if (lastXpathNode) {
                                result.add(_node.get());
                            }
                        }
                        continue;
                    }
                    descendants.forEach(_node -> {
                        if (nodeMatchesXpathNode(_node, xPathNode)) {
                            nodes.add(_node);
                            if (lastXpathNode) {
                                result.add(_node);
                            }
                        }
                    });
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

    private boolean xPathNodeWithIndex(XPathNode xPathNode) {
        return xPathNode.type == XPathNodeType.ANY_WITH_INDEX
                || xPathNode.type == XPathNodeType.WITH_INDEX;
    }

    private boolean xPathNodeWithAttribute(XPathNode xPathNode) {
        return xPathNode.type == XPathNodeType.ANY_WITH_ATTRIBUTE
                || xPathNode.type == XPathNodeType.WITH_ATTRIBUTE;
    }

    //todo rename
    private boolean nodeMatchesXpathNode(Node node, XPathNode xPathNode) {
        if (xPathNodeWithAttribute(xPathNode)) {
            return nodeHasAttribute(node, ((NodeWithAttribute) xPathNode).attrName,
                    ((NodeWithAttribute) xPathNode).attrValue);
        }
        return true;
    }

    private Optional<String> getFieldValue(String fieldName, Object holder) {
        try {
            Object value = ReflectionUtils.getFieldValue(fieldName, holder);
            return Optional.ofNullable(string(value));
        } catch (FieldNotFoundError fieldNotFoundError) {
            return Optional.empty();
        }
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
        for (Node attr : node.attributes) {
            if (attr.name.equals(xPathNode.name)) {
                nodes.add(attr);
            }
        }
        if (xPathNode.relationship == NodeRelationship.DESCENDANT) {
            node.children.forEach(child -> {
                nodes.addAll(findAttributeNode(child, xPathNode));
            });
        }
        return nodes;
    }

    private List<Node> findNextNode(Node parent, XPathNode xPathNode) {
        if (xPathNode.type == XPathNodeType.ATTRIBUTE) {
            return findAttributeNode(parent, xPathNode);
        }
        List<Node> nodes = arrayListOf();
        for (Node child : parent.children) {
            if (anyXpathNode(xPathNode)) {
                nodes.add(child);
            } else if (child.name.equals(xPathNode.name)) {
                nodes.add(child);
            }
            if (xPathNode.relationship == NodeRelationship.DESCENDANT) { //todo check for primitive node
                nodes.addAll(findNextNode(child, xPathNode));
            }
        }
        return nodes;
    }

    private boolean anyXpathNode(XPathNode xPathNode) {
        return xPathNode.type == XPathNodeType.ANY_WITH_ATTRIBUTE
                || xPathNode.type == XPathNodeType.ANY_WITH_INDEX
                || xPathNode.type == XPathNodeType.ANY;
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

        List<Field> fields = getAllFieldsOf(object);
        List<Field> attributes = arrayListOf();
        fields.removeIf(field -> {
            if (isPrimitive(field)) {
                attributes.add(field);
                return true;
            }
            return false;
        });
        root.attributes = transformList(attributes, field -> toNode(root, ReflectionUtils.getFieldValue(field, object),
                field.getName(), null));
        root.children = transformList(fields, field -> toNode(root, ReflectionUtils.getFieldValue(field, object),
                field.getName(), null));
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
            //todo map key type (string or number)
            Map map = (Map) value;
            //todo do not use stream here for performance reasons? (iterator instead)
            map.keySet().stream().findFirst().ifPresent(key->{
                if (isPrimitive(key)) {
                    map.entrySet().forEach(entry -> {
                        Map.Entry<Comparable, Object> _entry = (Map.Entry<Comparable, Object>) entry;
                        node.children.add(toNode(node, _entry.getValue(), string(_entry.getKey()), null));
                    });
                }
            });
            return node;
        }
        if (isCollection(value)) {
            Collection collection = (Collection) value;
            int i = 1;
            for (Object item : collection) {
                //todo add null support here
                node.children.add(toNode(node, item, getNameFor(item), i++));
            }
            return node;
        }

        List<Field> fields = getAllFieldsOf(value.getClass());
        List<Field> attributes = arrayListOf();
        fields.removeIf(field -> {
            if (isPrimitive(field)) {
                attributes.add(field);
                return true;
            }
            return false;
        });
        node.attributes = transformList(attributes, field -> toNode(node, ReflectionUtils.getFieldValue(field, value),
                field.getName(), null));
        node.children = transformList(fields, field -> toNode(node, ReflectionUtils.getFieldValue(field, value),
                field.getName(), null));
        return node;
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

    private String string(Object object) {
        return object == null ? null : String.valueOf(object);
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
