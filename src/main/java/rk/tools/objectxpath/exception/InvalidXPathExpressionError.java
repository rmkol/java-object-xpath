package rk.tools.objectxpath.exception;

public class InvalidXPathExpressionError extends RuntimeException {
    public InvalidXPathExpressionError(String xPath) {
        super("Provided XPath expression is invalid - " + xPath);
    }
}
