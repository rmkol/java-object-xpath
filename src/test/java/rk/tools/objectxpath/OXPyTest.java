package rk.tools.objectxpath;

import org.junit.jupiter.api.Test;
import rk.tools.objectxpath.exception.InvalidXPathExpressionError;
import rk.tools.objectxpath.object.Characteristic;
import rk.tools.objectxpath.object.Sedan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static rk.tools.objectxpath.Lists.arrayListOf;

@SuppressWarnings("unchecked")
class OXPyTest {

    final OXPy oxpy = new OXPy();
    final Sedan sedan = Sedan.createDefault();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<Object> result;
    List list;

    //todo map of map? :O
    //todo map with complex (non primitive) keys
    //todo map with number key

    @Test
    void test() {
    }

    @Test
    void listAsRoot() {
        List<String> list = arrayListOf("h1", "h2");
        this.result = oxpy.process("/", list);
        assertTrue(result.isPresent());
        assertEquals(list, result.get());

        this.result = oxpy.process("/*", list);
        assertTrue(result.isPresent());
        this.list = (List) result.get();
        assertEquals(2, this.list.size());
        assertTrue(this.list.stream().anyMatch(item -> item.equals("h1")));
        assertTrue(this.list.stream().anyMatch(item -> item.equals("h2")));

        this.result = oxpy.process("//*", list);
        assertTrue(result.isPresent());
        this.list = (List) result.get();
        assertEquals(2, this.list.size());
        assertTrue(this.list.stream().anyMatch(item -> item.equals("h1")));
        assertTrue(this.list.stream().anyMatch(item -> item.equals("h2")));

        this.result = oxpy.process("/*[1]", list);
        assertTrue(result.isPresent());
        assertNotNull(this.result.get());
        assertEquals("h1", this.result.get());

        this.result = oxpy.process("/*[3]", list);
        assertFalse(result.isPresent());
    }

    @Test
    void primitiveAsRoot() {
        result = oxpy.process("/", 25);
        assertTrue(result.isPresent());
        assertEquals(25, result.get());

        result = oxpy.process("/", "25");
        assertTrue(result.isPresent());
        assertEquals("25", result.get());

        result = oxpy.process("/", 22.6);
        assertTrue(result.isPresent());
        assertEquals(22.6, result.get());

        result = oxpy.process("/value", 25);
        assertFalse(result.isPresent());

        result = oxpy.process("/*", 25);
        assertFalse(result.isPresent());
    }

    @Test
    void mapAsRoot() {
        Map<String, String> map = new HashMap<>();
        map.put("record-11", "details-11");
        map.put("record-12", "details-12");

        result = oxpy.process("/record-11", map);
        assertTrue(result.isPresent());
        assertEquals("details-11", result.get());

        result = oxpy.process("/record-12", map);
        assertTrue(result.isPresent());
        assertEquals("details-12", result.get());

        result = oxpy.process("/*", map);
        assertTrue(result.isPresent());
        list = (List) result.get();
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(item -> item.equals("details-11")));
        assertTrue(list.stream().anyMatch(item -> item.equals("details-12")));

