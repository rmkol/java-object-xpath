package rk.tools.objectxpath;

/**
 * Represents type of relationship between two XPath nodes.
 */
public enum NodeRelationship {
    /**
     * <p>'/car/engine'</p>
     */
    CHILD,
    /**
     * <p>'/car//engine'</p>
     */
    DESCENDANT
}