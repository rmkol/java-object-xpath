//package rk.tools.objectxpath;
//
//import java.lang.reflect.Field;
//import java.util.*;
//
//import static rk.utils.reflection.ReflectionUtils.getAllFieldsOf;
//import static rk.utils.reflection.ReflectionUtils.getFieldValue;
//
//public class XML {
//    /**
//     * Primitive fields first.
//     */
//    private static final Comparator<Field> fieldsComparator = (f1, f2) -> {
//        if (isPrimitive(f1) && isPrimitive(f2)) {
//            return 0;
//        }
//        if (isPrimitive(f1)) {
//            return -1;
//        }
//        if (isPrimitive(f2)) {
//            return 1;
//        }
//        return 0;
//    };
//
//    /**
//     * {@link Set} of wrapper types considered as 'primitive'.
//     */
//    private static final Set<Class> primitiveTypes = new HashSet<>();
//
//    static {
//        primitiveTypes.add(Boolean.class);
//        primitiveTypes.add(Character.class);
//        primitiveTypes.add(Byte.class);
//        primitiveTypes.add(Short.class);
//        primitiveTypes.add(Integer.class);
//        primitiveTypes.add(Long.class);
//        primitiveTypes.add(Float.class);
//        primitiveTypes.add(Double.class);
//        primitiveTypes.add(String.class);
//    }
//
//    public String toXml(String rootName, Object object) {
//        if (object == null) {
//            return ""; //todo is it ok?
//        }
//        State state = new State();
//        state.setElement(rootName);
//        state.openElement();
//
//        if (isPrimitive(object)) {
//            state.closeElement();
//            state.addValue(object.toString());
//        } else if (isCollection(object)) {
//            state.closeElement();
//            Collection<?> collection = (Collection<?>) object;
//            if (collection.size() > 0) {
//                Object firstItem = collection.iterator().next();
//                String itemName = isPrimitive(firstItem)
//                        ? "item"
//                        : getClassNameFor(firstItem);
//                collection.forEach(item -> {
//                    state.addElement(toXml(itemName, item));
//                });
//            }
//        } else if (isMap(object)) {
//            state.closeElement();
//            Map<?, ?> map = (Map<?, ?>) object;
//            map.forEach((key, value) -> {
//                state.addElement(toXml(key.toString(), value));
//            });
//        } else {
//            List<Field> fields = getAllFieldsOf(object.getClass());
//
//            List<Field> attributes = new ArrayList<>();
//            fields.removeIf(field -> {
//                if (isPrimitive(field)) {
//                    attributes.add(field);
//                    return true;
//                }
//                return false;
//            });
//            attributes.forEach(attribute -> {
//                Object attrValue = getFieldValue(attribute, object);
//                if (attrValue != null) { //todo customize null output
//                    state.addAttribute(attribute.getName(), attrValue.toString());
//                }
//            });
//            state.closeElement();
//
//            for (Field field : fields) {
//                Object element = getFieldValue(field, object);
//                if (element != null) { //todo is it ok?
//                    state.addElement(toXml(field.getName(), element));
//                }
//            }
//        }
//
//        state.close();
//        return state.getXml();
//    }
//
//    private boolean isMap(Object object) {
//        return Map.class.isAssignableFrom(object.getClass());
//    }
//
//    //todo consider moving to ReflectionUtils
//    private boolean isCollection(Field field) {
//        return Collection.class.isAssignableFrom(field.getType());
//    }
//
//    private boolean isCollection(Object object) {
//        return Collection.class.isAssignableFrom(object.getClass());
//    }
//
//    //todo consider moving to ReflectionUtils
//    private boolean isMap(Field field) {
//        return Map.class.isAssignableFrom(field.getType());
//    }
//
//    //TODO allow to customize primitive types
//    private static boolean isPrimitive(Field field) {
//        Class type = field.getType();
//        return type.isPrimitive() || primitiveTypes.contains(type);
//    }
//
//    //TODO allow to customize primitive types
//    private static boolean isPrimitive(Object object) {
//        Class type = object.getClass();
//        return type.isPrimitive() || primitiveTypes.contains(type);
//    }
//
//    private String getClassNameFor(Object object) {
//        return object.getClass().getSimpleName();
//    }
//
//    private class State {
//        private String element;
//        private final StringBuilder xml = new StringBuilder();
//
//        public String getXml() {
//            return xml.toString();
//        }
//
//        public void addValue(String value) {
//            xml.append(value);
//        }
//
//        public void addElement(String element) {
//            xml.append(element).append("\n");
//        }
//
//        public void close() {
//            xml.append("</").append(this.element).append(">");
//        }
//
//        public void setElement(String element) {
//            if (this.element != null) {
//                xml.append("</").append(this.element).append(">\n");
//            }
//            this.element = element;
//        }
//
//        void openElement() {
//            xml.append("<").append(element);
//        }
//
//        void closeElement() {
//            xml.append(">\n");
//        }
//
//        void addAttribute(String name, String value) {
//            xml.append(" ").append(name).append("=").append("\"").append(value).append("\"");
//        }
//    }
//}