        result = oxpy.process("//*", map);
        assertTrue(result.isPresent());
        list = (List) result.get();
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(item -> item.equals("details-11")));
        assertTrue(list.stream().anyMatch(item -> item.equals("details-12")));
    }

    @Test
    void mapWithNumberKeys() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "item1");
        map.put(2, "item1");
        //todo add support for map with number keys
        assertThrows(InvalidXPathExpressionError.class, () -> oxpy.process("/1", map));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void mapWithComplexStringKeys() {
        sedan.details.put("detail-21", "details");
        result = oxpy.process("/details/detail-21", sedan);
        assertTrue(result.isPresent());
        assertEquals("details", result.get());

        result = oxpy.process("/details/d1", sedan);
        assertTrue(result.isPresent());
        assertEquals(sedan.details.get("d1"), result.get());
        sedan.details.clear();

        result = oxpy.process("/details/detail-21", sedan);
        assertFalse(result.isPresent());
    }

    @Test
    void emptyMap() {
        sedan.details.clear();
        result = oxpy.process("/details/detail1", sedan);
        assertFalse(result.isPresent());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void process() {
        result = processXpath("//engine/@volume");
        assertTrue(result.isPresent());
        assertEquals(2.0D, result.get());

        result = processXpath("/gears/gear[1]/characteristics/characteristic[2]");
        assertTrue(result.isPresent());
        assertEquals(sedan.getGears().get(0).characteristics.get(1), result.get());

        result = processXpath("/gears[@id='111']/characteristics");
        assertFalse(result.isPresent());

        result = processXpath("/gears/gear[@id='111']/characteristics");
        assertTrue(result.isPresent());
        assertEquals(sedan.getGears().get(0).characteristics, result.get());

        result = processXpath("/gears/gear[1]//characteristics/characteristic[1]/@details");
        assertTrue(result.isPresent());
        assertEquals(sedan.getGears().get(0).characteristics.get(0).details, result.get());

        result = processXpath("//gears[1]//characteristics[2]/@details");
        assertFalse(result.isPresent());

        result = processXpath("/countryCodes/item[1]");
        assertTrue(result.isPresent());
        assertEquals(sedan.countryCodes.get(0), result.get());

        result = processXpath("/countryCodes/item[2]");
        assertTrue(result.isPresent());
        assertEquals(sedan.countryCodes.get(1), result.get());

        result = processXpath("/gears/gear[2]//characteristics");
        assertTrue(result.isPresent());
        assertEquals(sedan.getGears().get(1).characteristics, result.get());

        result = processXpath("/engine");
        assertTrue(result.isPresent());
        assertEquals(sedan.engine, result.get());

        result = processXpath("/engine[@volume='2.0']");
        assertTrue(result.isPresent());
        assertEquals(sedan.engine, result.get());

        result = processXpath("/engine/characteristics");
        assertFalse(result.isPresent());

        result = processXpath("/engine//characteristics");
        assertTrue(result.isPresent());
        list = (List) result.get();
        assertEquals(3, list.size());
        List<Characteristic> characteristics = (List<Characteristic>) list.get(1);
        assertEquals(sedan.engine.gears.get(0).characteristics.get(0), characteristics.get(0));
        assertEquals(sedan.engine.gears.get(0).characteristics.get(1), characteristics.get(1));
        assertEquals(sedan.engine.gears.get(0).characteristics.get(2), characteristics.get(2));

        result = processXpath("//valve");
        assertTrue(result.isPresent());
        assertEquals(sedan.engine.valve, result.get());

        result = processXpath("//valve/@name");
        assertTrue(result.isPresent());
        assertEquals(sedan.engine.valve.name, result.get());

        result = processXpath("/");
        assertTrue(result.isPresent());
        assertEquals(sedan, result.get());

        result = processXpath("//@weight");
        assertTrue(result.isPresent());
        list = (List) result.get();
        assertEquals(5, list.size());

        //multiple items
        {
            result = processXpath("//characteristic[@details='size:1']");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(2, list.size());
            assertEquals(sedan.getGears().get(0).characteristics.get(1), list.get(0));
            assertEquals(sedan.getGears().get(2).characteristics.get(1), list.get(1));

            result = processXpath("/gears//gear");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(3, list.size());
            assertEquals(sedan.getGears().get(0), list.get(0));
            assertEquals(sedan.getGears().get(1), list.get(1));
            assertEquals(sedan.getGears().get(2), list.get(2));

            result = processXpath("//characteristic");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(7, list.size());
            assertEquals(sedan.getGears().get(0).characteristics.get(0), list.get(0));
            assertEquals(sedan.getGears().get(0).characteristics.get(1), list.get(1));
            assertEquals(sedan.getGears().get(2).characteristics.get(0), list.get(2));
            assertEquals(sedan.getGears().get(2).characteristics.get(1), list.get(3));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(0), list.get(4));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(1), list.get(5));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(2), list.get(6));
        }

        //any node *
        {
            result = processXpath("/*");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(5, list.size());
            assertEquals(sedan.details, list.get(0));
            assertEquals(sedan.getGears(), list.get(2));
            assertEquals(sedan.countryCodes, list.get(3));
            assertEquals(sedan.engine, list.get(4));

            result = processXpath("//*");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(28, list.size());

            result = processXpath("//*[@details='sn:22']");
            assertTrue(result.isPresent());
            assertEquals(sedan.engine.gears.get(0).characteristics.get(1), result.get());

            result = processXpath("/engine/*");
            assertTrue(result.isPresent());
            list = (List) result.get();
            assertEquals(3, list.size());
            assertNull(list.get(0));
            assertTrue(list.stream().anyMatch(o -> sedan.engine.valve.equals(o)));
            assertTrue(list.stream().anyMatch(o -> sedan.engine.gears.equals(o)));
        }

        //maps
        {
            result = processXpath("/details/d1");
            assertTrue(result.isPresent());
            assertEquals(sedan.details.get("d1"), result.get());

            result = processXpath("/details/d2");
            assertTrue(result.isPresent());
            assertEquals(sedan.details.get("d2"), result.get());

            result = processXpath("/details/d3");
            assertFalse(result.isPresent());
        }

        //.. parent node
        {
            result = processXpath("/gears/..");
            assertTrue(result.isPresent());
            assertEquals(sedan, result.get());

            result = processXpath("/gears/../gears");
            assertTrue(result.isPresent());
            assertEquals(sedan.getGears(), result.get());

            result = processXpath("/..");
            assertFalse(result.isPresent());

            result = processXpath("//*[@id='22']/../..");
            assertTrue(result.isPresent());
            assertEquals(sedan.getGears().get(0), result.get());
        }
    }

    @Test
    void nullObject() {
        assertThrows(NullPointerException.class, () -> oxpy.process("/", null));
    }

    @Test
    void processNegative() {
        assertThrows(NullPointerException.class, () -> checkInvalidXpath(null));
        checkInvalidXpath("/engine/");
        checkInvalidXpath("///");
        checkInvalidXpath("/[]");
    }

    Optional<Object> processXpath(String xPath) {
        return oxpy.process(xPath, sedan);
    }

    void checkInvalidXpath(String xPath) {
        try {
            processXpath(xPath);
        } catch (InvalidXPathExpressionError e) {
            return;
        }
        throw new AssertionError(xPath + " was treated like valid XPath expression");
    }
}